package com.thaiopensource.relaxng;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;

public interface ValidatorHandler extends ContentHandler {
  boolean isValid();
  boolean isComplete();
  void reset();
  void setErrorHandler(ErrorHandler eh);
  ErrorHandler getErrorHandler();
}
