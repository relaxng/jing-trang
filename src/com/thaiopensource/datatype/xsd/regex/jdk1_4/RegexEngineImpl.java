package com.thaiopensource.datatype.xsd.regex.jdk1_4;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;

import com.thaiopensource.datatype.xsd.RegexEngine;
import com.thaiopensource.datatype.xsd.Regex;
import com.thaiopensource.datatype.xsd.InvalidRegexException;

public class RegexEngineImpl implements RegexEngine {
  public RegexEngineImpl() {
    // Force a linkage error on instantiation if JDK 1.4 is not available.
    try {
      Pattern.compile("");
    }
    catch (PatternSyntaxException e) { }
  }

  public Regex compile(String str) throws InvalidRegexException {
    try {
      final Pattern pattern = Pattern.compile(Translator.translate(str));
      return new Regex() {
        public boolean matches(String str) {
          return pattern.matcher(str).matches();
        }
      };
    }
    catch (PatternSyntaxException e) {
      // don't use getIndex() since that returns the index in the translated regex not the original regex
      throw new InvalidRegexException(e.getDescription());
    }
  }
}
