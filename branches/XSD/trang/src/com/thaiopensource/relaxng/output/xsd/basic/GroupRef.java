package com.thaiopensource.relaxng.output.xsd.basic;

public class GroupRef extends Particle {
  private final String name;

  public GroupRef(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Object accept(ParticleVisitor visitor) {
    return visitor.visitGroupRef(this);
  }
}
