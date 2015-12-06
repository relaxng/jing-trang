package com.thaiopensource.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 *
 */
public class UriTest {
  @DataProvider(name = "resolve")
  public Object[][] resolveData() {
    return new Object[][] {
            { "http://example.com/", "foo", "http://example.com/foo"},
            { "http://example.com/<>{}", "foo", "http://example.com/foo"},            
            { "http://example.com/", "foo bar", "http://example.com/foo%20bar"},
            { "http://example.com/", "\u0e01", "http://example.com/\u0e01"},
            { "junk", "foo", "foo"},
            { "null", "foo", "foo"}
    };
  }
  @Test(dataProvider = "resolve")
  public void testResolve(String base, String ref, String result) {
    Assert.assertEquals(Uri.resolve(base, ref), result);
  }

  @DataProvider(name = "valid")
  public Object[][] createValid() {
    return new Object[][] {
            {"http://192.168.88.1"},
            {""},
            {"random:stuff"},
            {"random:\u0e01"},
            {"foo"}
    };
  }
  @Test(dataProvider = "valid")
  public void testValid(String uri) {
    Assert.assertTrue(Uri.isValid(uri));
  }

  @DataProvider(name = "invalid")
  public Object[][] createInvalid() {
    return new Object[][] {
            {"foo%0G"},
            {"foo#bar#baz"},
            {"foo_bar:baz"},
            {"123:foo"}
    };
  }
  @Test(dataProvider = "invalid")
  public void testInvalid(String uri) {
     Assert.assertFalse(Uri.isValid(uri));
  }
}
