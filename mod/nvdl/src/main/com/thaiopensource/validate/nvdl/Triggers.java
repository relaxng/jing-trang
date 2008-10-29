package com.thaiopensource.validate.nvdl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Triggers {
  /**
   * A list with defined triggers.
   */
  private final List triggers = new ArrayList();
    
  /**
   * Adds a trigger for a namespace and a local name.
   * @param ns The namespace.
   * @param nameList The list of local names.
   */
  public String addTrigger(String ns, String nameList) {
  	Trigger t = new Trigger(ns, nameList);
  	triggers.add(t);
  	return t.errors;
  }
  
  /**
   * Indicates if we have a trigger on a namespace and local name.
   * @param ns The namespace.
   * @param name The local name.
   * @return true if we have a trigger set, otherwise false.
   */
  public boolean trigger(String namespace, String name, String parent) {
    // iterate triggers
  	Iterator i = triggers.iterator();
  	while (i.hasNext()) {
  	  Trigger t = (Trigger)i.next();
  	  if (t.trigger(namespace, name, parent)) {
  	    return true;
  	  }
  	}
  	return false;
  }  
}
