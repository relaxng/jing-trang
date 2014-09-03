package com.thaiopensource.validate.rng.impl;

import com.thaiopensource.relaxng.sax.PatternValidator;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.relaxng.pattern.ValidatorPatternBuilder;
import com.thaiopensource.validate.Validator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;

public class RngValidator extends PatternValidator implements Validator {
  public RngValidator(Pattern pattern, ValidatorPatternBuilder builder, ErrorHandler eh) {
    super(pattern, builder, eh);
  }

  public ContentHandler getContentHandler() {
    return this;
  }

  public DTDHandler getDTDHandler() {
    return this;
  }

  public void reset() {
    super.reset();
  }
}
