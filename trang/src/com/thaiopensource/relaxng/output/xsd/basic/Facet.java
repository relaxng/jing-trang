package com.thaiopensource.relaxng.output.xsd.basic;

public class Facet {
  private final String name;
  private final String value;

  public Facet(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}
