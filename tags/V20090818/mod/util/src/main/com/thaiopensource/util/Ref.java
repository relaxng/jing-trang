package com.thaiopensource.util;

/**
 * Simple generic class to hold a reference to an object.
 */
public class Ref<T> {
  private T obj;

  public Ref() {
  }

  public Ref(T obj) {
    this.obj = obj;
  }

  public T get() {
    return obj;
  }

  public void set(T obj) {
    this.obj = obj;
  }

  public void clear() {
    this.obj = null;
  }
}
