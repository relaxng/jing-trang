package com.thaiopensource.relaxng.edit;

public class AbstractComponentVisitor implements ComponentVisitor {
  public Object visitDefine(DefineComponent c) {
    return visitComponent(c);
  }

  public Object visitDiv(DivComponent c) {
    return visitComponent(c);
  }

  public Object visitInclude(IncludeComponent c) {
    return visitComponent(c);
  }

  public Object visitComponent(Component c) {
    return null;
  }
}
