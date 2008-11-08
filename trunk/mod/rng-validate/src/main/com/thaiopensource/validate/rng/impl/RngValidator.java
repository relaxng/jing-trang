package com.thaiopensource.validate.rng.impl;

import com.thaiopensource.relaxng.sax.PatternValidator;
import com.thaiopensource.relaxng.impl.Pattern;
import com.thaiopensource.relaxng.impl.ValidatorPatternBuilder;
import com.thaiopensource.validate.Validator;
import org.xml.sax.ErrorHandler;

public class RngValidator extends PatternValidator implements Validator {
  public RngValidator(Pattern pattern, ValidatorPatternBuilder builder, ErrorHandler eh) {
    super(pattern, builder, eh);
  }
}
