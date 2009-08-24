package com.thaiopensource.relaxng.match;

import com.thaiopensource.xml.util.Name;

import java.util.Set;

/**
 * A RELAX NG name class.  Equivalent name classes are normalized so that they have the
 * same representation.
 */
public interface NameClass {
  /**
   * Tests whether this name class is empty. A name class is empty if there is no name in the
   * name class.
   * @return true if the name class is empty, false otherwise
   */
  boolean isEmpty();

  /**
   * Tests whether this name class contains a name.
   * @param name the name to test; must not be null
   * @return true if the name class contains the name, false otherwise
   */
  boolean contains(Name name);

  /**
   * Tests whethers this name class contains a wildcard matching any name.  If true, then the
   * name class contains all names except for those in getExcludedNames() or whose namespace
   * is in getExcludedNamespaces(); it also contains those names in getIncludedNames().
   * @return true if this name class contains a wildcard matching any name, false otherwise.
   */
  boolean isAnyNameIncluded();

  /**
   * Returns the set of namespaces excluded from a wildcard matching any name.
   * @return a non-null immutable, possibly empty Set each member of which is a non-null String,
   * if isAnyNameIncluded() is true, null otherwise
   * @see #isAnyNameIncluded
   */
  Set<String> getExcludedNamespaces();

  /**
   * Returns the set of names excluded from a wildcard matching any name.  None of the names
   * will have namespaces in getExcludedNamespaces().
   * @return a non-null immutable, possibly empty Set each member of which is a non-null Name,
   * if isAnyNameIncluded() is true, null otherwise
   * @see #isAnyNameIncluded
   * @see Name
   */
  Set<Name> getExcludedNames();

  /**
   * Returns the set of names that this name class contains. This doesn't include any wildcards
   * included in the name class. If isAnyNameIncluded() is true, then all the names in the
   * returned set will have a namespace that is in getExcludedNamespaces(). None of the names
   * will have a namespace that is in getIncludedNamespaces().
   * @return a non-null, possibly empty Set, each member of which is a non-null Name
   * @see Name
   */
  Set<Name> getIncludedNames();

  /**
   * Returns the set of namespace wildcards that this name class contains. If a string <var>s</var> is in the
   * returned set, then the name class contains all names whose namespace is s, with
   * the exception of any names contained in getExcludedNames(<var>s</var>). The
   * default namespace is represented by a zero-length String. The returned set will be empty
   * if isAnyNameIncluded() is true.
   * @return a non-null, possibly empty, immutable Set each member of which is a non-null String
   * @see #isAnyNameIncluded
   */
  Set<String> getIncludedNamespaces();

  /**
   * Returns the set of local names excluded from a namespace wildcard.
   * @param ns the namespace from which the local names are excluded
   * @return a non-null possibly empty Set each member of which is a non-null String,
   * if ns is in getIncludedNamespaces(), null otherwise
   * @see #getIncludedNamespaces
   */
  Set<String> getExcludedLocalNames(String ns);
}
