package com.thaiopensource.relaxng.parse;

public interface SubParser<P, NC, L, EA, CL extends CommentList<L>, A extends Annotations<L, EA, CL>> {
  SubParseable<P, NC, L, EA, CL, A> createSubParseable(String href, String base) throws BuildException;
}
