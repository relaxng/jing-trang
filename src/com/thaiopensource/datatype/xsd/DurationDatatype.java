package com.thaiopensource.datatype.xsd;

class DurationDatatype extends RegexDatatype {
  static private final String PATTERN =
    "-?P([0-9]+Y)?([0-9]+M)?([0-9]+D)?(T([0-9]+H)?([0-9]+M)?(([0-9]+(\\.[0-9]*)?|\\.[0-9]+)S)?)?";
  DurationDatatype() {
    super(PATTERN);
  }

  public boolean lexicallyAllows(String str) {
    if (!super.lexicallyAllows(str))
      return false;
    char last = str.charAt(str.length()-1);
    // This enforces that there must be at least one component
    // and that T is omitted if all time components are omitted
    return last != 'P' && last != 'T';
  }
}
