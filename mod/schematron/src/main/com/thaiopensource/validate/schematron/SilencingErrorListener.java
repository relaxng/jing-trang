package com.thaiopensource.validate.schematron;

import net.sf.saxon.lib.StandardErrorListener;
import net.sf.saxon.trans.XPathException;

import javax.xml.transform.TransformerException;

public class SilencingErrorListener extends StandardErrorListener {
  @Override
  public void warning(TransformerException exception)
      throws TransformerException {
    if (exception instanceof XPathException) {
        XPathException xe = (XPathException) exception;
        if ("SXWN9000".equals(xe.getErrorCodeLocalPart())) {
            return;
        }
    }
    super.warning(exception);
  }
}
