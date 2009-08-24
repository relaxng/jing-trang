package com.thaiopensource.relaxng.jaxp;

import com.thaiopensource.relaxng.match.Matcher;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.relaxng.pattern.PatternMatcher;
import com.thaiopensource.relaxng.pattern.ValidatorPatternBuilder;
import com.thaiopensource.relaxng.sax.Context;
import com.thaiopensource.validation.ValidatorHandler2;
import com.thaiopensource.xml.sax.DraconianErrorHandler;
import com.thaiopensource.xml.util.Name;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.validation.TypeInfoProvider;

class ValidatorHandlerImpl extends ValidatorHandler2 {
  private Matcher matcher;
  static private final ErrorHandler defaultErrorHandler = new DraconianErrorHandler();
  private ErrorHandler specifiedErrorHandler = null;
  private ErrorHandler actualErrorHandler = defaultErrorHandler;

  private boolean bufferingCharacters = false;
  private final StringBuffer charBuf = new StringBuffer();
  private Locator locator = null;
  private final Context context;
  private ContentHandler contentHandler = null;
  private DTDHandler dtdHandler;
  private LSResourceResolver resourceResolver = null;
  private boolean secureProcessing;

  ValidatorHandlerImpl(SchemaFactoryImpl factory, Pattern pattern, ValidatorPatternBuilder builder) {
    matcher = new PatternMatcher(pattern, builder);
    context = new Context();
    // the docs say it gets the properties of its factory, not the features
    secureProcessing = false;
  }

  public void reset() {
    bufferingCharacters = false;
    locator = null;
    matcher = matcher.start();
    context.reset();
  }

  public void startElement(String namespaceURI,
			   String localName,
			   String qName,
			   Attributes atts) throws SAXException {
    if (bufferingCharacters) {
      bufferingCharacters = false;
      check(matcher.matchTextBeforeStartTag(charBuf.toString(), context));
    }
    Name name = new Name(namespaceURI, localName);
    check(matcher.matchStartTagOpen(name, qName, context));
    int len = atts.getLength();
    for (int i = 0; i < len; i++) {
      Name attName = new Name(atts.getURI(i), atts.getLocalName(i));
      String attQName = atts.getQName(i);
      check(matcher.matchAttributeName(attName, attQName, context));
      check(matcher.matchAttributeValue(atts.getValue(i), attName, attQName, context));
    }
    check(matcher.matchStartTagClose(name, qName, context));
    if (matcher.isTextTyped()) {
      bufferingCharacters = true;
      charBuf.setLength(0);
    }
    if (contentHandler != null)
      contentHandler.startElement(namespaceURI, localName, qName, atts);
  }

  public void endElement(String namespaceURI,
			 String localName,
			 String qName) throws SAXException {
    if (bufferingCharacters) {
      bufferingCharacters = false;
      if (charBuf.length() > 0)
        check(matcher.matchTextBeforeEndTag(charBuf.toString(), new Name(namespaceURI, localName),
                                            qName, context));
    }
    check(matcher.matchEndTag(new Name(namespaceURI, localName), qName, context));
    if (contentHandler != null)
      contentHandler.endElement(namespaceURI, localName, qName);
  }

  public void characters(char ch[], int start, int length) throws SAXException {
    if (bufferingCharacters) {
      charBuf.append(ch, start, length);
      return;
    }
    for (int i = 0; i < length; i++) {
      switch (ch[start + i]) {
      case ' ':
      case '\r':
      case '\t':
      case '\n':
	break;
      default:
	check(matcher.matchUntypedText(context));
	return;
      }
    }
  }

  public void endDocument() throws SAXException {
    check(matcher.matchEndDocument());
    if (contentHandler != null)
      contentHandler.endDocument();
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
    if (contentHandler != null)
      contentHandler.setDocumentLocator(locator);
  }

  public void startDocument() throws SAXException {
    check(matcher.matchStartDocument());
    if (contentHandler != null)
      contentHandler.startDocument();
  }

  public void processingInstruction(String target, String data) throws SAXException {
    if (contentHandler != null)
      contentHandler.processingInstruction(target, data);
  }

  public void skippedEntity(String name) throws SAXException {
    if (contentHandler != null)
      contentHandler.skippedEntity(name);
  }

  public void ignorableWhitespace(char[] ch, int start, int len) throws SAXException {
    if (contentHandler != null)
      contentHandler.ignorableWhitespace(ch, start, len);
  }

  private void check(boolean ok) throws SAXException {
    if (!ok)
      actualErrorHandler.error(new SAXParseException(matcher.getErrorMessage(), locator));
  }

  public void setContentHandler(ContentHandler delegate) {
    this.contentHandler = delegate;
  }

  public ContentHandler getContentHandler() {
    return contentHandler;
  }

  public void setDTDHandler(DTDHandler dtdHandler) {
    this.dtdHandler = dtdHandler;
  }

  public DTDHandler getDTDHandler() {
    return dtdHandler;
  }

  public TypeInfoProvider getTypeInfoProvider() {
    return null;
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    this.specifiedErrorHandler = errorHandler;
    this.actualErrorHandler = errorHandler == null ? defaultErrorHandler : errorHandler;
  }

  public ErrorHandler getErrorHandler() {
    return specifiedErrorHandler;
  }

  public void setResourceResolver(LSResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
  }

  public LSResourceResolver getResourceResolver() {
    return resourceResolver;
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    // namespace declarations on the start-tag shouldn't apply to the characters before the start-tag
    if (bufferingCharacters) {
      bufferingCharacters = false;
      check(matcher.matchTextBeforeStartTag(charBuf.toString(), context));
    }
    context.startPrefixMapping(prefix, uri);
    if (contentHandler != null)
      contentHandler.startPrefixMapping(prefix, uri);
  }

  public void endPrefixMapping(String prefix) throws SAXException {
    context.endPrefixMapping(prefix);
    if (contentHandler != null)
      contentHandler.endPrefixMapping(prefix);
  }

  public void notationDecl(String name, String publicId, String systemId) throws SAXException {
    context.notationDecl(name, publicId, systemId);
    if (dtdHandler != null)
      dtdHandler.notationDecl(name, publicId, systemId);
  }

  public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
    context.unparsedEntityDecl(name, publicId, systemId, notationName);
    if (dtdHandler != null)
      dtdHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
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
}
