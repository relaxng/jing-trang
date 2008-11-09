package com.thaiopensource.relaxng.jaxp;

import com.thaiopensource.relaxng.impl.Pattern;
import com.thaiopensource.relaxng.impl.PatternMatcher;
import com.thaiopensource.relaxng.impl.ValidatorPatternBuilder;
import com.thaiopensource.relaxng.match.Matcher;
import com.thaiopensource.relaxng.sax.Context;
import com.thaiopensource.xml.sax.DraconianErrorHandler;
import com.thaiopensource.xml.util.Name;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.validation.TypeInfoProvider;
import javax.xml.validation.ValidatorHandler;

class ValidatorHandlerImpl extends ValidatorHandler implements DTDHandler {
  private final Matcher initialMatcher;
  private Matcher matcher;
  static private final ErrorHandler defaultErrorHandler = new DraconianErrorHandler();
  private ErrorHandler specifiedErrorHandler = null;
  private ErrorHandler actualErrorHandler = defaultErrorHandler;

  private boolean bufferingCharacters = false;
  private final StringBuffer charBuf = new StringBuffer();
  private Locator locator = null;
  private final Context context;
  private ContentHandler delegate = null;
  private LSResourceResolver resourceResolver = null;

  ValidatorHandlerImpl(Pattern pattern, ValidatorPatternBuilder builder) {
    initialMatcher = new PatternMatcher(pattern, builder);
    matcher = initialMatcher.copy();
    context = new Context();
  }

  public void reset() {
    bufferingCharacters = false;
    locator = null;
    matcher = initialMatcher.copy();
    context.reset();
  }

  public void startElement(String namespaceURI,
			   String localName,
			   String qName,
			   Attributes atts) throws SAXException {
    if (bufferingCharacters) {
      bufferingCharacters = false;
      check(matcher.matchTextBeforeStartTag(charBuf.toString()));
    }
    Name name = new Name(namespaceURI, localName);
    check(matcher.matchStartTagOpen(name));
    int len = atts.getLength();
    for (int i = 0; i < len; i++) {
      Name attName = new Name(atts.getURI(i), atts.getLocalName(i));
      check(matcher.matchAttributeName(attName));
      check(matcher.matchAttributeValue(attName, atts.getValue(i), context));
    }
    check(matcher.matchStartTagClose());
    if (matcher.isTextTyped()) {
      bufferingCharacters = true;
      charBuf.setLength(0);
    }
    if (delegate != null)
      delegate.startElement(namespaceURI, localName, qName, atts);
  }

  public void endElement(String namespaceURI,
			 String localName,
			 String qName) throws SAXException {
    if (bufferingCharacters) {
      bufferingCharacters = false;
      check(matcher.matchTextBeforeEndTag(charBuf.toString(), context));
    }
    check(matcher.matchEndTag(context));
    if (delegate != null)
      delegate.endElement(namespaceURI, localName, qName);
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
	check(matcher.matchUntypedText());
	return;
      }
    }
  }

  public void endDocument() throws SAXException {
    check(matcher.matchEndDocument());
    if (delegate != null)
      delegate.endDocument();
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
    if (delegate != null)
      delegate.setDocumentLocator(locator);
  }

  public void startDocument() throws SAXException {
    check(matcher.matchStartDocument());
    if (delegate != null)
      delegate.startDocument();
  }

  public void processingInstruction(String target, String data) throws SAXException {
    if (delegate != null)
      delegate.processingInstruction(target, data);
  }

  public void skippedEntity(String name) throws SAXException {
    if (delegate != null)
      delegate.skippedEntity(name);
  }

  public void ignorableWhitespace(char[] ch, int start, int len) throws SAXException {
    if (delegate != null)
      delegate.ignorableWhitespace(ch, start, len);
  }

  private void check(boolean ok) throws SAXException {
    if (!ok)
      actualErrorHandler.error(new SAXParseException(matcher.getErrorMessage(), locator));
  }

  public void setContentHandler(ContentHandler delegate) {
    this.delegate = delegate;
  }

  public ContentHandler getContentHandler() {
    return delegate;
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

  ErrorHandler getActualErrorHandler() {
    return actualErrorHandler;
  }
  public void setResourceResolver(LSResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
  }

  public LSResourceResolver getResourceResolver() {
    return resourceResolver;
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    context.startPrefixMapping(prefix, uri);
    if (delegate != null)
      delegate.startPrefixMapping(prefix, uri);
  }

  public void endPrefixMapping(String prefix) throws SAXException {
    context.endPrefixMapping(prefix);
    if (delegate != null)
      delegate.endPrefixMapping(prefix);
  }

  public void notationDecl(String name, String publicId, String systemId) throws SAXException {
    context.notationDecl(name, publicId, systemId);
  }

  public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
    context.unparsedEntityDecl(name, publicId, systemId, notationName);
  }
}
