package com.thaiopensource.relaxng;

import org.xml.sax.Locator;
import java.util.Hashtable;

import org.relaxng.datatype.Datatype;

final public class PatternBuilder {
  
  private static final int INIT_SIZE = 256;
  private static final float LOAD_FACTOR = 0.3f;
  private Pattern[] table;
  private int used;
  private int usedLimit;

  private final EmptyPattern empty;
  private final NotAllowedPattern notAllowed;
  private final UnexpandedNotAllowedPattern unexpandedNotAllowed;
  private final TextPattern text;

  private PatternFunction endAttributesFunction;
  private PatternFunction ignoreMissingAttributesFunction;
  private PatternFunction endTagDerivFunction;
  private PatternFunction mixedTextDerivFunction;
  private PatternFunction textOnlyFunction;
  private PatternFunction recoverAfterFunction;

  private Hashtable patternMemoMap = new Hashtable();

  private boolean idTypes;

  public PatternBuilder() {
    init();
    table = null;
    used = 0;
    usedLimit = 0;
    empty = new EmptyPattern();
    notAllowed = new NotAllowedPattern();
    unexpandedNotAllowed = new UnexpandedNotAllowedPattern();
    text = new TextPattern();
  }

  public PatternBuilder(PatternBuilder parent) {
    init();
    // XXX Should we try to clone the parent's patternMemoMap? Tricky.
    table = parent.table;
    if (table != null)
      table = (Pattern[])table.clone();
    used = parent.used;
    usedLimit = parent.usedLimit;
    empty = parent.empty;
    notAllowed = parent.notAllowed;
    unexpandedNotAllowed = parent.unexpandedNotAllowed;
    text = parent.text;
  }

  private void init() {
    endAttributesFunction = new EndAttributesFunction(this);
    ignoreMissingAttributesFunction = new IgnoreMissingAttributesFunction(this);
    endTagDerivFunction = new EndTagDerivFunction(this);
    mixedTextDerivFunction = new MixedTextDerivFunction(this);
    textOnlyFunction = new TextOnlyFunction(this);
    recoverAfterFunction = new RecoverAfterFunction(this);
  }    

  public boolean hasIdTypes() {
    return idTypes;
  }

  Pattern makeEmpty() {
    return empty;
  }
  Pattern makeNotAllowed() {
    return notAllowed;
  }
  Pattern makeUnexpandedNotAllowed() {
    return unexpandedNotAllowed;
  }
  Pattern makeError() {
    return intern(new ErrorPattern());
  }
  Pattern makeAfter(Pattern p1, Pattern p2) {
    if (p1 == notAllowed || p2 == notAllowed)
      return notAllowed;
    return intern(new AfterPattern(p1, p2));
  }
  Pattern makeGroup(Pattern p1, Pattern p2) {
    if (p1 == empty)
      return p2;
    if (p2 == empty)
      return p1;
    if (p1 == notAllowed || p2 == notAllowed)
      return notAllowed;
    if (false && p1 instanceof GroupPattern) {
      GroupPattern sp = (GroupPattern)p1;
      return makeGroup(sp.p1, makeGroup(sp.p2, p2));
    }
    return intern(new GroupPattern(p1, p2));
  }
  Pattern makeInterleave(Pattern p1, Pattern p2) {
    if (p1 == empty)
      return p2;
    if (p2 == empty)
      return p1;
    if (p1 == notAllowed || p2 == notAllowed)
      return notAllowed;
    if (false && p1 instanceof InterleavePattern) {
      InterleavePattern ip = (InterleavePattern)p1;
      return makeInterleave(ip.p1, makeInterleave(ip.p2, p2));
    }
    if (false) {
    if (p2 instanceof InterleavePattern) {
      InterleavePattern ip = (InterleavePattern)p2;
      if (p1.hashCode() > ip.p1.hashCode())
	return makeInterleave(ip.p1, makeInterleave(p1, ip.p2));
    }
    else if (p1.hashCode() > p2.hashCode())
      return makeInterleave(p2, p1);
    }
    return intern(new InterleavePattern(p1, p2));
  }
  Pattern makeText() {
    return text;
  }
  Pattern makeValue(Datatype dt, Object obj) {
    noteDatatype(dt);
    return intern(new ValuePattern(dt, obj));
  }

  Pattern makeData(Datatype dt) {
    noteDatatype(dt);
    return intern(new DataPattern(dt));
  }

  Pattern makeDataExcept(Datatype dt, Pattern except, Locator loc) {
    noteDatatype(dt);
    return intern(new DataExceptPattern(dt, except, loc));
  }

  Pattern makeChoice(Pattern p1, Pattern p2) {
    if (p1 == notAllowed)
      return p2;
    if (p2 == notAllowed)
      return p1;
    if (p1 == empty) {
      if (p2.isNullable())
	return p2;
      // Canonicalize position of notAllowed.
      return makeChoice(p2, p1);
    }
    if (p2 == empty && p1.isNullable())
      return p1;
    if (p1 instanceof ChoicePattern) {
      ChoicePattern cp = (ChoicePattern)p1;
      return makeChoice(cp.p1, makeChoice(cp.p2, p2));
    }
    if (p1 instanceof AfterPattern) {
      Pattern tem = p2.combineAfter(this, (AfterPattern)p1);
      if (tem != null)
        return tem;
    }
    else if (p2.containsChoice(p1))
      return p2;
    if (false) {
    if (p2 instanceof ChoicePattern) {
      ChoicePattern cp = (ChoicePattern)p2;
      if (p1.hashCode() > cp.p1.hashCode())
	return makeChoice(cp.p1, makeChoice(p1, cp.p2));
    }
    else if (p1.hashCode() > p2.hashCode())
      return makeChoice(p2, p1);
    }
    return intern(new ChoicePattern(p1, p2));
  }

  Pattern makeOneOrMore(Pattern p) {
    if (p == text
	|| p == empty
	|| p == notAllowed
	|| p instanceof OneOrMorePattern)
      return p;
    return intern(new OneOrMorePattern(p));
  }

  Pattern makeOptional(Pattern p) {
    return makeChoice(p, empty);
  }

  Pattern makeZeroOrMore(Pattern p) {
    return makeOptional(makeOneOrMore(p));
  }

  Pattern makeList(Pattern p, Locator loc) {
    if (p == notAllowed)
      return p;
    return intern(new ListPattern(p, loc));
  }

  Pattern makeElement(NameClass nameClass, Pattern content, Locator loc) {
    return intern(new ElementPattern(nameClass, content, loc));
  }

  Pattern makeAttribute(NameClass nameClass, Pattern value, Locator loc) {
    if (value.isNotAllowed())
      return value;
    return intern(new AttributePattern(nameClass, value, loc));
  }

  private void noteDatatype(Datatype dt) {
    if (dt.getIdType() != Datatype.ID_TYPE_NULL)
      idTypes = true;
  }

  private Pattern intern(Pattern p) {
    int h;

    if (table == null) {
      table = new Pattern[INIT_SIZE];
      usedLimit = (int)(INIT_SIZE * LOAD_FACTOR);
      h = firstIndex(p);
    }
    else {
      for (h = firstIndex(p); table[h] != null; h = nextIndex(h)) {
	if (p.samePattern(table[h]))
	  return table[h];
      }
    }
    if (used >= usedLimit) {
      // rehash
      Pattern[] oldTable = table;
      table = new Pattern[table.length << 1];
      for (int i = oldTable.length; i > 0;) {
	--i;
	if (oldTable[i] != null) {
	  int j;
	  for (j = firstIndex(oldTable[i]); table[j] != null; j = nextIndex(j))
	    ;
	  table[j] = oldTable[i];
	}
      }
      for (h = firstIndex(p); table[h] != null; h = nextIndex(h))
	;
      usedLimit = (int)(table.length * LOAD_FACTOR);
    }
    used++;
    table[h] = p;
    return p;
  }

  private int firstIndex(Pattern p) {
    return p.patternHashCode() & (table.length - 1);
  }

  private int nextIndex(int i) {
    return i == 0 ? table.length - 1 : i - 1;
  }

  void printStats() {
    System.err.println(used + " distinct patterns");
  }

  PatternMemo getPatternMemo(Pattern p) {
    PatternMemo memo = (PatternMemo)patternMemoMap.get(p);
    if (memo == null) {
      memo = new PatternMemo(p, this);
      patternMemoMap.put(p, memo);
    }
    return memo;
  }

  PatternFunction getEndAttributesFunction() {
    return endAttributesFunction;
  }

  PatternFunction getIgnoreMissingAttributesFunction() {
    return ignoreMissingAttributesFunction;
  }

  PatternFunction getEndTagDerivFunction() {
    return endTagDerivFunction;
  }

  PatternFunction getMixedTextDerivFunction() {
    return mixedTextDerivFunction;
  }

  PatternFunction getTextOnlyFunction() {
    return textOnlyFunction;
  }

  PatternFunction getRecoverAfterFunction() {
    return recoverAfterFunction;
  }
}
