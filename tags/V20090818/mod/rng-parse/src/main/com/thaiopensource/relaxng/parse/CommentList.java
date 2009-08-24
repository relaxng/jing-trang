package com.thaiopensource.relaxng.parse;

public interface CommentList<L> {
  void addComment(String value, L loc) throws BuildException;
}
