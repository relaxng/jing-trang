package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

import java.util.Collections;
import java.util.Set;

/**
 * Base class for all implementations of com.thaiopensource.relaxng.match.NameClass.
 */
public abstract class NormalizedNameClass implements com.thaiopensource.relaxng.match.NameClass {
  // This is a Set<Name>.
  private final Set includedNames;

  /**
   * Create a NormalizedNameClass representing a name class without any wildcards.
   * @param includedNames an immutable set of names
   */
  public NormalizedNameClass(Set includedNames) {
    this.includedNames = immutable(includedNames);
  }

  public boolean isEmpty() {
    return includedNames.isEmpty();
  }

  public boolean contains(Name name) {
    return includedNames.contains(name);
  }

  public boolean isAnyNameIncluded() {
    return false;
  }

  public Set getExcludedNamespaces() {
    return null;
  }

  public Set getIncludedNames() {
    return includedNames;
  }

  public Set getExcludedNames() {
    return null;
  }

  public Set getIncludedNamespaces() {
    return Collections.EMPTY_SET;
  }

  public Set getExcludedLocalNames(String ns) {
    return null;
  }

  public abstract boolean equals(Object obj);

  boolean equal(NormalizedNameClass nc1, NormalizedNameClass nc2) {
    return nc1.includedNames.equals(nc2.includedNames);
  }

  public int hashCode() {
    return includedNames.hashCode();
  }

  Set immutable(Set set) {
    if (set.isEmpty())
      return Collections.EMPTY_SET;
    return Collections.unmodifiableSet(set);
  }

  abstract boolean includesNamespace(String ns);
}
