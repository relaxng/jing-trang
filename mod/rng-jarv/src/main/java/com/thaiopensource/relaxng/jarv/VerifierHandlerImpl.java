package com.thaiopensource.relaxng.jarv;

import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.relaxng.sax.PatternValidator;
import com.thaiopensource.relaxng.pattern.ValidatorPatternBuilder;
import com.thaiopensource.xml.sax.CountingErrorHandler;
import org.iso_relax.verifier.VerifierHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

class VerifierHandlerImpl extends PatternValidator implements VerifierHandler {
  private boolean complete = false;
  private final CountingErrorHandler ceh;

  VerifierHandlerImpl(Pattern pattern, ValidatorPatternBuilder builder, CountingErrorHandler ceh) {
    super(pattern, builder, ceh);
    this.ceh = ceh;
  }

  public void endDocument() throws SAXException {
    super.endDocument();
    complete = true;
  }

  public boolean isValid() throws IllegalStateException {
    if (!complete)
      throw new IllegalStateException();
    return !ceh.getHadErrorOrFatalError();
  }

  void setErrorHandler(ErrorHandler eh) {
    ceh.setErrorHandler(eh);
  }

  public void reset() {
    super.reset();
    if (ceh != null)
      ceh.reset();
  }
}
