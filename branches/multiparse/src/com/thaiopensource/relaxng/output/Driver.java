package com.thaiopensource.relaxng.output;

import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.edit.SchemaBuilderImpl;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.output.dtd.DtdOutputFormat;
import com.thaiopensource.relaxng.output.rng.RngOutputFormat;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.nonxml.NonXmlParseable;
import com.thaiopensource.relaxng.util.DraconianErrorHandler;
import com.thaiopensource.relaxng.util.Jaxp11XMLReaderCreator;
import com.thaiopensource.relaxng.util.ValidationEngine;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;

public class Driver {
  static public void main(String[] args) throws IncorrectSchemaException, SAXException, IOException {
    System.exit(doMain(args));
  }

  static public int doMain(String[] args) throws IncorrectSchemaException, SAXException, IOException {
    ErrorHandler eh = new DraconianErrorHandler();

    InputSource in = ValidationEngine.fileInputSource(args[0]);
    Parseable parseable;
    if (args[0].endsWith(".rng"))
      parseable = new SAXParseable(new Jaxp11XMLReaderCreator(), in, eh);
    else if (args[0].endsWith(".rngnx"))
      parseable = new NonXmlParseable(in, eh);
    else {
      System.err.println("unrecognized input file extension");
      return 2;
    }
    OutputFormat of;
    if (args[1].endsWith(".dtd"))
      of = new DtdOutputFormat();
    else if (args[1].endsWith(".rng"))
      of = new RngOutputFormat();
    else {
      System.err.println("unrecognized output file extension");
      return 2;
    }
    SchemaCollection sc = SchemaBuilderImpl.parse(parseable,
                                                  new DatatypeLibraryLoader());
    OutputDirectory od = new LocalOutputDirectory(new File(args[1]));
    of.output(sc, od, eh);
    return 0;
  }
}
