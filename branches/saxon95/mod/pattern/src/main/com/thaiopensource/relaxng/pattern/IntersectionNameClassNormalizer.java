package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Computes the normalized intersection of zero or more name classes.
 */
public class IntersectionNameClassNormalizer extends AbstractNameClassNormalizer {
  private final List<NameClass> nameClasses = new ArrayList<NameClass>();

  public void add(NameClass nc) {
    nameClasses.add(nc);
  }

  protected void accept(NameClassVisitor visitor) {
    for (NameClass nameClass : nameClasses)
      (nameClass).accept(visitor);
  }

  protected boolean contains(Name name) {
    Iterator<NameClass> iter = nameClasses.iterator();
    if (!iter.hasNext())
      return false;
    for (;;) {
      if (!(iter.next()).contains(name))
        return false;
      if (!iter.hasNext())
        break;
    }
    return true;
  }
}
