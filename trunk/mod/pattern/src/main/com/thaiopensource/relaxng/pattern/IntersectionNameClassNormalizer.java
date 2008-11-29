package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Computes the normalized intersection of zero or more name classes.
 */
public class IntersectionNameClassNormalizer extends AbstractNameClassNormalizer {
  private final List nameClasses = new ArrayList();

  public void add(NameClass nc) {
    nameClasses.add(nc);
  }

  protected void accept(NameClassVisitor visitor) {
    for (Iterator iter = nameClasses.iterator(); iter.hasNext();)
      ((NameClass)iter.next()).accept(visitor);
  }

  protected boolean contains(Name name) {
    Iterator iter = nameClasses.iterator();
    if (!iter.hasNext())
      return false;
    for (;;) {
      if (!((NameClass)iter.next()).contains(name))
        return false;
      if (!iter.hasNext())
        break;
    }
    return true;
  }
}
