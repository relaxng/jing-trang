package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;

public class Schema extends Located {
  private final String uri;
  private Schema parent;

  public Schema(SourceLocation location, String uri) {
    super(location);
    this.uri = uri;
  }

  public String getUri() {
    return uri;
  }

  public Schema getParent() {
    return parent;
  }

  public void defineSimpleType(String name, SimpleType simpleType, SourceLocation location) {
  }

  public void defineGroup(String name, Particle particle, SourceLocation location) {
  }

  public void defineAttributeGroup(String name, List attributeUses, SourceLocation location) {
  }

  public void addRoot(Particle particle, SourceLocation location) {
  }

  public Schema addInclude(String uri, SourceLocation location) {
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
