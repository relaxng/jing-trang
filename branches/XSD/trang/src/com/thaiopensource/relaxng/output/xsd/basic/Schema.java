package com.thaiopensource.relaxng.output.xsd.basic;

import java.util.List;

public class Schema {
  private final String uri;
  private Schema parent;

  public Schema(String uri) {
    this.uri = uri;
  }

  public String getUri() {
    return uri;
  }

  public Schema getParent() {
    return parent;
  }

  public void defineSimpleType(String name, SimpleType simpleType) {
  }

  public void defineGroup(String name, Particle particle) {
  }

  public void defineAttributeGroup(String name, List attributeUses) {
  }

  public void addRoot(Particle particle) {
  }

  public Schema addInclude(String uri) {
    return null;
  }

  public Particle getGroup(String name) {
    return null;
  }

  public SimpleType getSimpleType(String name) {
    return null;
  }

  public List getAttributeGroup(String name) {
    return null;
  }

  public void accept(SchemaVisitor visitor) {

  }

  // remove methods
  // XXX where is each thing defined
  // XXX
}
