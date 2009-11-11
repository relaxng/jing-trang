package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;

class EntityDatatype extends NCNameDatatype {
  boolean allowsValue(String str, ValidationContext vc) {
    return vc.isUnparsedEntity(str);
  }

  Object getValue(String str, ValidationContext vc) throws DatatypeException {
    if (!allowsValue(str, vc))
      throw new DatatypeException(localizer().message("entity_violation"));
    return str;
  }
}
