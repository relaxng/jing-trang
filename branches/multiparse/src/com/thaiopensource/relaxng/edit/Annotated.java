package com.thaiopensource.relaxng.edit;

import java.util.List;
import java.util.Vector;

public abstract class Annotated extends SourceObject {
  private final List attributeAnnotations = new Vector();
  private final List childElementAnnotations = new Vector();
  private final List followingElementAnnotations = new Vector();
  public List getAttributeAnnotations() {
    return attributeAnnotations;
  }
  public List getChildElementAnnotations() {
    return childElementAnnotations;
  }
  public List getFollowingElementAnnotations() {
    return followingElementAnnotations;
  }
  public boolean mayContainText() {
    return false;
  }
}
