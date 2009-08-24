package com.thaiopensource.relaxng.edit;

import java.util.Set;

public interface NamespaceContext {
  String getNamespace(String prefix);
  Set<String> getPrefixes();
}
