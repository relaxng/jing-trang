package com.thaiopensource.relaxng.parse;

/**
 * Includes attributes and child elements before any RELAX NG element.
 */
public interface Annotations {
  void addAttribute(String ns, String localName, String prefix, String value, Location loc)
          throws BuildException;
  void addElement(ElementAnnotation ea) throws BuildException;
}
