package com.thaiopensource.validation;

import javax.xml.validation.Schema;

/**
 * An extension to the Schema abstract class.  The main difference is that
 * there is a default implementation of newValidator in terms of newValidatorHandler.
 * Also both newValidator and newValidatorHandler return the extended versions
 * of Validator and ValidatorHandler (using covariant return types).
 */
public abstract class Schema2 extends Schema {
  protected Schema2() { }

  public Validator2 newValidator() {
    return new ValidatorImpl(newValidatorHandler());
  }
  
  public abstract ValidatorHandler2 newValidatorHandler();
}
