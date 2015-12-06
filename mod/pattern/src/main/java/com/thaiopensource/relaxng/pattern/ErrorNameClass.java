package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

class ErrorNameClass implements NameClass {
  public boolean contains(Name name) {
    return false;
  }

  public int containsSpecificity(Name name) {
    return SPECIFICITY_NONE;
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitError();
  }

  public boolean isOpen() {
    return false;
  }
}
