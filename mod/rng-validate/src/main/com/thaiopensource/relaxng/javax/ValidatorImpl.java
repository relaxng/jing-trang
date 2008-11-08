package com.thaiopensource.relaxng.javax;

import com.thaiopensource.relaxng.impl.Pattern;
import com.thaiopensource.relaxng.impl.ValidatorPatternBuilder;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Validator;
import java.io.IOException;

class ValidatorImpl extends Validator {
  private final ValidatorHandlerImpl handler;

  ValidatorImpl(Pattern pattern, ValidatorPatternBuilder builder) {
    handler = new ValidatorHandlerImpl(pattern, builder);
  }

  public void reset() {
    handler.reset();
    // XXX not sure if we should do this
    handler.setErrorHandler(null);
    handler.setResourceResolver(null);
  }

  public void validate(Source source, Result result) throws SAXException, IOException {
    if (!(source instanceof SAXSource))
      throw new IllegalArgumentException();
    SAXSource saxSource = (SAXSource)source;
    XMLReader xr = saxSource.getXMLReader();
    if (result != null) {
      // XXX handle results other than SAXResult
      if (!(result instanceof SAXResult))
        throw new IllegalArgumentException();
      handler.setContentHandler(((SAXResult)result).getHandler());
    }
    xr.setContentHandler(handler);
    xr.setDTDHandler(handler);
    xr.setErrorHandler(handler.getActualErrorHandler());
    xr.parse(saxSource.getInputSource());
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    handler.setErrorHandler(errorHandler);
  }

  public ErrorHandler getErrorHandler() {
    return handler.getErrorHandler();
  }

  public void setResourceResolver(LSResourceResolver resourceResolver) {
    handler.setResourceResolver(resourceResolver);
  }

  public LSResourceResolver getResourceResolver() {
    return handler.getResourceResolver();
  }
}
