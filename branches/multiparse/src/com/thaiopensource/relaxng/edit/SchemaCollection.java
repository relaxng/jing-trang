package com.thaiopensource.relaxng.edit;


import java.util.Map;
import java.util.HashMap;

public class SchemaCollection {
  private final Map schemas = new HashMap();

  public SchemaCollection() {
  }

  public Map getSchemas() {
    return schemas;
  }
}
