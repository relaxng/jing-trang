package com.thaiopensource.validation;

import org.xml.sax.SAXException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.InputSource;
import org.w3c.dom.ls.LSResourceResolver;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.XMLConstants;
import java.io.File;
import java.net.URL;

/**
 * Extends the SchemaFactory abstract class.  All methods of SchemaFactory
 * that return a Schema are overridden to return a Schema2.
 */
public abstract class SchemaFactory2 extends SchemaFactory {
  // Corresponds to XMLConstants.FEATURE_SECURE_PROCESSING.
  private boolean secureProcessing = false;
  private ErrorHandler errorHandler = null;
  private LSResourceResolver resourceResolver = null;

  public abstract Schema2 newSchema(SAXSource schema) throws SAXException;

  public Schema2 newSchema(Source[] schemas) throws SAXException {
    if (schemas.length != 1)
      throw new UnsupportedOperationException();
    return newSchema(schemas[0]);
  }

  public Schema2 newSchema() throws SAXException {
    throw new UnsupportedOperationException();
  }

  public Schema2 newSchema(Source source) throws SAXException {
    if (source == null)
      throw new NullPointerException();
    if (source instanceof SAXSource)
      return newSchema((SAXSource)source);
    InputSource inputSource = SAXSource.sourceToInputSource(source);
    // XXX support other types of Source for the schema
    if (inputSource == null)
      throw new IllegalArgumentException("unsupported type of Source for schema");
    return newSchema(new SAXSource(inputSource));
  }

  public Schema2 newSchema(File schema) throws SAXException {
    return newSchema(new StreamSource(schema));
  }

  public Schema2 newSchema(URL schema) throws SAXException {
    return newSchema(new StreamSource(schema.toExternalForm()));
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
  }

  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public void setResourceResolver(LSResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
  }

  public LSResourceResolver getResourceResolver() {
    return resourceResolver;
  }

  public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
    if (XMLConstants.FEATURE_SECURE_PROCESSING.equals(name))
      secureProcessing = value;
    else
      super.setFeature(name, value);
  }

  public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
    if (XMLConstants.FEATURE_SECURE_PROCESSING.equals(name))
      return secureProcessing;
    return super.getFeature(name);
  }

  public void setSecureProcessing(boolean secureProcessing) {
    this.secureProcessing = secureProcessing;
  }
  
  public boolean getSecureProcessing() {
    return secureProcessing;
  }
}
