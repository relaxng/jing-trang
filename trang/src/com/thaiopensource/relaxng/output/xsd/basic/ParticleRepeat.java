package com.thaiopensource.relaxng.output.xsd.basic;

public class ParticleRepeat extends Particle {
  private final Particle child;
  private final Occurs occcurs;

  public ParticleRepeat(Particle child, Occurs occcurs) {
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
