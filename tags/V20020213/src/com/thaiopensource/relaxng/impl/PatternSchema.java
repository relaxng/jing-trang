package com.thaiopensource.relaxng.impl;

import com.thaiopensource.relaxng.AbstractSchema;
import com.thaiopensource.relaxng.ValidatorHandler;
import org.xml.sax.ErrorHandler;

public class PatternSchema extends AbstractSchema {
  private final SchemaPatternBuilder spb;
  private final Pattern start;

  public PatternSchema(SchemaPatternBuilder spb, Pattern start) {
    this.spb = spb;
    this.start = start;
  }

  public ValidatorHandler createValidator(ErrorHandler eh) {
    return new PatternValidatorHandler(start, new ValidatorPatternBuilder(spb), eh);
  }
}
