package com.thaiopensource.relaxng;

import org.xml.sax.ErrorHandler;

public interface Schema {
  ValidatorHandler createValidator(ErrorHandler eh);
  ValidatorHandler createValidator();
}
