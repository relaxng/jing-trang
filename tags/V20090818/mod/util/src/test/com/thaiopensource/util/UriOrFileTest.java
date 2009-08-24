package com.thaiopensource.util;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.io.File;

public class UriOrFileTest {
  @DataProvider(name = "files")
  public Object[][] createFiles() {
    return new Object[][] {
            {"foo"},
            {"./foo:bar"},
            {"foo\u0e01"},
            {"c:foo"},
            {"c:\\foo"},
            {"12:34"}
    };
  }

  @Test(dataProvider = "files")
  public void testFileRoundTrip(String file) {
    Assert.assertEquals(UriOrFile.uriToUriOrFile(UriOrFile.toUri(file)),
                        new File(file).getAbsolutePath());
  }

  @DataProvider(name = "uris")
  public Object[][] createUris() {
    return new Object[][] {
            {"foo:bar"},
            {"http://www.example.com"},
            {"fo:o"}
    };
  }
  
  @Test(dataProvider = "uris")
  public void testUriRoundTrip(String uri) {
    Assert.assertEquals(UriOrFile.toUri(uri), uri);
  }

  @Test(dataProvider = "files")
  public void testToUri(String file) {
    Assert.assertEquals(UriOrFile.toUri(file),
                        new File(file).getAbsoluteFile().toURI().toString());
  }
}
