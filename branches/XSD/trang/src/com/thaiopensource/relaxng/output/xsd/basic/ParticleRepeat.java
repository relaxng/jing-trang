package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class ParticleRepeat extends Particle {
  private final Particle child;
  private final Occurs occcurs;

  public ParticleRepeat(SourceLocation location, Particle child, Occurs occcurs) {
    super(location);
    this.child = child;
    this.occcurs = occcurs;
  }

  public Particle getChild() {
    return child;
  }

  public Occurs getOcccurs() {
    return occcurs;
  }

  public Object accept(ParticleVisitor visitor) {
    return visitor.visitRepeat(this);
  }
}
