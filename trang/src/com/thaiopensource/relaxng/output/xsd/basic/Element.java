package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.output.common.Name;

public class Element extends Particle {
  private final Name name;
  private final ComplexType complexType;

  public Element(Name name, ComplexType complexType) {
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
}
