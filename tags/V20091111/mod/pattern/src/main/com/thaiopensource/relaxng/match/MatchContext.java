package com.thaiopensource.relaxng.match;

import org.relaxng.datatype.ValidationContext;

/**
 * Extends ValidationContext to provide information about which namespace URI is bound
 * to a prefix.
 */
public interface MatchContext extends ValidationContext {
  /**
   * Return a prefix bound to a namespace URI. When multiple prefixes
   * are bound to a namespace URI, one of the innermost such ones should be returned.
   * If namespaceURI is the empty string, null will be returned.
   * @param namespaceURI a String containing a namespace URI; must not be null
   * @return a non-empty prefix bound to namespaceURI, or null if no prefix is bound or if it is not
   * known which prefix is bound to namespaceURI
   */
  String getPrefix(String namespaceURI);
}
