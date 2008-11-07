package com.thaiopensource.relaxng.impl;

import com.thaiopensource.relaxng.match.Matcher;
import com.thaiopensource.relaxng.parse.sax.DtdContext;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.xml.util.Name;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class PatternValidator extends DtdContext implements Validator, ContentHandler, DTDHandler {
  private Matcher matcher;
  private final Matcher initialMatcher;
  private final ErrorHandler eh;
  private boolean bufferingCharacters;
  private final StringBuffer charBuf = new StringBuffer();
  private PrefixMapping prefixMapping = new PrefixMapping("xml", WellKnownNamespaces.XML, null);
  private Locator locator;

  private static final class PrefixMapping {
    private final String prefix;
    private final String namespaceURI;
    private final PrefixMapping previous;

    PrefixMapping(String prefix, String namespaceURI, PrefixMapping prev) {
      this.prefix = prefix;
      this.namespaceURI = namespaceURI;
      this.previous = prev;
    }

    PrefixMapping getPrevious() {
      return previous;
    }
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
      check(matcher.matchAttributeValue(attName, atts.getValue(i), this));
    }
    check(matcher.matchStartTagClose());
    if (matcher.isTextTyped()) {
      bufferingCharacters = true;
      charBuf.setLength(0);
    }
  }

  public void endElement(String namespaceURI,
			 String localName,
			 String qName) throws SAXException {
    if (bufferingCharacters) {
      bufferingCharacters = false;
      check(matcher.matchTextBeforeEndTag(charBuf.toString(), this));
    }
    check(matcher.matchEndTag(this));
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
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  public void startDocument() throws SAXException {
    check(matcher.matchStartDocument());
  }

  public void processingInstruction(String target, String date) { }
  public void skippedEntity(String name) { }
  public void ignorableWhitespace(char[] ch, int start, int len) { }
  public void startPrefixMapping(String prefix, String uri) {
    prefixMapping = new PrefixMapping(prefix, uri, prefixMapping);
  }
  public void endPrefixMapping(String prefix) {
    prefixMapping = prefixMapping.getPrevious();
  }

  public PatternValidator(Pattern pattern, ValidatorPatternBuilder builder, ErrorHandler eh) {
    this.initialMatcher = new PatternMatcher(pattern, builder);
    this.matcher = initialMatcher.copy();
    this.eh = eh;
    reset();
  }

  public void reset() {
    bufferingCharacters = false;
    locator = null;
    matcher = initialMatcher.copy();
    prefixMapping = new PrefixMapping("xml", WellKnownNamespaces.XML, null);
    clearDtdContext();
  }

  public ContentHandler getContentHandler() {
    return this;
  }

  public DTDHandler getDTDHandler() {
    return this;
  }

  private void check(boolean ok) throws SAXException {
     if (!ok)
       eh.error(new SAXParseException(matcher.getErrorMessage(), locator));
  }

  public String resolveNamespacePrefix(String prefix) {
    PrefixMapping tem = prefixMapping;
    do {
      if (tem.prefix.equals(prefix))
        return tem.namespaceURI;
      tem = tem.previous;
    } while (tem != null);
    return null;
  }

  public String getBaseUri() {
    return null;
  }
}
