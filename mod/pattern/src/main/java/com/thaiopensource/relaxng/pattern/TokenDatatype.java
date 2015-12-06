package com.thaiopensource.relaxng.pattern;

import org.relaxng.datatype.ValidationContext;

class TokenDatatype extends StringDatatype {
  public Object createValue(String str, ValidationContext vc) {
    return StringNormalizer.normalize(str);
  }
}
