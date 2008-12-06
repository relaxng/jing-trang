package com.thaiopensource.relaxng.parse;

public interface ElementAnnotationBuilder<L, EA, CL extends CommentList<L>> extends Annotations<L, EA, CL> {
  void addText(String value, L loc, CL comments) throws BuildException;
  EA makeElementAnnotation() throws BuildException;
}
