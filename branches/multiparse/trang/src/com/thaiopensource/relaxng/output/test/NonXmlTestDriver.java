package com.thaiopensource.relaxng.output.test;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;

import java.io.IOException;
import java.io.File;

import com.thaiopensource.relaxng.XMLReaderCreator;
import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.SchemaBuilderImpl;
import com.thaiopensource.relaxng.output.rng.RngOutputFormat;
import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.LocalOutputDirectory;
import com.thaiopensource.relaxng.output.OutputFailedException;
import com.thaiopensource.relaxng.parse.nonxml.NonXmlParseable;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.util.Jaxp11XMLReaderCreator;
import com.thaiopensource.util.UriOrFile;

public class NonXmlTestDriver {

  private XMLReaderCreator xrc = new Jaxp11XMLReaderCreator();
  private ErrorHandler eh;
  private OutputFormat of = new RngOutputFormat();

  private NonXmlTestDriver() {
  }

  static public void main(String[] args) throws IOException {
    System.exit(new NonXmlTestDriver().doMain(args));
  }

  private int doMain(String[] args) throws IOException {
    return runTestSuite(new File(args[1])) ? 0 : 1;
  }

  private boolean runTestSuite(File dir) throws IOException {
    boolean passed = true;
    String[] subdirs = dir.list();
    for (int i = 0; i < subdirs.length; i++) {
      File subdir = new File(dir, subdirs[i]);
      if (subdir.isDirectory()) {
        if (!runTestCase(subdir))
          passed = false;
      }
    }
    return passed;
  }

  static private final String XML_DIR = "xml";
  static private final String NON_XML_DIR = "nonxml";
  static private final String OUT_DIR = "out";
  static private final String CORRECT_SCHEMA_NAME = "c";
  static private final String INCORRECT_SCHEMA_NAME = "i";
  static private final String NON_XML_EXTENSION = ".rnx";
  static private final String XML_EXTENSION = ".rng";
  static private final String OUTPUT_ENCODING = "UTF-8";

  private boolean runTestCase(File dir) throws IOException {
    File xmlDir = new File(dir, XML_DIR);
    File nonxmlDir = new File(dir, NON_XML_DIR);
    File outputDir = new File(dir, OUT_DIR);
    File correct = new File(nonxmlDir, CORRECT_SCHEMA_NAME + NON_XML_EXTENSION);
    File incorrect = new File(nonxmlDir, INCORRECT_SCHEMA_NAME + NON_XML_EXTENSION);
    boolean passed = true;
    if (correct.exists()) {
      File output = new File(outputDir, CORRECT_SCHEMA_NAME + XML_EXTENSION);
      if (!run(correct, output) || !compareDir(xmlDir, outputDir)) {
        passed = false;
        failed(correct);
      }
    }
    if (incorrect.exists()) {
      File output = new File(outputDir, INCORRECT_SCHEMA_NAME + XML_EXTENSION);
      if (run(incorrect, output)) {
        passed = false;
        failed(incorrect);
      }
    }
    return passed;
  }

  private boolean compareDir(File goodDir, File testDir) {
    try {
      String[] files = goodDir.list();
      for (int i = 0; i < files.length; i++) {
        File file = new File(goodDir, files[i]);
        if (file.isDirectory()) {
          if (!compareDir(file, new File(testDir, files[i])))
            return false;
        }
        else if (!Compare.compare(file, new File(testDir, files[i]), xrc))
          return false;
      }
      return true;
    }
    catch (SAXException e) {
    }
    catch (IOException e) {
    }
    return false;
  }

  private void failed(File f) {
    System.err.println(f.toString() + " failed");
  }

  private boolean run(File in, File out) throws IOException {
    try {
      Parseable parseable = new NonXmlParseable(new InputSource(UriOrFile.fileToUri(in)), eh);
      SchemaCollection sc = SchemaBuilderImpl.parse(parseable,
                                                    new DatatypeLibraryLoader());
      OutputDirectory od = new LocalOutputDirectory(out, XML_EXTENSION, OUTPUT_ENCODING);
      of.output(sc, od, eh);
      return true;
    }
    catch (SAXException e) {
      return false;
    }
    catch (IncorrectSchemaException e) {
      return false;
    }
    catch (OutputFailedException e) {
      return false;
    }
  }

}
