package com.thaiopensource.relaxng.parse.sax;

import org.relaxng.datatype.ValidationContext;
import org.xml.sax.DTDHandler;
import org.xml.sax.SAXException;

import java.util.HashSet;
import java.util.Set;

public abstract class DtdContext implements DTDHandler, ValidationContext {
  private final Set<String> notations;
  private final Set<String> unparsedEntities;

  public DtdContext() {
    notations = new HashSet<String>();
    unparsedEntities = new HashSet<String>();
  }

  public DtdContext(DtdContext dc) {
    notations = dc.notations;
    unparsedEntities = dc.unparsedEntities;
  }

  public void notationDecl(String name,
                           String publicId,
                           String systemId)
          throws SAXException {
    notations.add(name);
  }

  public void unparsedEntityDecl(String name,
                                 String publicId,
                                 String systemId,
                                 String notationName)
          throws SAXException {
    unparsedEntities.add(name);
  }

  public boolean isNotation(String notationName) {
    return notations.contains(notationName);
  }

  public boolean isUnparsedEntity(String entityName) {
    return unparsedEntities.contains(entityName);
  }

  public void clearDtdContext() {
    notations.clear();
    unparsedEntities.clear();
  }
}
