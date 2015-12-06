package com.thaiopensource.relaxng.parse;

public interface DataPatternBuilder<P, L, EA, CL extends CommentList<L>, A extends Annotations<L, EA, CL>> {
  void addParam(String name, String value, Context context, String ns, L loc, A anno)
    throws BuildException;
  void annotation(EA ea);
  P makePattern(L loc, A anno)
    throws BuildException;
  P makePattern(P except, L loc, A anno)
    throws BuildException;
}
