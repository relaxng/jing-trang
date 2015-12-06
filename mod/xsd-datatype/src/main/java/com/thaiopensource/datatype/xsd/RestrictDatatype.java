package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;

abstract class RestrictDatatype extends DatatypeBase {
  protected final DatatypeBase base;
  
  RestrictDatatype(DatatypeBase base) {
    this(base, base.getWhiteSpace());
  }

  RestrictDatatype(DatatypeBase base, int whiteSpace) {
    super(whiteSpace);
    this.base = base;
  }

  boolean lexicallyAllows(String str) {
    return base.lexicallyAllows(str);
  }

  void checkLexicallyAllows(String str) throws DatatypeException {
    base.checkLexicallyAllows(str);
  }

  String getLexicalSpaceKey() {
    return base.getLexicalSpaceKey();
  }

  OrderRelation getOrderRelation() {
    return base.getOrderRelation();
  }

  Measure getMeasure() {
    return base.getMeasure();
  }

  DatatypeBase getPrimitive() {
    return base.getPrimitive();
  }

  public int getIdType() {
    return base.getIdType();
  }

  public boolean sameValue(Object value1, Object value2) {
    return base.sameValue(value1, value2);
  }

  public int valueHashCode(Object value) {
    return base.valueHashCode(value);
  }

  Object getValue(String str, ValidationContext vc) throws DatatypeException {
    return base.getValue(str, vc);
  }
}
