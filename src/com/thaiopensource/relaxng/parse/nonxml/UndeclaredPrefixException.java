package com.thaiopensource.relaxng.parse.nonxml;

class UndeclaredPrefixException extends SyntaxException {
  UndeclaredPrefixException(String prefix, Token t) {
    super("undeclared prefix \"" + prefix + "\"", t);
  }
}
