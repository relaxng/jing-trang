package com.thaiopensource.relaxng;

class ErrorNameClass implements NameClass {
  public boolean contains(Name name) {
    return false;
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitError();
  }

  public boolean isOpen() {
    return false;
  }
}
