package com.thaiopensource.validation;

import org.xml.sax.DTDHandler;

import javax.xml.validation.ValidatorHandler;

/**
 *
 */
public abstract class ValidatorHandler2 extends ValidatorHandler implements DTDHandler {
  abstract public void reset();
  abstract public void setDTDHandler(DTDHandler dtdHandler);
  abstract public DTDHandler getDTDHandler();
}
