package com.thaiopensource.relaxng.output.dtd;

import org.xml.sax.ErrorHandler;
import com.thaiopensource.relaxng.edit.SourceLocation;

class ErrorReporter {
  private ErrorHandler eh;
  boolean hadError = false;

  ErrorReporter(ErrorHandler eh) {
    this.eh = eh;
  }

  void error(String key, SourceLocation loc) {
    hadError = true;
    System.err.println(loc.getLineNumber() + ":" + key);
  }
}
