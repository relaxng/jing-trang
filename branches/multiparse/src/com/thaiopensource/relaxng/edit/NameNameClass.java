package com.thaiopensource.relaxng.edit;

public class NameNameClass extends NameClass {
  public boolean mayContainText() {
    return true;
  }

  Object accept(NameClassVisitor visitor) {
    return visitor.visitName(this);
  }
}
