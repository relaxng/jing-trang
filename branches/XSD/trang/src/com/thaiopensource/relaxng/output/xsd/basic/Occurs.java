package com.thaiopensource.relaxng.output.xsd.basic;

public class Occurs {
  private final int min;
  private final int max;
  static public final int UNBOUNDED = Integer.MAX_VALUE;

  public Occurs(int min, int max) {
    this.min = min;
    this.max = max;
  }

  public int getMin() {
    return min;
  }

  public int getMax() {
    return max;
  }
}
