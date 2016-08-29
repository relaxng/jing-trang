package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.relaxng.parse.Annotations;
import com.thaiopensource.relaxng.parse.BuildException;
import com.thaiopensource.util.VoidValue;
import org.xml.sax.Locator;

public class AnnotationsImpl extends CommentListImpl implements
        Annotations<Locator, VoidValue, CommentListImpl> {
  
  String defaultValue = null;
    
  public void addAttribute(String ns, String localName, String prefix, String value, Locator loc)
          throws BuildException {
    
    if ("defaultValue".equals(localName) && 
        "http://relaxng.org/ns/compatibility/annotations/1.0".equals(ns)) {
      defaultValue = value;
    }
    
  }

  public void addElement(VoidValue voidValue) throws BuildException {
  }

  public void addComment(CommentListImpl comments) throws BuildException {
  }

  public void addLeadingComment(CommentListImpl comments) throws BuildException {
  }
}
