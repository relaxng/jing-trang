package com.thaiopensource.relaxng;

class NsNameClass implements NameClass {

  private final String namespaceURI;

  NsNameClass(String namespaceURI) {
    this.namespaceURI = namespaceURI;
  }

  public boolean contains(Name name) {
    return this.namespaceURI.equals(name.getNamespaceUri());
  }

  public int hashCode() {
    return namespaceURI.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof NsNameClass))
      return false;
    return namespaceURI.equals(((NsNameClass)obj).namespaceURI);
  }

  public void accept(NameClassVisitor visitor) {
    visitor.visitNsName(namespaceURI);
  }

  public boolean isOpen() {
    return true;
  }
}
