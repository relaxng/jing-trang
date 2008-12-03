package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

import java.util.Map;
import java.util.Set;

/**
 * A NormalizedNsNameClass that contains one or more namespace wildcards.
 */
public class NormalizedNsNameClass extends NormalizedNameClass {
  /**
   * A Map<String, Set<String>>.
   */
  private final Map nsMap;
  /**
   * A Set<String>.
   */
  private final Set includedNamespaces;

  public NormalizedNsNameClass(Set includedNames, Map nsMap) {
    super(includedNames);
    this.nsMap = nsMap;
    includedNamespaces = immutable(nsMap.keySet());
  }

  public boolean isEmpty() {
    return super.isEmpty() && nsMap.isEmpty();
  }

  public boolean contains(Name name) {
    Set excludedLocalNames = (Set)nsMap.get(name.getNamespaceUri());
    if (excludedLocalNames == null)
      return super.contains(name);
    else
      return !excludedLocalNames.contains(name);
  }

  public Set getIncludedNamespaces() {
    return includedNamespaces;
  }

  public Set getExcludedLocalNames(String ns) {
    return (Set)nsMap.get(ns);
  }

  public int hashCode() {
    return super.hashCode() ^ nsMap.hashCode();
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof NormalizedNsNameClass))
      return false;
    NormalizedNsNameClass other = (NormalizedNsNameClass)obj;
    if (!nsMap.equals(other.nsMap))
      return false;
    return equal(this, other);
  }

  boolean includesNamespace(String ns) {
    return getIncludedNamespaces().contains(ns);
  }
}
