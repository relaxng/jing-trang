package com.thaiopensource.resolver.catalog;

import com.thaiopensource.resolver.Input;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.ResolverException;
import com.thaiopensource.resolver.xml.ExternalIdentifier;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests CatalogResolver.
 */
public class CatalogResolverTest {
  @Test
  public void testResolve() throws IOException, ResolverException {
    List<String> catalogs = new ArrayList<String>();
    catalogs.add(resourceUri("catalog.xml"));
    Resolver resolver = new CatalogResolver(catalogs, new SAXResolver(null));
    ExternalIdentifier xid = new ExternalIdentifier("foo.xml", "http://www.example.com/index.html", "The Great Foo");
    Input input = new Input();
    resolver.resolve(xid, input);
    Assert.assertEquals(input.getUri(), "http://www.example.com/bar.xml");
  }

  static String resourceUri(String fileName) {
    String className = CatalogResolverTest.class.getName();
    int dotIndex = className.lastIndexOf('.');
    String resourceName = className.substring(0, dotIndex + 1).replace('.', '/') + fileName;
    return CatalogResolverTest.class.getClassLoader().getResource(resourceName).toString();
  }
}
