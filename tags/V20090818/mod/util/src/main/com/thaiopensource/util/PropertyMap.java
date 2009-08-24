package com.thaiopensource.util;

public interface PropertyMap {
  public static final PropertyMap EMPTY = new PropertyMap() {
    public <T> T get(PropertyId<T> pid) {
      return null;
    }

    public boolean contains(PropertyId<?> pid) {
      return false;
    }

    public int size() {
      return 0;
    }

    public PropertyId<?> getKey(int i) {
      throw new IndexOutOfBoundsException();
    }
  };
  <T> T get(PropertyId<T> pid);
  boolean contains(PropertyId<?> pid);
  int size();
  PropertyId<?> getKey(int i);
}
