package com.thaiopensource.relaxng.parse;

public interface ElementAnnotationBuilder extends Annotations {
  void addText(String value, Location loc) throws BuildException;
  ElementAnnotation makeElementAnnotation() throws BuildException;
}
