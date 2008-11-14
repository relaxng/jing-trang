package com.thaiopensource.resolver.xml.sax;

import com.thaiopensource.resolver.Input;
import org.xml.sax.XMLReader;

/**
 *
 */
public class SAXInput extends Input {
  private XMLReader reader;

  public XMLReader getXMLReader() {
    return reader;
  }

  public void setXMLReader(XMLReader reader) {
    this.reader = reader;
  }
}
