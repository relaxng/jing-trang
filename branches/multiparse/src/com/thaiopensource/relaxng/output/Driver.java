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
import com.thaiopensource.relaxng.util.ErrorHandlerImpl;
import com.thaiopensource.util.Localizer;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;

public class Driver {
  static private Localizer localizer = new Localizer(Driver.class);
  static public void main(String[] args) throws IncorrectSchemaException, SAXException, IOException {
    System.exit(doMain(args));
  }

  static public int doMain(String[] args) throws IncorrectSchemaException, SAXException, IOException {
    ErrorHandlerImpl eh = new ErrorHandlerImpl();
    try {
      InputSource in = ValidationEngine.fileInputSource(args[0]);
      Parseable parseable;
      String ext = extension(args[0]);
      if (ext.equalsIgnoreCase(".rng"))
        parseable = new SAXParseable(new Jaxp11XMLReaderCreator(), in, eh);
      else if (ext.equalsIgnoreCase(".rngnx") || ext.equalsIgnoreCase(".rnx"))
        parseable = new NonXmlParseable(in, eh);
      else {
        eh.printException(new SAXException(localizer.message("unrecognized_input_extension", ext)));
        return 2;
      }
      OutputFormat of;
      ext = extension(args[1]);
      if (ext.equalsIgnoreCase(".dtd"))
        of = new DtdOutputFormat();
      else if (ext.equalsIgnoreCase(".rng"))
        of = new RngOutputFormat();
      else {
        eh.printException(new SAXException(localizer.message("unrecognized_output_extension", ext)));
        return 2;
      }
      SchemaCollection sc = SchemaBuilderImpl.parse(parseable,
                                                    new DatatypeLibraryLoader());
      OutputDirectory od = new LocalOutputDirectory(new File(args[1]));
      of.output(sc, od, eh);
      return 0;
    }
    catch (IncorrectSchemaException e) {
    }
    catch (IOException e) {
      eh.printException(e);
    }
    catch (SAXException e) {
      eh.printException(e);
    }
    return 1;
  }

  static private String extension(String s) {
    int dot = s.lastIndexOf(".");
    if (dot < 0)
      return "";
    return s.substring(dot);
  }
}
