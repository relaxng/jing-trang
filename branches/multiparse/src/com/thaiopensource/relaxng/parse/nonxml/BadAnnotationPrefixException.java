package com.thaiopensource.relaxng.parse.nonxml;

class BadAnnotationPrefixException extends SyntaxException {
  BadAnnotationPrefixException(String prefix, Token t) {
    super("prefix \"" + prefix + "\" cannot be used as the prefix of an annotation", t);
  }
}
