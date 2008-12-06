package com.thaiopensource.relaxng.edit;

import com.thaiopensource.relaxng.parse.SchemaBuilder;

public abstract class NameClass extends Annotated {
  public static final String INHERIT_NS = SchemaBuilder.INHERIT_NS;
  public abstract <T> T accept(NameClassVisitor<T> visitor);
}
