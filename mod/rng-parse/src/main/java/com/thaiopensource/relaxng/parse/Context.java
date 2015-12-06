package com.thaiopensource.relaxng.parse;

import org.relaxng.datatype.ValidationContext;

import java.util.Set;

public interface Context extends ValidationContext {
  Set<String> prefixes();
  Context copy();
}
