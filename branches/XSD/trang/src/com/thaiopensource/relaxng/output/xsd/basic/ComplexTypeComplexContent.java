package com.thaiopensource.relaxng.output.xsd.basic;

import java.util.List;

public class ComplexTypeComplexContent extends ComplexType {
  private final Particle particle;
  private final boolean mixed;
  /**
   * particle may be null
   */
  public ComplexTypeComplexContent(List attributeUses, Particle particle, boolean mixed) {
    super(attributeUses);
    this.particle = particle;
    this.mixed = mixed;
  }

  public Particle getParticle() {
    return particle;
  }

  public boolean isMixed() {
    return mixed;
  }

  public Object accept(ComplexTypeVisitor visitor) {
    return visitor.visitComplexContent(this);
  }
}
