package com.thaiopensource.relaxng.edit;

public class AnyNameNameClass extends OpenNameClass {
  public AnyNameNameClass() {
  }

  public AnyNameNameClass(NameClass except) {
    super(except);
  }

  Object accept(NameClassVisitor visitor) {
    return visitor.visitAnyName(this);
  }
}
