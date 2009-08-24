package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
    final List<Name> mentionedNames = new ArrayList<Name>();
    final List<String> mentionedNamespaces = new ArrayList<String>();
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
      Set<Name> includedNames = new HashSet<Name>();
      Set<String> excludedNamespaces = new HashSet<String>();
      Set<Name> excludedNames = new HashSet<Name>();
      for (String ns : mentionedNamespaces) {
        if (!contains(new Name(ns, IMPOSSIBLE)))
          excludedNamespaces.add(ns);
      }
      for (Name name : mentionedNames) {
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
    Map<String, HashSet<String>> nsMap = new HashMap<String, HashSet<String>>();
    for (String ns : mentionedNamespaces) {
      if (contains(new Name(ns, IMPOSSIBLE)) && nsMap.get(ns) == null)
        nsMap.put(ns, new HashSet<String>());
    }
    Set<Name> includedNames = new HashSet<Name>();
    for (Name name : mentionedNames) {
      boolean in = contains(name);
      Set<String> excluded = nsMap.get(name.getNamespaceUri());
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
