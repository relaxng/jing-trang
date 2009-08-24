package com.thaiopensource.validate.schematron;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class IfValidHandler implements ContentHandler, ErrorHandler {
  private ContentHandler validator;
  private ContentHandler delegate;
  private ErrorHandler errorHandler;
  private boolean valid = true;

  public ContentHandler getValidator() {
    return validator;
  }

  public void setValidator(ContentHandler validator) {
    this.validator = validator;
  }

  public ContentHandler getDelegate() {
    return delegate;
  }

  public void setDelegate(ContentHandler delegate) {
    this.delegate = delegate;
  }

  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
  }

  public void setDocumentLocator(Locator locator) {
    validator.setDocumentLocator(locator);
    if (valid)
      delegate.setDocumentLocator(locator);
  }

  public void startDocument() throws SAXException {
    validator.startDocument();
    if (valid)
      delegate.startDocument();
  }

  public void endDocument() throws SAXException {
    validator.endDocument();
    if (valid)
      delegate.endDocument();
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    validator.startPrefixMapping(prefix, uri);
    if (valid)
      delegate.startPrefixMapping(prefix, uri);
  }

  public void endPrefixMapping(String prefix) throws SAXException {
    validator.endPrefixMapping(prefix);
    if (valid)
      delegate.endPrefixMapping(prefix);
  }

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    validator.startElement(uri, localName, qName, attributes);
    if (valid)
      delegate.startElement(uri, localName, qName, attributes);
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    validator.endElement(uri, localName, qName);
    if (valid)
      delegate.endElement(uri, localName, qName);
  }

  public void characters(char[] chars, int start, int length) throws SAXException {
    validator.characters(chars, start, length);
    if (valid)
      delegate.characters(chars, start, length);
  }

  public void ignorableWhitespace(char[] chars, int start, int length) throws SAXException {
    validator.ignorableWhitespace(chars, start, length);
    if (valid)
      delegate.ignorableWhitespace(chars, start, length);
  }

  public void processingInstruction(String target, String data) throws SAXException {
    validator.processingInstruction(target, data);
    if (valid)
      delegate.processingInstruction(target, data);
  }

  public void skippedEntity(String name) throws SAXException {
    validator.skippedEntity(name);
    if (valid)
      delegate.skippedEntity(name);
  }

  public void warning(SAXParseException exception) throws SAXException {
    errorHandler.warning(exception);
  }

  public void error(SAXParseException exception) throws SAXException {
    valid = false;
    errorHandler.error(exception);
  }

  public void fatalError(SAXParseException exception) throws SAXException {
    valid = false;
    errorHandler.fatalError(exception);
  }
}