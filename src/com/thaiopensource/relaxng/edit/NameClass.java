package com.thaiopensource.relaxng.edit;

import com.thaiopensource.relaxng.parse.ParsedNameClass;

public abstract class NameClass extends Annotated implements ParsedNameClass {
  abstract Object accept(NameClassVisitor visitor);
}
