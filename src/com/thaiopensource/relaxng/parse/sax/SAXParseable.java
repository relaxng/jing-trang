package com.thaiopensource.relaxng.parse.sax;

import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.ParsedPattern;
import com.thaiopensource.relaxng.parse.SchemaBuilder;
import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.IncludedGrammar;
import com.thaiopensource.relaxng.parse.Scope;
import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.datatype.xsd.NCNameDatatype;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;
import org.relaxng.datatype.Datatype;

import java.io.IOException;

public class SAXParseable implements Parseable {
  private XMLReaderCreator xrc;
  private InputSource in;
  private ErrorHandler eh;
  private Datatype ncNameDatatype = new NCNameDatatype();

  public SAXParseable(XMLReaderCreator xrc, InputSource in, ErrorHandler eh) {
    this.xrc = xrc;
    this.in = in;
    this.eh = eh;
  }

  public ParsedPattern parse(SchemaBuilder schemaBuilder) throws BuildException, IllegalSchemaException {
    try {
      XMLReader xr = xrc.createXMLReader();
      SchemaParser sp = new SchemaParser(xr, eh, schemaBuilder, ncNameDatatype, null, null);
      xr.parse(in);
      return sp.getStartPattern();
    }
    catch (SAXException e) {
      throw new BuildException(e);
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
  }

  public void parseInclude(String uri, SchemaBuilder schemaBuilder, IncludedGrammar g)
          throws BuildException, IllegalSchemaException {
    try {
      XMLReader xr = xrc.createXMLReader();
      SchemaParser sp = new SchemaParser(xr, eh, schemaBuilder, ncNameDatatype, g, g);
      xr.parse(makeInputSource(xr, uri));
      sp.getStartPattern();
    }
    catch (SAXException e) {
     throw new BuildException(e);
    }
    catch (IOException e) {
     throw new BuildException(e);
    }
  }

  public ParsedPattern parseExternal(String uri, SchemaBuilder schemaBuilder, Scope s)
          throws BuildException, IllegalSchemaException {
    try {
      XMLReader xr = xrc.createXMLReader();
      SchemaParser sp = new SchemaParser(xr, eh, schemaBuilder, ncNameDatatype, null, s);
      xr.parse(makeInputSource(xr, uri));
      return sp.getStartPattern();
    }
    catch (SAXException e) {
      throw new BuildException(e);
    }
    catch (IOException e) {
      throw new BuildException(e);
    }
  }

  private InputSource makeInputSource(XMLReader xr, String systemId) throws IOException, SAXException {
    EntityResolver er = xr.getEntityResolver();
    if (er != null) {
      InputSource inputSource = er.resolveEntity(null, systemId);
      if (inputSource != null)
	return inputSource;
    }
    return new InputSource(systemId);
  }
}
