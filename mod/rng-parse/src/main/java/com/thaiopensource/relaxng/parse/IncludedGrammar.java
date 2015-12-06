package com.thaiopensource.relaxng.parse;

public interface IncludedGrammar<P, L, EA, CL extends CommentList<L>, A extends Annotations<L, EA, CL>>
        extends GrammarSection<P, L, EA, CL, A>, Scope<P, L, EA, CL, A> {
  P endIncludedGrammar(L loc, A anno) throws BuildException;
}
