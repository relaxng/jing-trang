package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;

abstract class ValueRestrictDatatype extends RestrictDatatype {
  ValueRestrictDatatype(DatatypeBase base) {
    super(base);
  }

  Object getValue(String str, ValidationContext vc) throws DatatypeException {
    Object obj = super.getValue(str, vc);
    checkRestriction(obj);
    return obj;
  }

  abstract void checkRestriction(Object obj) throws DatatypeException;
}
