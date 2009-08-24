package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

/**
 *  Normalizes a name classes.
 */
public class NameClassNormalizer extends AbstractNameClassNormalizer {
  private NameClass nameClass;

  public NameClassNormalizer(NameClass nameClass) {
    this.nameClass = nameClass;
  }

  protected boolean contains(Name name) {
    return nameClass.contains(name);
  }

  protected void accept(NameClassVisitor visitor) {
    nameClass.accept(visitor);
  }

  public NameClass getNameClass() {
    return nameClass;
  }

  public void setNameClass(NameClass nameClass) {
    this.nameClass = nameClass;
  }
}
