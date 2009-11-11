package com.thaiopensource.xml.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NamingTest {
  @Test
  public void testIsQname() {
    Assert.assertFalse(Naming.isQname("foo::"));
  }
}
