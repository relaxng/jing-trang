package com.thaiopensource.validate.rng.impl;

import com.thaiopensource.relaxng.pattern.IdTypeMap;
import com.thaiopensource.relaxng.sax.IdContentHandler;
import com.thaiopensource.validate.Validator;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;

public class IdValidator extends IdContentHandler implements Validator {
  public IdValidator(IdTypeMap idTypeMap, ErrorHandler eh) {
    super(idTypeMap, eh);
  }

  public ContentHandler getContentHandler() {
    return this;
  }

  public DTDHandler getDTDHandler() {
    return null;
  }

  public void reset() {
    super.reset();
  }
}
