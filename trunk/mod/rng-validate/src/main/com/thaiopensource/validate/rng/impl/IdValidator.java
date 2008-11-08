package com.thaiopensource.validate.rng.impl;

import com.thaiopensource.relaxng.impl.IdSoundnessChecker;
import com.thaiopensource.relaxng.impl.IdTypeMap;
import com.thaiopensource.validate.Validator;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;

public class IdValidator extends IdSoundnessChecker implements Validator {
  public IdValidator(IdTypeMap idTypeMap, ErrorHandler eh) {
    super(idTypeMap, eh);
  }

  public ContentHandler getContentHandler() {
    return this;
  }

  public DTDHandler getDTDHandler() {
    return null;
  }
}
