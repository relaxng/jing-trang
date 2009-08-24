package com.thaiopensource.datatype.xsd;

import com.thaiopensource.xml.util.Name;
import com.thaiopensource.xml.util.Naming;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;

class QNameDatatype extends DatatypeBase {
  public boolean lexicallyAllows(String str) {
    return Naming.isQname(str);
  }

  String getLexicalSpaceKey() {
    return "qname";
  }

  Object getValue(String str, ValidationContext vc) throws DatatypeException {
    int i = str.indexOf(':');
    if (i < 0) {
      String ns = vc.resolveNamespacePrefix("");
      if (ns == null)
	ns = "";
      return new Name(ns, str);
    }
    else {
      String prefix = str.substring(0, i);
      String ns = vc.resolveNamespacePrefix(prefix);
      if (ns == null)
	throw new DatatypeException(localizer().message("undeclared_prefix", prefix));
      return new Name(ns, str.substring(i + 1));
    }
  }

  boolean allowsValue(String str, ValidationContext vc) {
    int i = str.indexOf(':');
    return i < 0 || vc.resolveNamespacePrefix(str.substring(0, i)) != null;
  }

  public boolean isContextDependent() {
    return true;
  }
}
