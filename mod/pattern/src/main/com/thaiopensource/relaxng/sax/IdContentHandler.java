package com.thaiopensource.relaxng.sax;

import com.thaiopensource.relaxng.pattern.IdSoundnessChecker;
import com.thaiopensource.relaxng.pattern.IdTypeMap;
import com.thaiopensource.xml.util.Name;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class IdContentHandler implements ContentHandler {
  private final IdSoundnessChecker checker;
  private Locator locator;

  public IdContentHandler(IdTypeMap idTypeMap, ErrorHandler eh) {
    this.checker = new IdSoundnessChecker(idTypeMap, eh);
  }

  public void reset() {
    checker.reset();
    locator = null;
  }

  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  public void startDocument() throws SAXException {
  }

  public void endDocument() throws SAXException {
    checker.endDocument();
    setComplete();
  }

  protected void setComplete() {
    /// XXX what's the point of this?
  }

  public void startPrefixMapping(String s, String s1) throws SAXException {
  }

  public void endPrefixMapping(String s) throws SAXException {
  }

  public void startElement(String namespaceUri, String localName, String qName, Attributes attributes)
          throws SAXException {
    Name elementName = new Name(namespaceUri, localName);
    int len = attributes.getLength();
    for (int i = 0; i < len; i++) {
      Name attributeName = new Name(attributes.getURI(i), attributes.getLocalName(i));
      String value = attributes.getValue(i);
      checker.attribute(elementName, attributeName, value, locator);
    }
  }

  public void endElement(String s, String s1, String s2) throws SAXException {
  }

  public void characters(char[] chars, int i, int i1) throws SAXException {
  }

  public void ignorableWhitespace(char[] chars, int i, int i1) throws SAXException {
  }

  public void processingInstruction(String s, String s1) throws SAXException {
  }

  public void skippedEntity(String s) throws SAXException {
  }

  public void notationDecl(String name,
                           String publicId,
                           String systemId)
          throws SAXException {
  }

  public void unparsedEntityDecl(String name,
                                 String publicId,
                                 String systemId,
                                 String notationName)
          throws SAXException {
  }
}
