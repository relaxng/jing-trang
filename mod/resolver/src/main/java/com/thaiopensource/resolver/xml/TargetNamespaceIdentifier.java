package com.thaiopensource.resolver.xml;

/**
 * Derived classes of Identifier should implement this when they have a "target namespace".
 * For example, when you are looking for a schema for a particular namespace, that namespace
 * would be your target namespace, which is distinct from the namespace of root element
 * of the schema.
 */
public interface TargetNamespaceIdentifier {
  public String getTargetNamespace();
}
