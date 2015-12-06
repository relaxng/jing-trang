package com.thaiopensource.relaxng.parse.sax;

import com.thaiopensource.relaxng.parse.ParseReceiver;
import com.thaiopensource.relaxng.parse.ParsedPatternFuture;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.Scope;
import com.thaiopensource.relaxng.parse.CommentList;
import com.thaiopensource.relaxng.parse.Annotations;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class SAXParseReceiver<P, NC, L, EA, CL extends CommentList<L>, A extends Annotations<L, EA, CL>>
        extends SAXSubParser<P, NC, L, EA, CL, A>
        implements ParseReceiver<P, NC, L, EA, CL, A> {
  public SAXParseReceiver(SAXResolver resolver, ErrorHandler eh) {
    super(resolver, eh);
  }

  public ParsedPatternFuture<P> installHandlers(XMLReader xr, SchemaBuilder<P, NC, L, EA, CL, A> schemaBuilder,
                                                Scope<P, L, EA, CL, A> scope)
          throws SAXException {
    return new SchemaParser<P, NC, L, EA, CL, A>(xr, eh, schemaBuilder, null, scope);
  }
}
