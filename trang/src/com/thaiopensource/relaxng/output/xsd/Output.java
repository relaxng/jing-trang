package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.output.common.XmlWriter;
import com.thaiopensource.relaxng.edit.Pattern;

import java.io.IOException;

class Output {
  private final SchemaInfo si;
  private final ErrorReporter er;
  private final XmlWriter xw;

  static void output(SchemaInfo si, OutputDirectory od, ErrorReporter er) throws IOException {
    try {
      new Output(si,
                 er,
                 new XmlWriter(od.getLineSeparator(),
                               od.open(od.MAIN), new String[0],
                               od.getEncoding())).outputSchema(si.getPattern());
    }
    catch (XmlWriter.WrappedException e) {
      throw e.getIOException();
    }
  }

  private Output(SchemaInfo si, ErrorReporter er, XmlWriter xw) {
    this.si = si;
    this.er = er;
    this.xw = xw;
  }

  private void outputSchema(Pattern p) {
  }
}
