package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

import java.util.Set;

/**
 * A NormalizedNameClass that includes an any name wildcard.
 */
public class NormalizedAnyNameClass extends NormalizedNameClass {
  // A Set<String>.
  private final Set excludedNamespaces;
  // A Set<Name>.
  private final Set excludedNames;

  public NormalizedAnyNameClass(Set includedNames, Set excludedNamespaces, Set excludedNames) {
    super(includedNames);
    this.excludedNamespaces = immutable(excludedNamespaces);
    this.excludedNames = immutable(excludedNames);
  }

  public boolean isAnyNameIncluded() {
    return true;
  }

  public boolean contains(Name name) {
    if (excludedNamespaces.contains(name.getNamespaceUri()))
      return super.contains(name);
    else
      return !excludedNames.contains(name);
  }

  public boolean isEmpty() {
    return false;
  }

  public Set getExcludedNamespaces() {
    return excludedNamespaces;
  }

  public Set getExcludedNames() {
    return excludedNames;
  }

  public int hashCode() {
    return super.hashCode() ^ excludedNamespaces.hashCode() ^ excludedNames.hashCode();
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof NormalizedAnyNameClass))
      return false;
    NormalizedAnyNameClass other = (NormalizedAnyNameClass)obj;
    if (!(excludedNamespaces.equals(other.excludedNamespaces)))
      return false;
     if (!(excludedNames.equals(other.excludedNames)))
      return false;
    return equal(this, other);
  }

  boolean includesNamespace(String ns) {
    return !getExcludedNamespaces().contains(ns);
  }
}
