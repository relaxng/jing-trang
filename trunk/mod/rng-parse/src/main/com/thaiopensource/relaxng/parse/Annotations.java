package com.thaiopensource.relaxng.parse;

/**
 * Includes attributes and child elements before any RELAX NG element.
 */
public interface Annotations<L, EA, CL extends CommentList<L>> {
  void addAttribute(String ns, String localName, String prefix, String value, L loc)
          throws BuildException;
  void addElement(EA ea) throws BuildException;
  /*
   * Adds comments following the last initial child element annotation.
   */
  void addComment(CL comments) throws BuildException;
  void addLeadingComment(CL comments) throws BuildException;
}
