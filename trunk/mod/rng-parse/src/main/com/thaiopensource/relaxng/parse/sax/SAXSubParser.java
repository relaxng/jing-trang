package com.thaiopensource.relaxng.parse.sax;

import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.SubParseable;
import com.thaiopensource.relaxng.parse.SubParser;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;

public class SAXSubParser implements SubParser {
  final SAXResolver resolver;
  final ErrorHandler eh;

  SAXSubParser(SAXResolver resolver, ErrorHandler eh) {
    this.resolver = resolver;
    this.eh = eh;
  }

  public SubParseable createSubParseable(String href, String base) throws BuildException {
    try {
      return new SAXParseable(resolver.resolve(href, base, WellKnownNamespaces.RELAX_NG), resolver, eh);
    }
    catch (SAXException e) {
      throw BuildException.fromSAXException(e);
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
  }
}
