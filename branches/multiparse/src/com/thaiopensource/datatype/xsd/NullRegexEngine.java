package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.xsd.regex.RegexEngine;
import com.thaiopensource.datatype.xsd.regex.Regex;

class NullRegexEngine implements RegexEngine {
  public Regex compile(String re) {
    return new Regex() {
	public boolean matches(String str) {
	  return true;
	}
      };
  }
}
