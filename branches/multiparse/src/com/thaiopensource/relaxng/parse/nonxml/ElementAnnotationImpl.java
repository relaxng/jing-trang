package com.thaiopensource.relaxng.parse.nonxml;

import com.thaiopensource.relaxng.parse.ElementAnnotation;

final public class ElementAnnotationImpl implements ElementAnnotation {
  private final String str;

  public ElementAnnotationImpl(String str) {
    this.str = str;
  }

  public String toString() {
    return str;
  }
}
