package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class ParticleRepeat extends Particle {
  private final Particle child;
  private final Occurs occurs;

  public ParticleRepeat(SourceLocation location, Particle child, Occurs occurs) {
    super(location);
    this.child = child;
    this.occurs = occurs;
  }

  public Particle getChild() {
    return child;
  }

  public Occurs getOccurs() {
    return occurs;
  }

  public Object accept(ParticleVisitor visitor) {
    return visitor.visitRepeat(this);
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof ParticleRepeat))
      return false;
    ParticleRepeat other = (ParticleRepeat)obj;
    return this.child.equals(other.child) && this.occurs.equals(other.occurs);
  }

  public int hashCode() {
    return child.hashCode() ^ occurs.hashCode();
  }
}
