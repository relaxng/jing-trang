package com.thaiopensource.relaxng;

public abstract class AbstractSchema implements Schema {
  public ValidatorHandler createValidator() {
    return createValidator(null);
  }
}
