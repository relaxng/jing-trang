package com.thaiopensource.relaxng.output.dtd;

import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.util.Jaxp11XMLReaderCreator;
import com.thaiopensource.relaxng.util.ValidationEngine;
import com.thaiopensource.relaxng.util.DraconianErrorHandler;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.SchemaBuilderImpl;
import org.xml.sax.SAXException;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class Driver {
  static public void main(String[] args) throws IncorrectSchemaException, SAXException, IOException {
    SchemaCollection sc = new SchemaCollection();
    Pattern p = SchemaBuilderImpl.parse(new SAXParseable(new Jaxp11XMLReaderCreator(),
                                                         ValidationEngine.fileInputSource(args[0]),
                                                         new DraconianErrorHandler()),
                                        sc,
                                        new DatatypeLibraryLoader());

    new DtdOutput(null, new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(args[1])))).output(p);

  }
}
