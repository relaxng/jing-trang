package com.thaiopensource.datatype.xsd.regex;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.util.Enumeration;

import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.util.Jaxp11XMLReaderCreator;
import com.thaiopensource.util.UriOrFile;
import com.thaiopensource.util.Service;
import com.thaiopensource.datatype.xsd.Regex;
import com.thaiopensource.datatype.xsd.RegexEngine;
import com.thaiopensource.datatype.xsd.InvalidRegexException;

public class TestDriver extends DefaultHandler {
  private final StringBuffer buf = new StringBuffer();
  private Regex regex;
  private int nFail = 0;
  private int nTests = 0;
  private Locator loc;
  private RegexEngine engine;

  static public void main(String[] args) throws SAXException, IOException {
    if (args.length != 2) {
      System.err.println("usage: TestDriver class testfile");
      System.exit(2);
    }
    XMLReaderCreator xrc = new Jaxp11XMLReaderCreator();
    XMLReader xr = xrc.createXMLReader();

    Enumeration e = new Service(RegexEngine.class).getProviders();
    RegexEngine engine;
    for (;;) {
      if (!e.hasMoreElements()) {
        System.err.println("couldn't find regex engine");
        System.exit(2);
      }
      engine = (RegexEngine)e.nextElement();
      if (engine.getClass().getName().equals(args[0]))
        break;
    }
    TestDriver tester = new TestDriver(engine);
    xr.setContentHandler(tester);
    InputSource in = new InputSource(UriOrFile.fileToUri(args[1]));
    xr.parse(in);
    System.err.println(tester.nTests + " tests performed");
    System.err.println(tester.nFail + " failures");
    if (tester.nFail > 0)
      System.exit(1);
  }

  public TestDriver(RegexEngine engine) {
    this.engine = engine;
  }

  public void setDocumentLocator(Locator locator) {
    this.loc = locator;
  }

  public void characters(char ch[], int start, int length)
          throws SAXException {
    buf.append(ch, start, length);
  }

  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException {
    buf.append(ch, start, length);
  }

  public void startElement(String uri, String localName,
                           String qName, Attributes attributes)
          throws SAXException {
    buf.setLength(0);
  }

  public void endElement(String uri, String localName, String qName)
          throws SAXException {
    if (localName.equals("valid"))
      valid(buf.toString());
    else if (localName.equals("invalid"))
      invalid(buf.toString());
    else if (localName.equals("correct"))
      correct(buf.toString());
    else if (localName.equals("incorrect"))
      incorrect(buf.toString());
  }

  private void correct(String str) {
    nTests++;
    regex = null;
    try {
      regex = engine.compile(str);
    }
    catch (InvalidRegexException e) {
      error("unexpected error: " + e.getMessage() + ": " + display(str));
    }
  }

  private void incorrect(String str) {
    nTests++;
    regex = null;
    try {
      engine.compile(str);
      error("failed to detect error in regex: " + display(str));
    }
    catch (InvalidRegexException e) { }
  }

  private void valid(String str) {
    if (regex == null)
      return;
    nTests++;
    if (!regex.matches(str))
      error("match failed for string: " + display(str));
  }

  private void invalid(String str) {
    if (regex == null)
      return;
    nTests++;
    if (regex.matches(str))
      error("match incorrectly succeeded for string: " + display(str));
  }

  private void error(String str) {
    int line = -1;
    if (loc != null)
      line = loc.getLineNumber();
    if (line >= 0)
      System.err.print("Line " + line + ": ");
    System.err.println(str);
    nFail++;
  }

  static String display(String str) {
    return str;
  }

}
