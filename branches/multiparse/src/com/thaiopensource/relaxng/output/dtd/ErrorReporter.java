package com.thaiopensource.relaxng.output.dtd;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;
import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.util.Localizer;

class ErrorReporter {
  private Localizer localizer = new Localizer(ErrorReporter.class);
  private ErrorHandler eh;
  boolean hadError = false;

  static class WrappedSAXException extends RuntimeException {
    private SAXException exception;

    private WrappedSAXException(SAXException exception) {
      this.exception = exception;
    }

    public SAXException getException() {
      return exception;
    }
  }

  ErrorReporter(ErrorHandler eh) {
    this.eh = eh;
  }

  void error(String key, SourceLocation loc) {
    hadError = true;
    if (eh == null)
      return;
    try {
      eh.error(new SAXParseException(localizer.message(key),
                                     null,
                                     loc.getUri(),
                                     loc.getLineNumber(),
                                     loc.getColumnNumber()));
    }
    catch (SAXException e) {
      throw new WrappedSAXException(e);
    }
  }
}
