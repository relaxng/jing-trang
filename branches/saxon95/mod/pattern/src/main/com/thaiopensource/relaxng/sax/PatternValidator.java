package com.thaiopensource.relaxng.sax;

import com.thaiopensource.relaxng.match.Matcher;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.relaxng.pattern.PatternMatcher;
import com.thaiopensource.relaxng.pattern.ValidatorPatternBuilder;
import com.thaiopensource.xml.util.Name;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class PatternValidator extends Context implements ContentHandler, DTDHandler {
  private Matcher matcher;
  private final ErrorHandler eh;
  private boolean bufferingCharacters = false;
  private final StringBuilder charBuf = new StringBuilder();
  private Locator locator = null;

  public void startElement(String namespaceURI,
			   String localName,
			   String qName,
			   Attributes atts) throws SAXException {
    if (bufferingCharacters) {
      bufferingCharacters = false;
      check(matcher.matchTextBeforeStartTag(charBuf.toString(), this));
    }
    Name name = new Name(namespaceURI, localName);
    check(matcher.matchStartTagOpen(name, qName, this));
    int len = atts.getLength();
    for (int i = 0; i < len; i++) {
      Name attName = new Name(atts.getURI(i), atts.getLocalName(i));
      String attQName = atts.getQName(i);
      check(matcher.matchAttributeName(attName, attQName, this));
      check(matcher.matchAttributeValue(atts.getValue(i), attName, attQName, this));
    }
    check(matcher.matchStartTagClose(name, qName, this));
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
      if (charBuf.length() > 0)
        check(matcher.matchTextBeforeEndTag(charBuf.toString(), new Name(namespaceURI, localName),
                                            qName, this));
    }
    check(matcher.matchEndTag(new Name(namespaceURI, localName), qName, this));
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
	check(matcher.matchUntypedText(this));
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

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    if (bufferingCharacters) {
      bufferingCharacters = false;
      check(matcher.matchTextBeforeStartTag(charBuf.toString(), this));
    }
    super.startPrefixMapping(prefix, uri);
  }

  public PatternValidator(Pattern pattern, ValidatorPatternBuilder builder, ErrorHandler eh) {
    this.matcher = new PatternMatcher(pattern, builder);
    this.eh = eh;
  }

  public void reset() {
    super.reset();
    bufferingCharacters = false;
    locator = null;
    matcher = matcher.start();
  }

  private void check(boolean ok) throws SAXException {
    if (!ok)
      eh.error(new SAXParseException(matcher.getErrorMessage(), locator));
  }
}
