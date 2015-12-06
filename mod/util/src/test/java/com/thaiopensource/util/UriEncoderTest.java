package com.thaiopensource.util;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
public class UriEncoderTest {
  @DataProvider(name = "paths")
  public Object[][] createPaths() {
    return new Object[][] {
            { "foo" },
            // disallowed printing chars
            { "<>\"{}|\\^`" },
            // ASCII controls
            { "\r\n\t\u0001\u007f" },
            { " " },
            // C1 controls
            { "\u0080\u009f" },
            // separators
            { "\u00a0\u2000\u2028\u2029" },
            // a normal Unicode character
            { "\u0e01" }
    };
  }

  @Test(dataProvider = "paths")
  public void testURIEncode(String path) throws URISyntaxException {
    Assert.assertEquals(UriEncoder.encode("http://example.com/" + path),
                        new URI("http", "example.com", "/" + path, null).toString());
  }

  @Test(dataProvider = "paths")
  public void testURIEncodeAsAscii(String path) throws URISyntaxException {
    Assert.assertEquals(UriEncoder.encodeAsAscii("http://example.com/" + path),
                        new URI("http", "example.com", "/" + path, null).toASCIIString());
  }

  @Test
  public void testPercentEncode() {
    Assert.assertEquals(new String(UriEncoder.percentEncode(new byte[] {
            0x00, 0x7e, 0x7f, (byte)0x80, (byte)0xFF })),
                        "%00%7E%7F%80%FF");
  }
}
