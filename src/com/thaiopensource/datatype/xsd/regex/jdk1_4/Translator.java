package com.thaiopensource.datatype.xsd.regex.jdk1_4;

import com.thaiopensource.util.Utf16;
import com.thaiopensource.util.Localizer;
import com.thaiopensource.datatype.xsd.InvalidRegexException;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

class Translator {
  private String regExp;
  private int pos = 0;
  private int length;
  private char curChar;
  private StringBuffer result = new StringBuffer();

  static private final String categories = "LMNPZSC";
  static private final String subCategories = "LuLlLtLmLoMnMcMeNdNlNoPcPdPsPePiPfPoZsZlZpSmScSkSoCcCfCoCn";

  static private final int NONBMP_MIN = 0x10000;
  static private final int NONBMP_MAX = 0x10FFFF;
  static private final char SURROGATE2_MIN = '\uDC00';
  static private final char SURROGATE2_MAX = '\uDFFF';

  static Localizer localizer = new Localizer(Translator.class);

  static private final String[] blockNames = {
    "BasicLatin",
    "Latin-1Supplement",
    "LatinExtended-A",
    "LatinExtended-B",
    "IPAExtensions",
    "SpacingModifierLetters",
    "CombiningDiacriticalMarks",
    "Greek",
    "Cyrillic",
    "Armenian",
    "Hebrew",
    "Arabic",
    "Syriac",
    "Thaana",
    "Devanagari",
    "Bengali",
    "Gurmukhi",
    "Gujarati",
    "Oriya",
    "Tamil",
    "Telugu",
    "Kannada",
    "Malayalam",
    "Sinhala",
    "Thai",
    "Lao",
    "Tibetan",
    "Myanmar",
    "Georgian",
    "HangulJamo",
    "Ethiopic",
    "Cherokee",
    "UnifiedCanadianAboriginalSyllabics",
    "Ogham",
    "Runic",
    "Khmer",
    "Mongolian",
    "LatinExtendedAdditional",
    "GreekExtended",
    "GeneralPunctuation",
    "SuperscriptsandSubscripts",
    "CurrencySymbols",
    "CombiningMarksforSymbols",
    "LetterlikeSymbols",
    "NumberForms",
    "Arrows",
    "MathematicalOperators",
    "MiscellaneousTechnical",
    "ControlPictures",
    "OpticalCharacterRecognition",
    "EnclosedAlphanumerics",
    "BoxDrawing",
    "BlockElements",
    "GeometricShapes",
    "MiscellaneousSymbols",
    "Dingbats",
    "BraillePatterns",
    "CJKRadicalsSupplement",
    "KangxiRadicals",
    "IdeographicDescriptionCharacters",
    "CJKSymbolsandPunctuation",
    "Hiragana",
    "Katakana",
    "Bopomofo",
    "HangulCompatibilityJamo",
    "Kanbun",
    "BopomofoExtended",
    "EnclosedCJKLettersandMonths",
    "CJKCompatibility",
    "CJKUnifiedIdeographsExtensionA",
    "CJKUnifiedIdeographs",
    "YiSyllables",
    "YiRadicals",
    "HangulSyllables",
    "HighSurrogates",
    "HighPrivateUseSurrogates",
    "LowSurrogates",
    // "PrivateUse", excluded because 3.1 adds non-BMP ranges
    "CJKCompatibilityIdeographs",
    "AlphabeticPresentationForms",
    "ArabicPresentationForms-A",
    "CombiningHalfMarks",
    "CJKCompatibilityForms",
    "SmallFormVariants",
    "ArabicPresentationForms-B",
    "Specials",
    "HalfwidthandFullwidthForms",
    "Specials"
  };


  /**
   * Names of blocks including ranges outside the BMP.
   */
  static private final String[] specialBlockNames = {
    "OldItalic",
    "Gothic",
    "Deseret",
    "ByzantineMusicalSymbols",
    "MusicalSymbols",
    "MathematicalAlphanumericSymbols",
    "CJKUnifiedIdeographsExtensionB",
    "CJKCompatibilityIdeographsSupplement",
    "Tags",
    "PrivateUse"
  };

  /**
   * CharClass for each block name in specialBlockNames.
   */
  static private final CharClass[] specialBlockCharClasses = {
    new CharRange(0x10300, 0x1032F),
    new CharRange(0x10330, 0x1034F),
    new CharRange(0x10400, 0x1044F),
    new CharRange(0x1D000, 0x1D0FF),
    new CharRange(0x1D100, 0x1D1FF),
    new CharRange(0x1D400, 0x1D7FF),
    new CharRange(0x20000, 0x2A6D6),
    new CharRange(0x2F800, 0x2FA1F),
    new CharRange(0xE0000, 0xE007F),
    new Union(new CharClass[] {
      new CharRange(0xE000, 0xF8FF),
      new CharRange(0xF0000, 0xFFFFD),
      new CharRange(0x100000, 0x10FFFD)
    })
  };

  static private final CharClass DOT = new Complement(new Union(new CharClass[] { new SingleChar('\n'), new SingleChar('\r') }));

  static private final CharClass ESC_d = new Property("Nd");

  static private final CharClass ESC_D = new Complement(ESC_d);

  static private final CharClass ESC_W = new Union(new CharClass[] {new Property("P"), new Property("Z"), new Property("C")});

  static private final CharClass ESC_w = new Complement(ESC_W);

  static private final CharClass ESC_s = new Union(new CharClass[] {
    new SingleChar(' '),
    new SingleChar('\n'),
    new SingleChar('\r'),
    new SingleChar('\t')
  });

  static private final CharClass ESC_S = new Complement(ESC_s);

  static private final CharClass ESC_i = makeCharClass(NamingExceptions.NMSTRT_CATEGORIES,
                                                       NamingExceptions.NMSTRT_INCLUDES,
                                                       NamingExceptions.NMSTRT_EXCLUDE_RANGES);

  static private final CharClass ESC_I = new Complement(ESC_i);

  static private final CharClass ESC_c = makeCharClass(NamingExceptions.NMCHAR_CATEGORIES,
                                                       NamingExceptions.NMCHAR_INCLUDES,
                                                       NamingExceptions.NMCHAR_EXCLUDE_RANGES);

  static private final CharClass ESC_C = new Complement(ESC_c);

  static private final char EOS = '\0';

  private Translator(String regExp) {
    this.regExp = regExp;
    this.length = regExp.length();
    advance();
  }

  static public String translate(String regExp) throws InvalidRegexException {
    Translator tr = new Translator(regExp);
    tr.translateTop();
    return tr.result.toString();
  }

  private void advance() {
    if (pos < length)
      curChar = regExp.charAt(pos++);
    else {
      pos++;
      curChar = EOS;
    }
  }

  private void translateTop() throws InvalidRegexException {
    translateRegExp();
    if (curChar != EOS)
      throw makeException("expected_eos");
  }

  private void translateRegExp() throws InvalidRegexException {
    translateBranch();
    while (curChar == '|') {
      copyCurChar();
      translateBranch();
    }
  }

  private void translateBranch() throws InvalidRegexException {
    while (translateAtom())
      translateQuantifier();
  }

  private void translateQuantifier() throws InvalidRegexException {
    switch (curChar) {
    case '*':
    case '?':
    case '+':
      copyCurChar();
      return;
    case '{':
      copyCurChar();
      translateQuantity();
      expect('}');
      copyCurChar();
    }
  }

  private void translateQuantity() throws InvalidRegexException {
    // XXX check that the lower bound is <= upper bound
    translateQuantExact();
    if (curChar == ',') {
      copyCurChar();
      if (curChar != '}') {
        advance();
        translateQuantExact();
      }
    }
  }

  private void translateQuantExact() throws InvalidRegexException {
    do {
      if ("0123456789".indexOf(curChar) < 0)
        throw makeException("expected_digit");
      copyCurChar();
    } while (curChar != ',' && curChar != '}');
  }

  private void copyCurChar() throws InvalidRegexException {
    result.append(curChar);
    advance();
  }

  static final int NONE = -1;
  static final int SOME = 0;
  static final int ALL = 1;

  static final String SURROGATES1_CLASS = "[\uD800-\uDBFF]";
  static final String SURROGATES2_CLASS = "[\uDC00-\uDFFF]";
  static final String NOT_ALLOWED_CLASS = "\u0000";

  static final class Range implements Comparable {
    private final int min;
    private final int max;

    Range(int min, int max) {
      this.min = min;
      this.max = max;
    }

    int getMin() {
      return min;
    }

    int getMax() {
      return max;
    }

    public int compareTo(Object o) {
      Range other = (Range)o;
      if (this.min < other.min)
        return -1;
      if (this.min > other.min)
        return 1;
      if (this.max > other.max)
        return -1;
      if (this.max < other.max)
        return 1;
      return 0;
    }
  }

  static abstract class CharClass  {

    private final int containsBmp;
    // if it contains ALL and containsBmp != NONE, then the generated class for containsBmp must
    // contain all the high surrogates
    private final int containsNonBmp;

    protected CharClass(int containsBmp, int containsNonBmp) {
      this.containsBmp = containsBmp;
      this.containsNonBmp = containsNonBmp;
    }

    int getContainsBmp() {
      return containsBmp;
    }

    int getContainsNonBmp() {
      return containsNonBmp;
    }

    void output(StringBuffer buf) {
      switch (containsNonBmp) {
      case NONE:
        if (containsBmp == NONE)
          buf.append(NOT_ALLOWED_CLASS);
        else
          outputBmp(buf);
        break;
      case ALL:
        buf.append('(');
        if (containsBmp == NONE) {
          buf.append(SURROGATES1_CLASS);
          buf.append(SURROGATES2_CLASS);
        }
        else {
          outputBmp(buf);
          buf.append(SURROGATES2_CLASS);
          buf.append('?');
        }
        buf.append(')');
        break;
      case SOME:
        buf.append('(');
        boolean needSep = false;
        if (containsBmp != NONE) {
          needSep = true;
          outputBmp(buf);
        }
        List ranges = new Vector();
        addNonBmpRanges(ranges);
        sortRangeList(ranges);
        String hi = highSurrogateRanges(ranges);
        if (hi.length() > 0) {
          if (needSep)
            buf.append('|');
          else
            needSep = true;
          buf.append('[');
          for (int i = 0, len = hi.length(); i < len; i += 2) {
            char min = hi.charAt(i);
            char max = hi.charAt(i + 1);
            if (min == max)
              buf.append(min);
            else {
              buf.append(min);
              buf.append('-');
              buf.append(max);
            }
          }
          buf.append(']');
          buf.append(SURROGATES2_CLASS);
        }
        String lo = lowSurrogateRanges(ranges);
        for (int i = 0, len = lo.length(); i < len; i += 3) {
          if (needSep)
            buf.append('|');
          else
            needSep = true;
          buf.append(lo.charAt(i));
          char min = lo.charAt(i + 1);
          char max = lo.charAt(i + 2);
          if (min == max)
            buf.append(min);
          else {
            buf.append('[');
            buf.append(min);
            buf.append('-');
            buf.append(max);
            buf.append(']');
          }
        }
        if (!needSep)
          buf.append(NOT_ALLOWED_CLASS);
        buf.append(')');
        break;
      }
    }

    static String highSurrogateRanges(List ranges) {
      StringBuffer highRanges = new StringBuffer();
      for (int i = 0, len = ranges.size(); i < len; i++) {
        Range r = (Range)ranges.get(i);
        char min1 = Utf16.surrogate1(r.getMin());
        char min2 = Utf16.surrogate2(r.getMin());
        char max1 = Utf16.surrogate1(r.getMax());
        char max2 = Utf16.surrogate2(r.getMax());
        if (min2 != SURROGATE2_MIN)
          min1++;
        if (max2 != SURROGATE2_MAX)
          max1--;
        if (max1 >= min1) {
          highRanges.append(min1);
          highRanges.append(max1);
        }
      }
      return highRanges.toString();
    }

    static String lowSurrogateRanges(List ranges) {
      StringBuffer lowRanges = new StringBuffer();
      for (int i = 0, len = ranges.size(); i < len; i++) {
        Range r = (Range)ranges.get(i);
        char min1 = Utf16.surrogate1(r.getMin());
        char min2 = Utf16.surrogate2(r.getMin());
        char max1 = Utf16.surrogate1(r.getMax());
        char max2 = Utf16.surrogate2(r.getMax());
        if (min1 == max1) {
          if (min2 != SURROGATE2_MIN || max2 != SURROGATE2_MAX) {
            lowRanges.append(min1);
            lowRanges.append(min2);
            lowRanges.append(max2);
          }
        }
        else {
          if (min2 != SURROGATE2_MIN) {
            lowRanges.append(min1);
            lowRanges.append(min2);
            lowRanges.append(SURROGATE2_MAX);
          }
          if (max2 != SURROGATE2_MAX) {
            lowRanges.append(max1);
            lowRanges.append(SURROGATE2_MIN);
            lowRanges.append(max2);
          }
        }
      }
      return lowRanges.toString();
    }

    void outputBmp(StringBuffer buf) {
      buf.append('[');
      inClassOutputBmp(buf);
      buf.append(']');
    }

    abstract void inClassOutputBmp(StringBuffer buf);

    // must not call if containsBmp == ALL
    void outputComplementBmp(StringBuffer buf) {
      if (containsBmp == NONE)
        buf.append("[\u0000-\uFFFF]");
      else {
        buf.append("[^");
        inClassOutputBmp(buf);
        buf.append(']');
      }
    }

    int singleChar() {
      return -1;
    }

    void addNonBmpRanges(List ranges) {
    }


    static void sortRangeList(List ranges) {
      Collections.sort(ranges);
      int toIndex = 0;
      int fromIndex = 0;
      int len = ranges.size();
      while (fromIndex < len) {
        Range r = (Range)ranges.get(fromIndex);
        int min = r.getMin();
        int max = r.getMax();
        while (++fromIndex < len) {
          Range r2 = (Range)ranges.get(fromIndex);
          if (r2.getMin() > max + 1)
            break;
          if (r2.getMax() > max)
            max = r2.getMax();
        }
        if (max != r.getMax())
          r = new Range(min, r.getMax());
        ranges.set(toIndex++, r);
      }
      while (len > toIndex)
        ranges.remove(--len);
    }

  }

  static class SingleChar extends CharClass {
    private char c;
    SingleChar(char c) {
      super(SOME, NONE);
      this.c = c;
    }

    int singleChar() {
      return c;
    }

    void outputBmp(StringBuffer buf) {
      inClassOutputBmp(buf);
    }

    void inClassOutputBmp(StringBuffer buf) {
      if (isJavaMetaChar(c))
        buf.append('\\');
      buf.append((char)c);
    }

  }

  static class WideSingleChar extends CharClass {
    private int c;

    WideSingleChar(int c) {
      super(NONE, SOME);
      this.c = c;
    }

    void inClassOutputBmp(StringBuffer buf) {
      throw new RuntimeException("BMP output botch");
    }

    int singleChar() {
      return c;
    }

    void addNonBmpRanges(List ranges) {
      ranges.add(new Range(c, c));
    }
  }

  static class CharRange extends CharClass {
    private int lower;
    private int upper;

    CharRange(int lower, int upper) {
      super(lower < NONBMP_MIN ? SOME : NONE,
            // don't use ALL here, because that requires that the BMP class contains high surrogates
            upper >= NONBMP_MIN ? SOME : NONE);
      this.lower = lower;
      this.upper = upper;
    }

    void inClassOutputBmp(StringBuffer buf) {
      if (lower >= NONBMP_MIN)
        throw new RuntimeException("BMP output botch");
      if (isJavaMetaChar((char)lower))
        buf.append('\\');
      buf.append((char)lower);
      buf.append('-');
      if (upper < NONBMP_MIN) {
        if (isJavaMetaChar((char)upper))
          buf.append('\\');
        buf.append((char)upper);
      }
      else
        buf.append('\uFFFF');
    }

    void addNonBmpRanges(List ranges) {
      if (upper >= NONBMP_MIN)
        ranges.add(new Range(lower < NONBMP_MIN ? NONBMP_MIN : lower, upper));
    }
  }

  static class Property extends CharClass {
    private String name;

    Property(String name) {
      super(SOME, NONE);
      this.name = name;
    }

    void outputBmp(StringBuffer buf) {
      inClassOutputBmp(buf);
    }

    void inClassOutputBmp(StringBuffer buf) {
      buf.append("\\p{");
      buf.append(name);
      buf.append('}');
    }

    void outputComplementBmp(StringBuffer buf) {
      buf.append("\\P{");
      buf.append(name);
      buf.append('}');
    }
  }

  static class Subtraction extends CharClass {
    private CharClass cc1;
    private CharClass cc2;
    Subtraction(CharClass cc1, CharClass cc2) {
      // min corresponds to intersection
      // complement corresponds to negation
      super(Math.min(cc1.getContainsBmp(), -cc2.getContainsBmp()),
            Math.min(cc1.getContainsNonBmp(), -cc2.getContainsNonBmp()));
      this.cc1 = cc1;
      this.cc2 = cc2;
    }

    void inClassOutputBmp(StringBuffer buf) {
      cc1.inClassOutputBmp(buf);
      buf.append("&&");
      cc2.outputComplementBmp(buf);
    }

    void addNonBmpRanges(List ranges) {
      List posList = new Vector();
      cc1.addNonBmpRanges(posList);
      List negList = new Vector();
      cc2.addNonBmpRanges(posList);
      sortRangeList(posList);
      sortRangeList(negList);
      Iterator negIter = negList.iterator();
      Range negRange;
      if (negIter.hasNext())
        negRange = (Range)negIter.next();
      else
        negRange = null;
      for (int i = 0, len = posList.size(); i < len; i++) {
        Range posRange = (Range)posList.get(i);
        while (negRange != null && negRange.getMax() < posRange.getMin()) {
          if (negIter.hasNext())
            negRange = (Range)negIter.next();
          else
            negRange = null;
        }
        // if negRange != null, negRange.max >= posRange.min
        int min = posRange.getMin();
        while (negRange != null && negRange.getMin() <= posRange.getMax()) {
          if (min < negRange.getMin()) {
            ranges.add(new Range(min, negRange.getMin() - 1));
          }
          min = negRange.getMax() + 1;
          if (min > posRange.getMax())
            break;
          if (negIter.hasNext())
            negRange = (Range)negIter.next();
          else
            negRange = null;
        }
        if (min <= posRange.getMax())
          ranges.add(new Range(min, posRange.getMax()));
      }
    }
  }

  static class Union extends CharClass {
    private List members;

    Union(CharClass[] v) {
      this(toList(v));
    }

    static private List toList(CharClass[] v) {
      List members = new Vector();
      for (int i = 0; i < v.length; i++)
        members.add(v[i]);
      return members;
    }

    Union(List members) {
      super(computeContainsBmp(members), computeContainsNonBmp(members));
      this.members = members;
    }

    void inClassOutputBmp(StringBuffer buf) {
      for (int i = 0, len = members.size(); i < len; i++) {
        CharClass cc = (CharClass)members.get(i);
        if (cc.getContainsBmp() != NONE)
          cc.inClassOutputBmp(buf);
      }
    }

    void addNonBmpRanges(List ranges) {
      for (int i = 0, len = members.size(); i < len; i++)
        ((CharClass)members.get(i)).addNonBmpRanges(ranges);
    }

    private static int computeContainsBmp(List members) {
      int ret = NONE;
      for (int i = 0, len = members.size(); i < len; i++)
        ret = Math.max(ret, ((CharClass)members.get(i)).getContainsBmp());
      return ret;
    }

    private static int computeContainsNonBmp(List members) {
      int ret = NONE;
      for (int i = 0, len = members.size(); i < len; i++)
        ret = Math.max(ret, ((CharClass)members.get(i)).getContainsNonBmp());
      return ret;
    }
  }

  static class Complement extends CharClass {
    private CharClass cc;
    Complement(CharClass cc) {
      super(-cc.getContainsBmp(), -cc.getContainsNonBmp());
      this.cc = cc;
    }

    void outputBmp(StringBuffer buf) {
      if (cc.getContainsBmp() == NONE)
        super.outputBmp(buf);
      else
        inClassOutputBmp(buf);
    }

    void outputComplementBmp(StringBuffer buf) {
      cc.outputBmp(buf);
    }

    void inClassOutputBmp(StringBuffer buf) {
      if (cc.getContainsBmp() == NONE)
        buf.append("\u0000-\uFFFF");
      else {
        buf.append("[^");
        cc.inClassOutputBmp(buf);
        buf.append(']');
      }
    }

    void addNonBmpRanges(List ranges) {
      List tem = new Vector();
      cc.addNonBmpRanges(tem);
      sortRangeList(tem);
      int c = NONBMP_MIN;
      for (int i = 0, len = tem.size(); i < len; i++) {
        Range r = (Range)tem.get(i);
        if (r.getMin() > c)
          ranges.add(new Range(c, r.getMin() - 1));
        c = r.getMax() + 1;
      }
      if (c != NONBMP_MAX + 1)
        ranges.add(new Range(c, NONBMP_MAX));
    }
  }

  boolean translateAtom() throws InvalidRegexException {
    switch (curChar) {
    case EOS:
    case '^':
    case '?':
    case '*':
    case '+':
    case ')':
    case '{':
    case '}':
    case '|':
    case ']':
      break;
    case '(':
      copyCurChar();
      translateRegExp();
      expect(')');
      copyCurChar();
      return true;
    case '\\':
      advance();
      parseEsc().output(result);
      return true;
    case '[':
      advance();
      parseCharClassExpr().output(result);
      return true;
    case '.':
      DOT.output(result);
      advance();
      return true;
    case '$':
      result.append('\\');
      // fall through
    default:
      copyCurChar();
      return true;
    }
    return false;
  }


  static private CharClass makeCharClass(String categories, String includes, String excludeRanges) {
    List includeList = new Vector();
    for (int i = 0, len = categories.length(); i < len; i += 2)
      includeList.add(new Property(categories.substring(i, i + 2)));
    for (int i = 0, len = includes.length(); i < len; i++) {
      int j = i + 1;
      for (; j < len && includes.charAt(j) - includes.charAt(i) == j - i; j++)
        ;
      --j;
      if (i == j - 1)
        --j;
      if (i == j)
        includeList.add(new SingleChar(includes.charAt(i)));
      else
        includeList.add(new CharRange(includes.charAt(i), includes.charAt(j)));
      i = j;
    }
    List excludeList = new Vector();
    for (int i = 0, len = excludeRanges.length(); i < len; i += 2) {
      char min = excludeRanges.charAt(i);
      char max = excludeRanges.charAt(i + 1);
      if (min == max)
        excludeList.add(new SingleChar(min));
      else if (min == max - 1) {
        excludeList.add(new SingleChar(min));
        excludeList.add(new SingleChar(max));
      }
      else
        excludeList.add(new CharRange(min, max));
    }
    return new Subtraction(new Union(includeList), new Union(excludeList));
  }

  private CharClass parseEsc() throws InvalidRegexException {
    switch (curChar) {
    case 'n':
      advance();
      return new SingleChar('\n');
    case 'r':
      advance();
      return new SingleChar('\r');
    case 't':
      advance();
      return new SingleChar('\t');
    case '\\':
    case '|':
    case '.':
    case '^':
    case '?':
    case '*':
    case '+':
    case '(':
    case ')':
    case '{':
    case '}':
    case '[':
    case ']':
      break;
    case 's':
      advance();
      return ESC_s;
    case 'S':
      advance();
      return ESC_S;
    case 'i':
      advance();
      return ESC_i;
    case 'I':
      advance();
      return ESC_I;
    case 'c':
      advance();
      return ESC_c;
    case 'C':
      advance();
      return ESC_C;
    case 'd':
      advance();
      return ESC_d;
    case 'D':
      advance();
      return ESC_D;
    case 'w':
      advance();
      return ESC_w;
    case 'W':
      advance();
      return ESC_W;
    case 'p':
      advance();
      return parseProp();
    case 'P':
      advance();
      return new Complement(parseProp());
    default:
      throw makeException("bad_escape");
    }
    CharClass tem = new SingleChar(curChar);
    advance();
    return tem;
  }

  private CharClass parseProp() throws InvalidRegexException {
    expect('{');
    int start = pos;
    for (;;) {
      advance();
      if (curChar == '}')
        break;
      if (!isAsciiAlnum(curChar) && curChar != '-')
        expect('}');
    }
    String propertyName = regExp.substring(start, pos - 1);
    advance();
    switch (propertyName.length()) {
    case 0:
      throw makeException("empty_property_name");
    case 2:
      if (subCategories.indexOf(propertyName) < 0)
        throw makeException("bad_category");
      // fall through
    case 1:
      if (categories.indexOf(propertyName.charAt(0)) < 1)
        throw makeException("bad_category", propertyName);
      return new Property(propertyName);
    default:
      if (!propertyName.startsWith("Is"))
        break;
      String blockName = propertyName.substring(2);
      for (int i = 0; i < specialBlockNames.length; i++)
        if (blockName.equals(specialBlockNames[i]))
          return specialBlockCharClasses[i];
      if (!isBlock(blockName))
        throw makeException("bad_block_name", blockName);
      return new Property( "In" + blockName);
    }
    throw makeException("bad_property_name", propertyName);
  }

  static private boolean isBlock(String name) {
    for (int i = 0; i < blockNames.length; i++)
      if (name.equals(blockNames[i]))
        return true;
    return false;
  }

  static private boolean isAsciiAlnum(char c) {
    if ('a' <= c && c <= 'z')
      return true;
    if ('A' <= c && c <= 'Z')
      return true;
    if ('0' <= c && c <= '9')
      return true;
    return false;
  }

  void expect(char c) throws InvalidRegexException {
    if (curChar != c)
      throw makeException("expected", new String(new char[]{c}));
  }

  private CharClass parseCharClassExpr() throws InvalidRegexException {
    boolean compl;
    if (curChar == '^') {
      advance();
      compl = true;
    }
    else
      compl = false;
    List members = new Vector();
    do {
      CharClass lower = parseCharClassEscOrXmlChar();
      members.add(lower);
      if (curChar == '-') {
        advance();
        if (curChar == '[')
          break;
        CharClass upper = parseCharClassEscOrXmlChar();
        if (lower.singleChar() < 0 || upper.singleChar() < 0)
          throw makeException("multi_range");
        if (lower.singleChar() > upper.singleChar())
          throw makeException("invalid_range");
        members.set(members.size() - 1,
                    new CharRange(lower.singleChar(), upper.singleChar()));
        if (curChar == '-') {
          advance();
          expect('[');
          break;
        }
      }
    } while (curChar != ']');
    CharClass result;
    if (members.size() == 1)
      result = (CharClass)members.get(0);
    else
      result = new Union(members);
    if (curChar == '[') {
      advance();
      result = new Subtraction(result, parseCharClassExpr());
      expect(']');
    }
    advance();
    if (compl)
      return new Complement(result);
    return result;
  }

  CharClass parseCharClassEscOrXmlChar() throws InvalidRegexException {
    switch (curChar) {
    case EOS:
      expect(']');
      break;
    case '\\':
      advance();
      return parseEsc();
    case '[':
    case ']':
    case '-':
      throw makeException("should_quote", new String(new char[]{curChar}));
    }
    CharClass tem;
    if (Utf16.isSurrogate1(curChar)) {
      char c1 = curChar;
      advance();
      if (!Utf16.isSurrogate2(curChar))
        throw makeException("invalid_surrogate");
      tem = new WideSingleChar(Utf16.scalarValue(c1, curChar));
    }
    else
      tem = new SingleChar(curChar);
    advance();
    return tem;
  }

  private InvalidRegexException makeException(String key) {
    return new InvalidRegexException(localizer.message(key), pos - 1);
  }

  private InvalidRegexException makeException(String key, String arg) {
    return new InvalidRegexException(localizer.message(key, arg), pos - 1);
  }

  static private boolean isJavaMetaChar(char c) {
    switch (c) {
    case '\\':
    case '^':
    case '?':
    case '*':
    case '+':
    case '(':
    case ')':
    case '{':
    case '}':
    case '|':
    case '[':
    case ']':
    case '-':
    case '&':
    case '$':
    case '.':
      return true;
    }
    return false;
  }

}
