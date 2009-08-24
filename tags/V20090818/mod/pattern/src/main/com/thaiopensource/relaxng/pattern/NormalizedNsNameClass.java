package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

import java.util.Map;
import java.util.Set;

/**
 * A NormalizedNsNameClass that contains one or more namespace wildcards.
 */
public class NormalizedNsNameClass extends NormalizedNameClass {
  private final Map<String, ? extends Set<String>> nsMap;
  private final Set<String> includedNamespaces;

  public NormalizedNsNameClass(Set<Name> includedNames, Map<String, ? extends Set<String>> nsMap) {
    super(includedNames);
    this.nsMap = nsMap;
    includedNamespaces = immutable(nsMap.keySet());
  }

  public boolean isEmpty() {
    return super.isEmpty() && nsMap.isEmpty();
  }

  public boolean contains(Name name) {
    Set<String> excludedLocalNames = nsMap.get(name.getNamespaceUri());
    if (excludedLocalNames == null)
      return super.contains(name);
    else
      return !excludedLocalNames.contains(name.getLocalName());
  }

  public Set<String> getIncludedNamespaces() {
    return includedNamespaces;
  }

  public Set<String> getExcludedLocalNames(String ns) {
    return nsMap.get(ns);
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
