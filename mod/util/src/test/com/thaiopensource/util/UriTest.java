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
}
