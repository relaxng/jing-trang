package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.output.common.Name;
import com.thaiopensource.relaxng.edit.SourceLocation;

public class Element extends Particle implements Structure {
  private final Name name;
  private final ComplexType complexType;

  public Element(SourceLocation location, Name name, ComplexType complexType) {
    super(location);
    this.name = name;
    this.complexType = complexType;
  }

  public Name getName() {
    return name;
  }

  public ComplexType getComplexType() {
    return complexType;
  }

  public Object accept(ParticleVisitor visitor) {
    return visitor.visitElement(this);
  }

  public Object accept(StructureVisitor visitor) {
    return visitor.visitElement(this);
  }

}
