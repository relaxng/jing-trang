package com.thaiopensource.relaxng.parse;

public interface ParsedPatternFuture<P> {
  P getParsedPattern() throws IllegalSchemaException;
}
