package com.thaiopensource.validation;

import org.xml.sax.DTDHandler;

import javax.xml.validation.ValidatorHandler;

/**
 * Extension to ValidatorHandler. This implements DTDHandler because some schema language
 * datatypes need to know whether a name is the name of a notation or an unparsed entity.
 * It also provides a reset() method.
 */
public abstract class ValidatorHandler2 extends ValidatorHandler implements DTDHandler {
  abstract public void reset();

  /**
   * Sets the DTD handler that receives the validation result.
   * @param dtdHandler the DTD hanlder
   */
  abstract public void setDTDHandler(DTDHandler dtdHandler);

  /**
   * Gets the DTD handler that receives the validation result.
   * @return the DTDHandler
   */
  abstract public DTDHandler getDTDHandler();
}
