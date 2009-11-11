package com.thaiopensource.relaxng.parse;

public interface Div<P, L, EA, CL extends CommentList<L>, A extends Annotations<L, EA, CL>>
        extends GrammarSection<P, L, EA, CL, A> {
  void endDiv(L loc, A anno) throws BuildException;
}
