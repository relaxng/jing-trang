package com.thaiopensource.relaxng.parse.nonxml;

class SyntaxException extends Exception {
  private final int line;
  private final int column;

  SyntaxException(String message, Token t) {
    super(message);
    this.line = t.beginLine;
    this.column = t.beginColumn;
  }

  public String getMessage() {
    return super.getMessage() + " at line " + line + ", column " + column + ".";
  }
}
