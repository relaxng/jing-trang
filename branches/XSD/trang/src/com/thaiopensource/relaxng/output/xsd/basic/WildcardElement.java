package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class WildcardElement extends Particle {
  private final Wildcard wildcard;

  public WildcardElement(SourceLocation location, Wildcard wildcard) {
    super(location);
    this.wildcard = wildcard;
  }

  public Wildcard getWildcard() {
    return wildcard;
  }

  public boolean equals(Object obj) {
    return obj instanceof WildcardElement && ((WildcardElement)obj).wildcard.equals(wildcard);
  }

  public int hashCode() {
    return wildcard.hashCode();
  }

  public Object accept(ParticleVisitor visitor) {
    return visitor.visitWildcardElement(this);
  }
}
