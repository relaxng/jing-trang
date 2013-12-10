package com.thaiopensource.validation;

import javax.xml.XMLConstants;

/**
 * Some useful constants for the names of schema languages.
 */
public class Constants  {
  private Constants() { }

  /**
   * URI representating the Relax NG Compact Syntax schema language.
   * @see javax.xml.validation.SchemaFactory#newInstance(String)
   */
  static public final String RELAXNG_COMPACT_URI
          = "http://www.iana.org/assignments/media-types/application/relax-ng-compact-syntax";

  /**
   * URI representating the Relax NG XML Syntax schema language.
   * Identical to XMLConstants#RELAXNG_NS_URI
   * @see javax.xml.validation.SchemaFactory#newInstance(String)
   */
  static public final String RELAXNG_XML_URI = XMLConstants.RELAXNG_NS_URI;
}
