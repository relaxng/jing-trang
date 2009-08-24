package com.thaiopensource.relaxng.input.parse;

import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.relaxng.edit.AttributeAnnotation;
import com.thaiopensource.relaxng.edit.AnnotationChild;
import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.parse.Annotations;
import com.thaiopensource.relaxng.parse.Context;
import com.thaiopensource.relaxng.parse.BuildException;

import java.util.List;
import java.util.Vector;

/**
 *
 */
public class AnnotationsImpl implements Annotations<SourceLocation, ElementAnnotationBuilderImpl, CommentListImpl> {
  private CommentListImpl comments;
  private final List<AttributeAnnotation> attributes = new Vector<AttributeAnnotation>();
  private final List<AnnotationChild> elements = new Vector<AnnotationChild>();
  private final Context context;

  AnnotationsImpl(CommentListImpl comments, Context context) {
    this.comments = comments;
    this.context = context;
  }

  public void addAttribute(String ns, String localName, String prefix, String value, SourceLocation loc)
          throws BuildException {
    AttributeAnnotation att = new AttributeAnnotation(ns, localName, value);
    att.setPrefix(prefix);
    att.setSourceLocation(loc);
    attributes.add(att);
  }

  public void addElement(ElementAnnotationBuilderImpl ea) throws BuildException {
    ea.addTo(elements);
  }

  public void addComment(CommentListImpl comments) throws BuildException {
    if (comments != null)
      elements.addAll(comments.list);
  }

  public void addLeadingComment(CommentListImpl comments) throws BuildException {
    if (this.comments == null)
      this.comments = comments;
    else if (comments != null)
      this.comments.add(comments);
  }

  void apply(Annotated subject) {
    subject.setContext(new NamespaceContextImpl(context));
    if (comments != null)
      subject.getLeadingComments().addAll(comments.list);
    subject.getAttributeAnnotations().addAll(attributes);
    List<AnnotationChild> list;
    if (subject.mayContainText())
      list = subject.getFollowingElementAnnotations();
    else
      list = subject.getChildElementAnnotations();
    list.addAll(elements);
  }
}
