package com.thaiopensource.relaxng.edit;

public class AbstractNameClassVisitor implements NameClassVisitor {
  public Object visitChoice(ChoiceNameClass nc) {
    return visitNameClass(nc);
  }

  public Object visitAnyName(AnyNameNameClass nc) {
    return visitNameClass(nc);
  }

  public Object visitNsName(NsNameNameClass nc) {
    return visitNsName(nc);
  }

  public Object visitName(NameNameClass nc) {
    return visitNameClass(nc);
  }

  public Object visitNameClass(NameClass nc) {
    return null;
  }
}
