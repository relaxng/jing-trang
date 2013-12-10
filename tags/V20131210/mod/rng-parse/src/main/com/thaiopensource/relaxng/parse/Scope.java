package com.thaiopensource.relaxng.parse;

public interface Scope<P, L, EA, CL extends CommentList<L>, A extends Annotations<L, EA, CL>> {
  P makeParentRef(String name, L loc, A anno) throws BuildException;
  P makeRef(String name, L loc, A anno) throws BuildException;
}
