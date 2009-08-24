package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

/**
 * This is used for the name class of an element pattern when the content expands to notAllowed.
 */
class NullNameClass implements NameClass {
  public boolean contains(Name name) {
    return false;
  }

  public int containsSpecificity(Name name) {
    return SPECIFICITY_NONE;
  }

  public int hashCode() {
    return NullNameClass.class.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof NullNameClass))
      return false;
    return true;
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitNull();
  }

  public boolean isOpen() {
    return false;
  }
}
