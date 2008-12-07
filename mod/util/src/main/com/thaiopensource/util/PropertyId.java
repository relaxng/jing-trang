package com.thaiopensource.util;

public class PropertyId<T> {
  private final String name;
  private final Class<T> valueClass;

  public static <T> PropertyId<T> newInstance(String name, Class<T> valueClass) {
    return new PropertyId<T>(name, valueClass);
  }

  protected PropertyId(String name, Class<T> valueClass) {
    if (name == null || valueClass == null)
      throw new NullPointerException();
    this.name = name;
    this.valueClass = valueClass;
  }

  public Class<T> getValueClass() {
    return valueClass;
  }

  public final int hashCode() {
    return super.hashCode();
  }

  public final boolean equals(Object obj) {
    return super.equals(obj);
  }

  public String toString() {
    return name;
  }

  /**
   * @deprecated
   */
  public T get(PropertyMap map) {
    return map.get(this);
  }

  /**
   * @deprecated
   */
  public T put(PropertyMapBuilder builder, T value) {
    return builder.put(this, value);
  }
}
