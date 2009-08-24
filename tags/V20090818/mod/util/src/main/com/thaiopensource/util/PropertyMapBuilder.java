package com.thaiopensource.util;

import java.util.HashMap;
import java.util.Map;

public class PropertyMapBuilder {
  private Map<PropertyId<?>, Object> map;
  private PropertyId<?>[] keys;

  private static class PropertyMapImpl implements PropertyMap {
    private final Map<PropertyId<?>, Object> map;
    private final PropertyId<?>[] keys;

    private PropertyMapImpl(Map<PropertyId<?>, Object> map, PropertyId<?>[] keys) {
      this.map = map;
      this.keys = keys;
    }

    public <T> T get(PropertyId<T> pid) {
      return pid.getValueClass().cast(map.get(pid));
    }

    public int size() {
      return keys.length;
    }

    public boolean contains(PropertyId<?> pid) {
      return map.get(pid) != null;
    }

    public PropertyId<?> getKey(int i) {
      return keys[i];
    }
  }

  public PropertyMapBuilder() {
    this.map = new HashMap<PropertyId<?>, Object>();
  }

  public PropertyMapBuilder(PropertyMap pm) {
    if (pm instanceof PropertyMapImpl) {
      PropertyMapImpl pmi = (PropertyMapImpl)pm;
      this.map = pmi.map;
      this.keys = pmi.keys;
    }
    else {
      this.map = new HashMap<PropertyId<?>, Object>();
      add(pm);
    }
  }

  public void add(PropertyMap pm) {
   for (int i = 0, len = pm.size(); i < len; i++)
     copy(pm.getKey(i), pm);
  }
  
  private <T> void copy(PropertyId<T> pid, PropertyMap pm) {
    put(pid, pm.get(pid));
  }

  private void lock() {
    if (keys != null)
      return;
    keys = new PropertyId<?>[map.size()];
    int i = 0;
    for (PropertyId<?> propertyId : map.keySet())
      keys[i++] = propertyId;
  }

  private void copyIfLocked() {
    if (keys == null)
      return;
    Map<PropertyId<?>, Object> newMap = new HashMap<PropertyId<?>, Object>();
    for (int i = 0; i < keys.length; i++)
      newMap.put(keys[i], map.get(keys[i]));
    map = newMap;
    keys = null;
  }

  public PropertyMap toPropertyMap() {
    lock();
    return new PropertyMapImpl(map, keys);
  }

  public <T> T put(PropertyId<T> id, T value) {
    copyIfLocked();
    final Class<T> cls = id.getValueClass();
    if (value == null)
      return cls.cast(map.remove(id));
    return cls.cast(map.put(id, cls.cast(value)));
  }

  public <T> T get(PropertyId<T> pid) {
    return pid.getValueClass().cast(map.get(pid));
  }

  public boolean contains(PropertyId<?> pid) {
    return map.get(pid) != null;
  }
}
