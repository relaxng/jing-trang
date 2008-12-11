package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.Datatype2;
import com.thaiopensource.util.Localizer;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeStreamingValidator;
import org.relaxng.datatype.ValidationContext;
import org.relaxng.datatype.helpers.StreamingValidatorImpl;

abstract class DatatypeBase implements Datatype2 {
  abstract boolean lexicallyAllows(String str);
  private final int whiteSpace;

  static final int WHITE_SPACE_PRESERVE = 0;
  static final int WHITE_SPACE_REPLACE = 1;
  static final int WHITE_SPACE_COLLAPSE = 2;

  DatatypeBase() {
    whiteSpace = WHITE_SPACE_COLLAPSE;
  }

  DatatypeBase(int whiteSpace) {
    this.whiteSpace = whiteSpace;
  }

  int getWhiteSpace() {
    return whiteSpace;
  }

  public boolean isValid(String str, ValidationContext vc) {
    str = normalizeWhiteSpace(str);
    return lexicallyAllows(str) && allowsValue(str, vc);
  }

  public void checkValid(String str, ValidationContext vc) throws DatatypeException {
    str = normalizeWhiteSpace(str);
    checkLexicallyAllows(str);
    getValue(str, vc);
  }

  public Object createValue(String str, ValidationContext vc) {
    str = normalizeWhiteSpace(str);
    if (!lexicallyAllows(str))
      return null;
    try {
      return getValue(str, vc);
    }
    catch (DatatypeException e) {
      return null;
    }
  }

  final String normalizeWhiteSpace(String str) {
    switch (whiteSpace) {
    case WHITE_SPACE_COLLAPSE:
      return collapseWhiteSpace(str);
    case WHITE_SPACE_REPLACE:
      return replaceWhiteSpace(str);
    }
    return str;
  }

  void checkLexicallyAllows(String str) throws DatatypeException {
    if (!lexicallyAllows(str))
      throw createLexicallyInvalidException();
  }

  String getDescriptionForRestriction() {
    return getLexicalSpaceDescription(getLexicalSpaceKey());
  }

  final String getLexicalSpaceDescription(String key) {
    return localizer().message("lexical_space_" + key);
  }

  abstract String getLexicalSpaceKey();

  DatatypeException createLexicallyInvalidException() {
    return new DatatypeException(localizer().message("lexical_violation",
                                                     getLexicalSpaceDescription(getLexicalSpaceKey())));
  }

  // Requires lexicallyAllows to be true
  boolean allowsValue(String str, ValidationContext vc) {
    try {
      getValue(str, vc);
      return true;
    }
    catch (DatatypeException e) {
      return false;
    }
  }

  /* Requires lexicallyAllows to be true. Throws DatatypeException if value does not satisfy
     constraints on value space. */
  abstract Object getValue(String str, ValidationContext vc) throws DatatypeException;
  
  OrderRelation getOrderRelation() {
    return null;
  }

  /* For datatypes that have a length. */
  Measure getMeasure() {
    return null;
  }

  static private String collapseWhiteSpace(String s) {
    int i = collapseStart(s);
    if (i < 0)
      return s;
    StringBuffer buf = new StringBuffer(s.substring(0, i));
    boolean collapsing = (i == 0 || s.charAt(i - 1) == ' ');
    for (int len = s.length(); i < len; i++) {
      char c = s.charAt(i);
      switch (c) {
      case '\r':
      case '\n':
      case '\t':
      case ' ':
        if (!collapsing) {
          buf.append(' ');
          collapsing = true;
        }
        break;
      default:
        collapsing = false;
        buf.append(c);
        break;
      }
    }
    if (buf.length() > 0 && buf.charAt(buf.length() - 1) == ' ')
      buf.setLength(buf.length() - 1);
    return buf.toString();
  }

  static private int collapseStart(String s) {
    for (int i = 0, len = s.length(); i < len; i++) {
      switch (s.charAt(i)) {
      case ' ':
        if (i == 0 || s.charAt(i - 1) == ' ' || i == len - 1)
          return i;
        break;
      case '\r':
      case '\n':
      case '\t':
        return i;
      }
    }
    return -1;
  }

  static private String replaceWhiteSpace(String s) {
    int len = s.length();
    for (int i = 0; i < len; i++)
      switch (s.charAt(i)) {
      case '\r':
      case '\n':
      case '\t':
	{
	  char[] buf = s.toCharArray();
	  buf[i] = ' ';
	  for (++i; i < len; i++)
	    switch (buf[i]) {
	    case '\r':
	    case '\n':
	    case '\t':
	      buf[i] = ' ';
	    }
	  return new String(buf);
	}
      }
    return s;
  }

  DatatypeBase getPrimitive() {
    return this;
  }

  public boolean isContextDependent() {
    return false;
  }

  public boolean alwaysValid() {
    return false;
  }

  public int getIdType() {
    return ID_TYPE_NULL;
  }

  public int valueHashCode(Object value) {
    return value.hashCode();
  }

  public boolean sameValue(Object value1, Object value2) {
    return value1.equals(value2);
  }

  public DatatypeStreamingValidator createStreamingValidator(ValidationContext vc) {
    return new StreamingValidatorImpl(this, vc);
  }

  protected static Localizer localizer() {
    return DatatypeBuilderImpl.localizer;
  }
}
