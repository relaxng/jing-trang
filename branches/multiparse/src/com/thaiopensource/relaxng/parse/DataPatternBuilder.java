package com.thaiopensource.relaxng.parse;

import org.relaxng.datatype.ValidationContext;

public interface DataPatternBuilder {
  void addParam(String name, String value, ValidationContext vc, Location loc, Annotations anno)
    throws BuildException;
  ParsedPattern makePattern(Location loc, Annotations anno)
    throws BuildException;
  ParsedPattern makePattern(ParsedPattern except, Location loc, Annotations anno)
    throws BuildException;
}
