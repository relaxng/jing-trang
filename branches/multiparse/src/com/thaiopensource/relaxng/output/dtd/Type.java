package com.thaiopensource.relaxng.output.dtd;

class Type {
  private final Type parent1;
  private final Type parent2;
  static Type COMPLEX_TYPE = new Type();
  static Type MIXED_ELEMENT_CLASS = new Type();
  static Type NOT_ALLOWED = new Type();
  static Type ATTRIBUTE_TYPE = new Type();
  static Type ATTRIBUTE_GROUP = new Type(COMPLEX_TYPE);
  static Type EMPTY = new Type(ATTRIBUTE_GROUP);
  static Type TEXT = new Type(MIXED_ELEMENT_CLASS, COMPLEX_TYPE);
  static Type DIRECT_TEXT = new Type(TEXT);
  // an attribute group plus a model group
  static Type COMPLEX_TYPE_MODEL_GROUP = new Type(COMPLEX_TYPE);
  static Type COMPLEX_TYPE_ZERO_OR_MORE_ELEMENT_CLASS = new Type(COMPLEX_TYPE_MODEL_GROUP);
  static Type MIXED_MODEL = new Type(COMPLEX_TYPE);
  static Type MODEL_GROUP = new Type(COMPLEX_TYPE_MODEL_GROUP);
  static Type ELEMENT_CLASS = new Type(MODEL_GROUP);
  static Type DIRECT_MULTI_ELEMENT = new Type(ELEMENT_CLASS);
  static Type DIRECT_SINGLE_ELEMENT = new Type(DIRECT_MULTI_ELEMENT);
  static Type DIRECT_SINGLE_ATTRIBUTE = new Type(ATTRIBUTE_GROUP);
  static Type OPTIONAL_ATTRIBUTE = new Type(ATTRIBUTE_GROUP);
  static Type ZERO_OR_MORE_ELEMENT_CLASS = new Type(MODEL_GROUP, COMPLEX_TYPE_ZERO_OR_MORE_ELEMENT_CLASS);
  static Type ENUM = new Type(ATTRIBUTE_TYPE);
  static Type ERROR = new Type();

  private Type() {
    this.parent1 = null;
    this.parent2 = null;
  }

  private Type(Type parent1) {
    this.parent1 = parent1;
    this.parent2 = null;
  }

  private Type(Type parent1, Type parent2) {
    this.parent1 = parent1;
    this.parent2 = parent2;
  }

  boolean isA(Type t) {
    if (this == t)
      return true;
    if (parent1 != null && parent1.isA(t))
      return true;
    if (parent2 != null && parent2.isA(t))
      return true;
    return false;
  }

  static Type zeroOrMore(Type t) {
    if (t.isA(ELEMENT_CLASS))
      return ZERO_OR_MORE_ELEMENT_CLASS;
    if (t.isA(MIXED_ELEMENT_CLASS))
      return MIXED_MODEL;
    return oneOrMore(t);
  }

  static Type oneOrMore(Type t) {
    if (t == ERROR)
      return ERROR;
    if (t.isA(MODEL_GROUP))
      return MODEL_GROUP;
    return null;
  }

  static Type group(Type t1, Type t2) {
    if (t1 == ERROR || t2 == ERROR)
      return ERROR;
    if (t1.isA(MODEL_GROUP) && t2.isA(MODEL_GROUP))
      return MODEL_GROUP;
    if (t1.isA(COMPLEX_TYPE_MODEL_GROUP) && t2.isA(COMPLEX_TYPE_MODEL_GROUP))
      return COMPLEX_TYPE_MODEL_GROUP;
    if (t1.isA(EMPTY) && t2.isA(EMPTY))
      return EMPTY;
    if (t1.isA(ATTRIBUTE_GROUP) && t2.isA(ATTRIBUTE_GROUP))
      return ATTRIBUTE_GROUP;
    if (t1.isA(ATTRIBUTE_GROUP)) {
      if (t2.isA(COMPLEX_TYPE_ZERO_OR_MORE_ELEMENT_CLASS))
        return COMPLEX_TYPE_ZERO_OR_MORE_ELEMENT_CLASS;
      if (t2.isA(COMPLEX_TYPE_MODEL_GROUP))
        return COMPLEX_TYPE_MODEL_GROUP;
      if (t2.isA(COMPLEX_TYPE))
        return COMPLEX_TYPE;
    }
    else if (t2.isA(ATTRIBUTE_GROUP))
      return group(t1, t2);
    return null;
  }

  static Type mixed(Type t) {
    if (t.isA(ATTRIBUTE_GROUP))
      return COMPLEX_TYPE;
    if (t.isA(ZERO_OR_MORE_ELEMENT_CLASS))
      return MIXED_MODEL;
    if (t.isA(COMPLEX_TYPE_ZERO_OR_MORE_ELEMENT_CLASS))
      return COMPLEX_TYPE;
    return null;
  }

  static Type interleave(Type t1, Type t2) {
    if (t1 == ERROR || t2 == ERROR)
      return ERROR;
    if (t1.isA(EMPTY) && t2.isA(EMPTY))
      return EMPTY;
    if (t1.isA(ATTRIBUTE_GROUP) && t2.isA(ATTRIBUTE_GROUP))
      return ATTRIBUTE_GROUP;
    if (t1.isA(ATTRIBUTE_GROUP)) {
      if (t2.isA(COMPLEX_TYPE_ZERO_OR_MORE_ELEMENT_CLASS))
        return COMPLEX_TYPE_ZERO_OR_MORE_ELEMENT_CLASS;
      if (t2.isA(COMPLEX_TYPE_MODEL_GROUP))
        return COMPLEX_TYPE_MODEL_GROUP;
      if (t2.isA(COMPLEX_TYPE))
        return COMPLEX_TYPE;
    }
    else if (t2.isA(ATTRIBUTE_GROUP))
      return interleave(t1, t2);
    return null;
  }

  static Type optional(Type t) {
    if (t == ERROR)
      return ERROR;
    if (t == DIRECT_SINGLE_ATTRIBUTE)
      return OPTIONAL_ATTRIBUTE;
    if (t == OPTIONAL_ATTRIBUTE)
      return OPTIONAL_ATTRIBUTE;
    if (t.isA(MODEL_GROUP))
      return MODEL_GROUP;
    if (t.isA(MIXED_ELEMENT_CLASS))
      return MIXED_ELEMENT_CLASS;
    if (t == NOT_ALLOWED)
      return MODEL_GROUP;
    return null;
  }

  static Type choice(Type t1, Type t2) {
    if (t1 == ERROR || t2 == ERROR)
      return ERROR;
    if (t1 == NOT_ALLOWED) {
      if (t2 == NOT_ALLOWED)
        return NOT_ALLOWED;
      if (t2.isA(ELEMENT_CLASS))
        return ELEMENT_CLASS;
      if (t2.isA(MIXED_ELEMENT_CLASS))
        return MIXED_ELEMENT_CLASS;
      if (t2.isA(MODEL_GROUP))
        return MODEL_GROUP;
      if (t2.isA(ENUM))
        return ENUM;
      return null;
    }
    if (t2 == NOT_ALLOWED)
      return choice(t2, t1);
    if (t1.isA(ELEMENT_CLASS) && t2.isA(ELEMENT_CLASS))
      return ELEMENT_CLASS;
    if (t1.isA(MODEL_GROUP) && t2.isA(MODEL_GROUP))
      return MODEL_GROUP;
    if ((t1.isA(MIXED_ELEMENT_CLASS) && t2.isA(ELEMENT_CLASS))
            || (t1.isA(ELEMENT_CLASS) && t2.isA(MIXED_ELEMENT_CLASS)))
      return MIXED_ELEMENT_CLASS;
    if (t1.isA(ENUM) && t2.isA(ENUM))
      return ENUM;
    return null;
  }

  static Type ref(Type t) {
    if (t == DIRECT_TEXT)
      return TEXT;
    if (t == DIRECT_SINGLE_ATTRIBUTE)
      return ATTRIBUTE_GROUP;
    if (t.isA(DIRECT_MULTI_ELEMENT))
      return ELEMENT_CLASS;
    if (t == ZERO_OR_MORE_ELEMENT_CLASS)
      return MODEL_GROUP;
    if (t == COMPLEX_TYPE_ZERO_OR_MORE_ELEMENT_CLASS)
      return COMPLEX_TYPE;
    return t;
  }
}
