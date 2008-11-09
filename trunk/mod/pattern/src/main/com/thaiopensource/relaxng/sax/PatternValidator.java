package com.thaiopensource.relaxng.sax;

import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.relaxng.pattern.PatternMatcher;
import com.thaiopensource.relaxng.pattern.ValidatorPatternBuilder;
import com.thaiopensource.relaxng.match.Matcher;
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
  private final Matcher initialMatcher;
  private final ErrorHandler eh;
  private boolean bufferingCharacters;
  private final StringBuffer charBuf = new StringBuffer();
  private Locator locator;

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

  public PatternValidator(Pattern pattern, ValidatorPatternBuilder builder, ErrorHandler eh) {
    this.initialMatcher = new PatternMatcher(pattern, builder);
    this.matcher = initialMatcher.copy();
    this.eh = eh;
    reset();
  }

  public void reset() {
    super.reset();
    bufferingCharacters = false;
    locator = null;
    matcher = initialMatcher.copy();

  }

  private void check(boolean ok) throws SAXException {
    if (!ok)
      eh.error(new SAXParseException(matcher.getErrorMessage(), locator));
  }
}
