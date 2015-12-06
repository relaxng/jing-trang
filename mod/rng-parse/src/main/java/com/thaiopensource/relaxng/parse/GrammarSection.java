package com.thaiopensource.relaxng.parse;

public interface GrammarSection<P, L, EA, CL extends CommentList<L>, A extends Annotations<L, EA, CL>> {

  static final class Combine {
    private final String name;
    private Combine(String name) {
      this.name = name;
    }
    final public String toString() {
      return name;
    }
  }

  static final Combine COMBINE_CHOICE = new Combine("choice");
  static final Combine COMBINE_INTERLEAVE = new Combine("interleave");

  static final String START = "#start";

  void define(String name, Combine combine, P pattern, L loc, A anno)
    throws BuildException;
  void topLevelAnnotation(EA ea) throws BuildException;
  void topLevelComment(CL comments) throws BuildException;
  Div<P, L, EA, CL, A> makeDiv();
  /*
   * Returns null if already in an include.
   */
  Include<P, L, EA, CL, A> makeInclude();
}
