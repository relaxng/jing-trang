package com.thaiopensource.relaxng.input.parse;

import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.relaxng.edit.Comment;
import com.thaiopensource.relaxng.parse.CommentList;
import com.thaiopensource.relaxng.parse.BuildException;

import java.util.List;
import java.util.Vector;

public class CommentListImpl implements CommentList<SourceLocation> {
  final List<Comment> list = new Vector<Comment>();
  public void addComment(String value, SourceLocation loc) throws BuildException {
    Comment comment = new Comment(value);
    comment.setSourceLocation(loc);
    list.add(comment);
  }
  void add(CommentListImpl comments) {
    list.addAll(comments.list);
  }
}
