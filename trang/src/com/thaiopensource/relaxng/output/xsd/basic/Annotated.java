package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class Annotated extends Located {
  private final Annotation annotation;

  public Annotated(SourceLocation location, Annotation annotation) {
    super(location);
    this.annotation = annotation;
  }

  public Annotation getAnnotation() {
    return annotation;
  }
}
