package com.thaiopensource.relaxng;

interface NameClass {
  boolean contains(Name name);
  void accept(NameClassVisitor visitor);
  boolean isOpen();
}
