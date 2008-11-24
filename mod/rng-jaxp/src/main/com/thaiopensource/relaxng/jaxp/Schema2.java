package com.thaiopensource.relaxng.jaxp;

import javax.xml.validation.Schema;

/**
 *
 */
public abstract class Schema2 extends Schema {
  protected Schema2() { }

  public Validator2 newValidator() {
    return new ValidatorImpl(newValidatorHandler());
  }
  
  public abstract ValidatorHandler2 newValidatorHandler();
}
