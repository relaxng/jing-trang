package com.thaiopensource.relaxng.output.xsd.basic;

public class SymbolSpace {
  private final String name;

  public final static SymbolSpace GROUP = new SymbolSpace("group");
  public final static SymbolSpace TYPE = new SymbolSpace("type");
  public final static SymbolSpace ATTRIBUTE_GROUP = new SymbolSpace("attributeGroup");

  private SymbolSpace(String name) {
    this.name = name;
  }

  public String toString() {
    return name;
  }
}
