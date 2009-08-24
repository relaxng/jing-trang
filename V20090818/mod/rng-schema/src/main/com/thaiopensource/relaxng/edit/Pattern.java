package com.thaiopensource.relaxng.edit;

public abstract class Pattern extends Annotated {
  public abstract <T> T accept(PatternVisitor<T> visitor);
}
