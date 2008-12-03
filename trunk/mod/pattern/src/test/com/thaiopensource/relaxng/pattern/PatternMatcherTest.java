package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.relaxng.match.Matcher;
import com.thaiopensource.relaxng.sax.Context;
import com.thaiopensource.xml.util.Name;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Test PatternMatcher.
 */
public class PatternMatcherTest extends SchemaPatternBuilder {
  final SchemaPatternBuilder spb = new SchemaPatternBuilder();
  static private final Name root = new Name("", "root");

  @DataProvider(name = "startTagPairs")
  Object[][] startTagPairs() {
    final Name foo = new Name("", "foo");
    final Name bar = new Name("", "bar");
    Set<Name> nameSet = new HashSet<Name>();
    nameSet.add(foo);
    nameSet.add(bar);
    final NormalizedNameClass foobarNNC = new NormalizedNsNameClass(nameSet, Collections.EMPTY_MAP);
    final Locator loc = new LocatorImpl();
    return new Object[][] {
            { rootMatcher(makeChoice(makeElement(new SimpleNameClass(foo), makeEmpty(), loc),
                                     makeElement(new SimpleNameClass(bar), makeEmpty(), loc))),
              foobarNNC }

    };
  }

  @Test(dataProvider = "startTagPairs")
  public void testPossibleStartTagNames(Matcher matcher, NormalizedNameClass nnc) {
    Assert.assertEquals(matcher.possibleStartTagNames(), nnc);
  }

  private Matcher rootMatcher(Pattern start) {
    Matcher matcher = new PatternMatcher(start, new ValidatorPatternBuilder(this));
    Assert.assertTrue(matcher.matchStartDocument());
    return matcher;
  }

  @DataProvider(name = "attributePairs")
  Object[][] attributePairs() {
    final Name foo = new Name("", "foo");
    final Name bar = new Name("", "bar");
    Set<Name> nameSet = new HashSet<Name>();
    nameSet.add(foo);
    nameSet.add(bar);
    final NormalizedNameClass foobarNNC = new NormalizedNsNameClass(nameSet, Collections.EMPTY_MAP);
    final Locator loc = new LocatorImpl();
    return new Object[][] {
            { rootAttributeMatcher(makeElement(new SimpleNameClass(root),
                                               makeGroup(makeAttribute(new SimpleNameClass(foo), makeText(), loc),
                                                         makeAttribute(new SimpleNameClass(bar), makeText(), loc)),
                                               loc)),
              foobarNNC }                    
    };
  }

  private Matcher rootAttributeMatcher(Pattern start) {
    Matcher matcher = rootMatcher(start);
    Assert.assertTrue(matcher.matchStartTagOpen(root, "", new Context()));
    return matcher;
  }

  @Test(dataProvider = "attributePairs")
  public void testPossibleAttributeNames(Matcher matcher, NormalizedNameClass nnc) {
    Assert.assertEquals(matcher.possibleAttributeNames(), nnc);
  }
}
