package com.thaiopensource.relaxng;

import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.XMLReader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import org.relaxng.datatype.ValidationContext;

public class Validator implements ContentHandler {
  private final PatternBuilder b;
  private Locator locator;
  private final XMLReader xr;
  private PatternMemo memo;
  private boolean hadError = false;
  private boolean collectingCharacters = false;
  private StringBuffer charBuf = new StringBuffer();
  private PrefixMapping prefixMapping = new PrefixMapping("xml", PatternReader.xmlURI, null);

  static private final class PrefixMapping implements ValidationContext {
    private final String prefix;
    private final String namespaceURI;
    private final PrefixMapping prev;

    PrefixMapping(String prefix, String namespaceURI, PrefixMapping prev) {
      this.prefix = prefix;
      this.namespaceURI = namespaceURI;
      this.prev = prev;
    }

    PrefixMapping getPrevious() {
      return prev;
    }

    public String resolveNamespacePrefix(String prefix) {
      PrefixMapping tem = this;
      do {
	if (tem.prefix.equals(prefix))
	  return tem.namespaceURI;
	tem = tem.prev;
      } while (tem != null);
      return null;
    }

    public String getBaseUri() {
      return null;
    }

    public boolean isUnparsedEntity(String name) {
      return false;
    }

    public boolean isNotation(String name) {
      return false;
    }

  }

  private void startCollectingCharacters() {
    if (!collectingCharacters) {
      collectingCharacters = true;
      charBuf.setLength(0);
    }
  }

  private void flushCharacters() throws SAXException {
    collectingCharacters = false;
    int len = charBuf.length();
    for (int i = 0; i < len; i++) {
      switch (charBuf.charAt(i)) {
      case ' ':
      case '\r':
      case '\t':
      case '\n':
	break;
      default:
	text();
	return;
      }
    }
  }

  public void startElement(String namespaceURI,
			   String localName,
			   String qName,
			   Attributes atts) throws SAXException {
    if (collectingCharacters)
      flushCharacters();

    Name name = new Name(namespaceURI, localName);
    if (!setMemo(memo.startTagOpenDeriv(name))) {
      error("impossible_element", localName);
      // XXX recover better
      memo = b.getPatternMemo(b.makeAfter(b.makeNotAllowed(), memo.getPattern()));
    }
    int len = atts.getLength();
    for (int i = 0; i < len; i++) {
      Name attName = new Name(atts.getURI(i), atts.getLocalName(i));

      if (!setMemo(memo.startAttributeDeriv(attName)))
	error("impossible_attribute_ignored", atts.getLocalName(i));
      else if (!setMemo(memo.dataDeriv(atts.getValue(i), prefixMapping))) {
	error("bad_attribute_value", atts.getLocalName(i));
	memo = memo.recoverAfter();
      }
    }
    if (!setMemo(memo.endAttributes())) {
      // XXX should specify which attributes
      error("required_attributes_missing");
      memo = memo.ignoreMissingAttributes();
    }
    if (memo.getPattern().getContentType() == Pattern.DATA_CONTENT_TYPE)
      startCollectingCharacters();
  }

  public void endElement(String namespaceURI,
			 String localName,
			 String qName) throws SAXException {
    if (collectingCharacters) {
      collectingCharacters = false;
      if (!setMemo(memo.textOnly())) {
	error("only_text_not_allowed");
	memo = memo.recoverAfter();
	return;
      }
      if (!setMemo(memo.dataDeriv(charBuf.toString(), prefixMapping))) {
	error("string_not_allowed");
	memo = memo.recoverAfter();
      }
    }
    else if (!setMemo(memo.endTagDeriv())) {
      error("unfinished_element");
      memo = memo.recoverAfter();
    }
  }

  public void characters(char ch[], int start, int length) throws SAXException {
    if (collectingCharacters) {
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
	text();
	return;
      }
    }
  }

  private void text() throws SAXException {
    if (!setMemo(memo.mixedTextDeriv()))
      error("text_not_allowed");
  }

  public void endDocument() throws SAXException {
    // XXX
  }

  public void setDocumentLocator(Locator loc) {
    locator = loc;
  }

  public void startDocument() { }
  public void processingInstruction(String target, String date) { }
  public void skippedEntity(String name) { }
  public void ignorableWhitespace(char[] ch, int start, int len) { }
  public void startPrefixMapping(String prefix, String uri) {
    prefixMapping = new PrefixMapping(prefix, uri, prefixMapping);
  }
  public void endPrefixMapping(String prefix) {
    prefixMapping = prefixMapping.getPrevious();
  }

  public Validator(Pattern pattern, PatternBuilder b, XMLReader xr) {
    this.b = b;
    this.xr = xr;
    this.memo = b.getPatternMemo(pattern);
    xr.setContentHandler(this);
  }

  public boolean getValid() {
    return !hadError;
  }

  private void error(String key) throws SAXException {
    hadError = true;
    ErrorHandler eh = xr.getErrorHandler();
    if (eh != null)
      eh.error(new SAXParseException(Localizer.message(key), locator));
  }

  private void error(String key, String arg) throws SAXException {
    hadError = true;
    ErrorHandler eh = xr.getErrorHandler();
    if (eh != null)
      eh.error(new SAXParseException(Localizer.message(key, arg), locator));
  }

  private void error(String key, String arg1, String arg2, Locator loc) throws SAXException {
    hadError = true;
    ErrorHandler eh = xr.getErrorHandler();
    if (eh != null)
      eh.error(new SAXParseException(Localizer.message(key, arg1, arg2),
				     loc));
  }

  private void error(String key, String arg1, String arg2) throws SAXException {
    hadError = true;
    ErrorHandler eh = xr.getErrorHandler();
    if (eh != null)
      eh.error(new SAXParseException(Localizer.message(key, arg1, arg2),
				     locator));
  }

  /* Return false is something went wrong. */
  private boolean setMemo(PatternMemo m) {
    if (m.isNotAllowed())
      return memo.isNotAllowed();
    else {
      memo = m;
      return true;
    }
  }
}
