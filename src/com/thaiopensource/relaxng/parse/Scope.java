package com.thaiopensource.relaxng.parse;

public interface Scope {
  Scope getParent();
  ParsedPattern makeRef(String name, Location loc, Annotations anno) throws BuildException;
}
