package com.thaiopensource.relaxng.output;

import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.SchemaBuilderImpl;
import com.thaiopensource.relaxng.util.Jaxp11XMLReaderCreator;
import com.thaiopensource.relaxng.util.DraconianErrorHandler;
import com.thaiopensource.relaxng.IncorrectSchemaException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;

import java.io.IOException;

public class Driver {
  static public void main(String[] args) throws IncorrectSchemaException, SAXException, IOException {
    SchemaCollection sc = new SchemaCollection();
    Pattern p = SchemaBuilderImpl.parse(new SAXParseable(new Jaxp11XMLReaderCreator(),
                                                         new InputSource(args[0]),
                                                         new DraconianErrorHandler()),
                                        sc,
                                        new DatatypeLibraryLoader());
    XmlOutput.output(p, args[1]);
  }
}