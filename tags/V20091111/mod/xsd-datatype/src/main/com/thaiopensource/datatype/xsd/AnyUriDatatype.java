package com.thaiopensource.datatype.xsd;

import com.thaiopensource.util.Uri;

class AnyUriDatatype extends TokenDatatype {
  public boolean lexicallyAllows(String str) {
    return Uri.isValid(str);
  }

  String getLexicalSpaceKey() {
    return "uri";
  }

  public boolean alwaysValid() {
    return false;
  }
}
