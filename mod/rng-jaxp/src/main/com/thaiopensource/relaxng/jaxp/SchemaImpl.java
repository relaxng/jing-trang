package com.thaiopensource.relaxng.jaxp;

import com.thaiopensource.relaxng.impl.Pattern;
import com.thaiopensource.relaxng.impl.SchemaPatternBuilder;
import com.thaiopensource.relaxng.impl.ValidatorPatternBuilder;

import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.validation.ValidatorHandler;

class SchemaImpl extends Schema {
  private final SchemaPatternBuilder spb;
  private final Pattern start;
  
  SchemaImpl(SchemaPatternBuilder spb, Pattern start) {
    this.spb = spb;
    this.start = start;
  }

  public Validator newValidator() {
    return new ValidatorImpl(start, new ValidatorPatternBuilder(spb));
  }

  public ValidatorHandler newValidatorHandler() {
    return new ValidatorHandlerImpl(start, new ValidatorPatternBuilder(spb));
  }
}
