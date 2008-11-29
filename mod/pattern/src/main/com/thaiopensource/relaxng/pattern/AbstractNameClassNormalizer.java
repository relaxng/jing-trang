package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  Base class for normalizing name classes.
 */
public abstract class AbstractNameClassNormalizer {
  private static final String IMPOSSIBLE = "\u0000";

  protected abstract boolean contains(Name name);
  protected abstract void accept(NameClassVisitor visitor);

  public NormalizedNameClass normalize() {
    final List mentionedNames = new ArrayList();
    final List mentionedNamespaces = new ArrayList();
    accept(new NameClassVisitor() {
      public void visitChoice(NameClass nc1, NameClass nc2) {
        nc1.accept(this);
        nc2.accept(this);
      }

      public void visitNsName(String ns) {
        mentionedNamespaces.add(ns);
      }

      public void visitNsNameExcept(String ns, NameClass nc) {
        mentionedNamespaces.add(ns);
        nc.accept(this);
      }

      public void visitAnyName() {
      }

      public void visitAnyNameExcept(NameClass nc) {
        nc.accept(this);
      }

      public void visitName(Name name) {
        mentionedNames.add(name);
      }

      public void visitNull() {
      }

      public void visitError() {
      }
    });
    if (contains(new Name(IMPOSSIBLE, IMPOSSIBLE))) {
      Set includedNames = new HashSet();
      Set excludedNamespaces = new HashSet();
      Set excludedNames = new HashSet();
      for (Iterator iter = mentionedNamespaces.iterator(); iter.hasNext();) {
        String ns = (String)iter.next();
        if (!contains(new Name(ns, IMPOSSIBLE)))
          excludedNamespaces.add(ns);
      }
      for (Iterator iter = mentionedNames.iterator(); iter.hasNext();) {
        Name name = (Name)iter.next();
        boolean in = contains(name);
        if (excludedNamespaces.contains(name.getNamespaceUri())) {
          if (in)
            includedNames.add(name);
        }
        else if (!in)
          excludedNames.add(name);
      }
      return new NormalizedAnyNameClass(includedNames, excludedNamespaces, excludedNames);
    }
    Map nsMap = new HashMap();
    for (Iterator iter = mentionedNamespaces.iterator(); iter.hasNext();) {
      String ns = (String)iter.next();
      if (contains(new Name(ns, IMPOSSIBLE)) && nsMap.get(ns) == null)
        nsMap.put(ns, new HashSet());
    }
    Set includedNames = new HashSet();
    for (Iterator iter = mentionedNames.iterator(); iter.hasNext();) {
      Name name = (Name)iter.next();
      boolean in = contains(name);
      Set excluded = (Set)nsMap.get(name.getNamespaceUri());
      if (excluded == null) {
        if (in)
          includedNames.add(name);
      }
      else if (!in)
        excluded.add(name.getLocalName());
    }
    return new NormalizedNsNameClass(includedNames, nsMap);
  }
}
