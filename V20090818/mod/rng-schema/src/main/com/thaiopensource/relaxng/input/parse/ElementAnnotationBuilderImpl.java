package com.thaiopensource.relaxng.input.parse;

import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.relaxng.edit.ElementAnnotation;
import com.thaiopensource.relaxng.edit.TextAnnotation;
import com.thaiopensource.relaxng.edit.AttributeAnnotation;
import com.thaiopensource.relaxng.edit.AnnotationChild;
import com.thaiopensource.relaxng.parse.ElementAnnotationBuilder;
import com.thaiopensource.relaxng.parse.BuildException;

import java.util.List;

public class ElementAnnotationBuilderImpl implements ElementAnnotationBuilder<SourceLocation, ElementAnnotationBuilderImpl, CommentListImpl> {
  private final ElementAnnotation element;
  private CommentListImpl comments;

  ElementAnnotationBuilderImpl(CommentListImpl comments, ElementAnnotation element) {
    this.comments = comments;
    this.element = element;
  }

  public void addText(String value, SourceLocation loc, CommentListImpl comments) throws BuildException {
    TextAnnotation t = new TextAnnotation(value);
    t.setSourceLocation(loc);
    if (comments != null)
      element.getChildren().addAll(comments.list);
    element.getChildren().add(t);
  }

  public void addAttribute(String ns, String localName, String prefix, String value, SourceLocation loc)
          throws BuildException {
    AttributeAnnotation att = new AttributeAnnotation(ns, localName, value);
    att.setPrefix(prefix);
    att.setSourceLocation(loc);
    element.getAttributes().add(att);
  }

  public ElementAnnotationBuilderImpl makeElementAnnotation() throws BuildException {
    return this;
  }

  public void addElement(ElementAnnotationBuilderImpl ea) throws BuildException {
    ea.addTo(element.getChildren());
  }

  public void addComment(CommentListImpl comments) throws BuildException {
    if (comments != null)
      element.getChildren().addAll(comments.list);
  }

  public void addLeadingComment(CommentListImpl comments) throws BuildException {
    if (this.comments == null)
      this.comments = comments;
    else if (comments != null)
      this.comments.add(comments);
  }

  void addTo(List<AnnotationChild> elementList) {
    if (comments != null)
      elementList.addAll(comments.list);
    elementList.add(element);
  }
}
