package com.thaiopensource.relaxng.parse.sax;

import com.thaiopensource.relaxng.parse.Annotations;
import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.CommentList;
import com.thaiopensource.relaxng.parse.SubParseable;
import com.thaiopensource.relaxng.parse.SubParser;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;

public class SAXSubParser<P, NC, L, EA, CL extends CommentList<L>, A extends Annotations<L, EA, CL>> implements
        SubParser<P, NC, L, EA, CL, A> {
  final SAXResolver resolver;
  final ErrorHandler eh;

  SAXSubParser(SAXResolver resolver, ErrorHandler eh) {
    this.resolver = resolver;
    this.eh = eh;
  }

  public SubParseable<P, NC, L, EA, CL, A> createSubParseable(String href, String base) throws BuildException {
    try {
      return new SAXParseable<P, NC, L, EA, CL, A>(resolver.resolve(href, base, WellKnownNamespaces.RELAX_NG), resolver, eh);
    }
    catch (SAXException e) {
      throw BuildException.fromSAXException(e);
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
  }
}
