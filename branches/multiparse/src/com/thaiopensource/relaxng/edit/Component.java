package com.thaiopensource.relaxng.edit;

public abstract class Component extends Annotated {
  abstract Object accept(ComponentVisitor visitor);
}
