package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *  Test NameClassNormalizer.
 */
public class NameClassNormalizerTest {
  @DataProvider(name = "pairs")
  Object[][] createPairs() {
    final Name foo = new Name("", "foo");
    final String ns1 = "http://www.example.com/1";
    final Name ns1foo = new Name(ns1, "foo");
    Map<String, HashSet<String>> ns1Map = new HashMap<String, HashSet<String>>();
    ns1Map.put(ns1, new HashSet<String>());
    final NormalizedNameClass ns1NNC = new NormalizedNsNameClass(emptyNameSet(), ns1Map);
    final NormalizedNameClass anyNNC = new NormalizedAnyNameClass(emptyNameSet(),
                                                                  emptyStringSet(),
                                                                  emptyNameSet());
    final NormalizedNsNameClass fooNNC = new NormalizedNsNameClass(Collections.singleton(foo),
                                                                   emptyMap());
    final NormalizedNsNameClass ns1fooNNC = new NormalizedNsNameClass(Collections.singleton(ns1foo),
                                                                      emptyMap());
    final NormalizedNsNameClass emptyNNC = new NormalizedNsNameClass(emptyNameSet(),
                                                                     emptyMap());
    return new Object[][] {
            { new SimpleNameClass(foo), fooNNC},
            { new ChoiceNameClass(new SimpleNameClass(foo), new SimpleNameClass(foo)),
              fooNNC },
            { new AnyNameClass(), anyNNC },
            { new AnyNameExceptNameClass(new AnyNameClass()), emptyNNC },
            { new NsNameClass(ns1), ns1NNC },
            { new ChoiceNameClass(new SimpleNameClass(foo), new AnyNameClass()), anyNNC },
            { new ChoiceNameClass(new NsNameClass(ns1), new AnyNameClass()), anyNNC },
            { new NsNameExceptNameClass(ns1, new AnyNameClass()), emptyNNC },
            { new NsNameExceptNameClass(ns1, new NsNameClass(ns1)), emptyNNC },
            { new NsNameExceptNameClass(ns1, new SimpleNameClass(foo)), ns1NNC },
            { new NsNameExceptNameClass(ns1, new NsNameExceptNameClass(ns1, new SimpleNameClass(ns1foo))),
              ns1fooNNC }
    };
  }
  @Test(dataProvider = "pairs")
  public void testNormalize(NameClass nc, NormalizedNameClass nnc) {
    Assert.assertEquals(new NameClassNormalizer(nc).normalize(), nnc);
  }

  static private Set<Name> emptyNameSet() {
    return Collections.emptySet();
  }

  static private Set<String> emptyStringSet() {
    return Collections.emptySet();
  }
  static private Map<String,HashSet<String>> emptyMap() {
    return Collections.emptyMap();
  }
}
