package com.codeborne.selenide.impl;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ShouldableWebElement;
import org.openqa.selenium.WebElement;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.codeborne.selenide.DOM.assertElement;

public class ShouldableWebElementProxy implements InvocationHandler {
  public static ShouldableWebElement wrap(WebElement element) {
    return (element instanceof ShouldableWebElement) ?
        (ShouldableWebElement) element :
        (ShouldableWebElement) Proxy.newProxyInstance(
          element.getClass().getClassLoader(), new Class<?>[]{ShouldableWebElement.class}, new ShouldableWebElementProxy(element));
  }

  private final WebElement delegate;

  private ShouldableWebElementProxy(WebElement delegate) {
    this.delegate = delegate;
  }

  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if ("should".equals(method.getName()) || "shouldHave".equals(method.getName()) || "shouldBe".equals(method.getName())) {
      Condition[] conditions = (Condition[]) args[0];
      for (Condition condition : conditions) {
        assertElement(delegate, condition);
      }
      return proxy;
    }
    else if ("toString".equals(method.getName())) {
      return new Describe(delegate)
        .attr("id").attr("name").attr("class").attr("value").attr("disabled").attr("type").attr("placeholder")
        .attr("onclick").attr("onClick").attr("onchange").attr("onChange")
        .toString();
    }
    else {
      return delegateMethod(method, args);
    }
  }

  private Object delegateMethod(Method method, Object[] args) throws Throwable {
    try {
      return method.invoke(delegate, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }
}