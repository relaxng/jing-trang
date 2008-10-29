package com.thaiopensource.validate.nvdl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import com.thaiopensource.xml.util.Naming;

/**
 * Strores trigger information and checks trigger activation.
 * @author george
 */
public class Trigger {
  /**
   * The namespace name for the local element names.
   */
  String namespace;
  /**
   * List with local names.
   */
  List elementNames;
  
  /**
   * Stores the invalid local names if any, otherwise null.
   */
  String errors;
  
  /**
   * Creates a trigger to store the elements that break sections
   * for a given namespace.
   * @param namespace The namespace
   * @param nameList A space separated list of local names.
   */
  Trigger(String namespace, String nameList) {
    StringTokenizer st = new StringTokenizer(nameList);
    elementNames = new ArrayList(st.countTokens());
    while (st.hasMoreTokens()) {
      String name = st.nextToken();
      elementNames.add(name);
      if (!Naming.isNcname(name)) {
        if (errors == null) {
          errors = name;
        } else {
          errors += (" " + name);
        }
      }
    }
    this.namespace = namespace;
  }

  /**
   * Checks trigger activation given an element name and its parent, 
   * both from the same namespace.
   * @param ns The namespace for the elements.
   * @param name The current element local name.
   * @param parent The parent element local name.
   * @return true if we should brake the section.
   */
  boolean trigger(String ns, String name, String parent) {
    if ((ns==null && namespace==null) || (ns!=null && ns.equals(namespace))) {
      if (elementNames.contains(name)) {
        return (!elementNames.contains(parent));
      }
    }
	return false;
  }
}
