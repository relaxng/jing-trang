package com.thaiopensource.validation;

import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Adds some convenience methods to Validator.
 */
abstract public class Validator2 extends Validator {
  protected Validator2() { }

  /**
   * Validate a file.
   * @param file the file to validate; must not be null.
   */
  public void validate(File file) throws SAXException, IOException {
    validate(new StreamSource(file));
  }

  /**
   * Validate a URL.
   * @param url the URL to validate
   */
  public void validate(URL url) throws SAXException, IOException {
    validate(new StreamSource(url.toExternalForm()));
  }
}
