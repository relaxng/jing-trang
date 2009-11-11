package com.thaiopensource.datatype.xsd;

import com.thaiopensource.util.Utf16;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;

class TokenDatatype extends DatatypeBase implements Measure {

  TokenDatatype() { }
  TokenDatatype(int whiteSpace) {
    super(whiteSpace);
  }

  public boolean lexicallyAllows(String str) {
    return true;
  }

  String getLexicalSpaceKey() {
    return "string";
  }

  public boolean alwaysValid() {
    return true;
  }

  Object getValue(String str, ValidationContext vc) throws DatatypeException {
    return str;
  }

  Measure getMeasure() {
    return this;
  }

  public int getLength(Object obj) {
    String str = (String)obj;
    int len = str.length();
    int nSurrogatePairs = 0;
    for (int i = 0; i < len; i++)
      if (Utf16.isSurrogate1(str.charAt(i)))
	nSurrogatePairs++;
    return len - nSurrogatePairs;
  }
}
