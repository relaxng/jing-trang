package com.thaiopensource.relaxng.parse;

import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

public interface ParseReceiver<P, NC, L, EA, CL extends CommentList<L>, A extends Annotations<L, EA, CL>>
        extends SubParser<P, NC, L, EA, CL, A> {
  ParsedPatternFuture<P> installHandlers(XMLReader xr,
                                         SchemaBuilder<P, NC, L, EA, CL, A> schemaBuilder,
                                         Scope<P, L, EA, CL, A> scope)
          throws SAXException;
}
