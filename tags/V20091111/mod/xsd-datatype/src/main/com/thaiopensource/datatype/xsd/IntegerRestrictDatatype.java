package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.DatatypeException;

class IntegerRestrictDatatype extends ScaleRestrictDatatype {
  IntegerRestrictDatatype(DatatypeBase base) {
    super(base, 0);
  }

  boolean lexicallyAllows(String str) {
    return super.lexicallyAllows(str) && str.charAt(str.length() - 1) != '.';
  }

  void checkLexicallyAllows(String str) throws DatatypeException {
    if (!lexicallyAllows(str))
      throw createLexicallyInvalidException();
  }

  String getLexicalSpaceKey() {
    return "integer";
  }
}
