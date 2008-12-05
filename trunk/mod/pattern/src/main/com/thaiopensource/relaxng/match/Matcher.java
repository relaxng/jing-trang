package com.thaiopensource.relaxng.match;

import com.thaiopensource.xml.util.Name;

import java.util.Set;

/**
 * Represents the state of matching an XML document against a RELAX NG pattern.
 * The XML document is considered as a linear sequence of events of different
 * kinds.  For each kind of event <var>E</var> in the sequence, a call must be made
 * to a corresponding method <code>match<var>E</var></code> on the
 * <var>Matcher</var> object. The kinds of event are:
 * <p/>
 * <ul>
 * <li>StartDocument</li>
 * <li>StartTagOpen</li>
 * <li>AttributeName</li>
 * <li>AttributeValue</li>
 * <li>StartTagClose</li>
 * <li>Text</li>
 * <li>EndTag</li>
 * <li>EndDocument</li>
 * </ul>
 * <p/>
 * <p>The method calls must occur in an order corresponding to a well-formed XML
 * document.  In a well-formed document the sequence of events matches
 * the following grammar:
 * <p/>
 * <pre>
 * document ::= StartDocument <var>element</var> EndDocument
 * element ::= <var>startTag</var> <var>child</var>* EndTag
 * startTag ::= StartTagOpen <var>attribute</var>* StartTagClose
 * attribute ::= AttributeName AttributeValue
 * child ::= <var>element</var> | Text
 * </pre>
 * <p/>
 * <p>Text events must be maximal.  Two consecutive Text events are not allowed.
 * Matching text is special, and is done with <code>matchTextBeforeStartTag</code>
 * or <code>matchTextBeforeEndTag</code>, according as the Text event is
 * followed by a StartTagOpen or an EndTag event.  Callers may optionally choose
 * to optimize calls to <code>matchTextBeforeStartTag</code>
 * or <code>matchTextBeforeEndTag</code> into calls to <code>matchUntypedText</code>,
 * but this is only allowed when <code>isTextTyped</code> returns false.
 * <p/>
 * <p>Each method <code>match<var>E</var></code> returns false if matching
 * the event against the document resulted in an error and true otherwise.
 * If it returned false, then the error message can be obtained using
 * <code>getErrorMessage</code>.  In either case, the state of the
 * <code>Matcher</code> changes so the <code>Matcher</code> is prepared
 * to match the next event.
 * <p/>
 * <p>The <code>copy()</code> and <code>equals()</code> methods allow
 * applications to perform incremental revalidation.
 */
public interface Matcher {
  /**
   * Return a copy of the current <code>Matcher</code>.
   * Future changes to the state of the copy will not affect this and vice-versa.
   *
   * @return a <code>Matcher</code> that is a copy of this
   */
  Matcher copy();

  /**
   * Return a copy of this <code>Matcher</code> reset to its starting state.
   * @return a new <code>Matcher</code>
   */
  Matcher start();

  /**
   * Test whether obj is an equivalent <code>Matcher</code>.
   * @return true if they are obj is known to be equivalent, false otherwise
   */
  boolean equals(Object obj);

  /**
   * Return a hashCode for the Matcher. This is consistent with equals.
   * @return a hash code
   */
  int hashCode();

  /**
   * Match a StartDocument event. This can only generate an error if the schema was
   * equivalent to <code>notAllowed</code>.
   *
   * @return false if there was an error, true otherwise
   */
  boolean matchStartDocument();

  /**
   * Match an EndDocument event.
   *
   * @return false if there was an error, true otherwise
   */
  boolean matchEndDocument();

  /**
   * Match a StartTagOpen event.
   * @param name the element name
   * @param qName the element qName (may be empty or null if unknown)
   * @param context the MatchContext
   * @return false if there was an error, true otherwise
   */
  boolean matchStartTagOpen(Name name, String qName, MatchContext context);

  /**
   * Match an AttributeName event.
   *
   * @param name the attribute name
   * @param qName the attribute qName (may be empty or null if unknown)
   * @param context the MatchContext
   * @return false if there was an error, true otherwise
   */
  boolean matchAttributeName(Name name, String qName, MatchContext context);

  /**
   * Match an AttributeValue event.
   * The MatchContext must include all the namespace declarations in the start-tag
   * including those that lexically follow the attribute.
   *
   * @param value the attribute value, normalized in accordance with XML 1.0
   * @param name  the attribute name (included for use in error messages)
   * @param qName the attribute qName (included for use in error messages)
   * @param context the MatchContext
   * @return false if there was an error, true otherwise
   */
  boolean matchAttributeValue(String value, Name name, String qName, MatchContext context);

  /**
   * Match a StartTagClose event.  This corresponds to the  <code>&gt;</code> character
   * that ends the start-tag).
   * It may cause an error if there are required attributes that have not been matched.
   * The parameters are used to generate error messages.
   *
   * @param name the element name
   * @param qName the element qName (may be null or empty)
   * @param context the MatchContext
   * @return false if there was an error, true otherwise
   */
  boolean matchStartTagClose(Name name, String qName, MatchContext context);

  /**
   * Match a Text event that occurs immediately before an EndTag event.
   * All text between two tags must be collected together: consecutive
   * calls to <code>matchTextBeforeEndTag</code>/<code>matchTextBeforeStartTag</code> are not
   * allowed unless separated by a call to <code>matchStartTagOpen</code> or <code>matchEndTag</code>.
   * Calls to <code>matchTextBeforeEndTag</code> can sometimes be optimized into
   * calls to <code>matchUntypedText</code>.
   *
   * @param string the text to be matched
   * @param name the name of the parent element (i.e. the name of the element of the following
   * EndTag event)
   * @param qName the qName of the parent element
   * @param context a match context
   * @return false if there was an error, true otherwise
   */
  boolean matchTextBeforeEndTag(String string, Name name, String qName, MatchContext context);

  /**
   * Match a Text event that occurs immediately before a StartTagOpen event.
   * All text between two tags must be collected together: consecutive
   * calls to <code>matchTextBeforeEndTag</code>/<code>matchTextBeforeStartTag</code> are not
   * allowed unless separated by a call to <code>matchStartTagOpen</code> or <code>matchEndTag</code>.
   * Calls to <code>matchTextBeforeStartTag</code> can sometimes be optimized into
   * calls to <code>matchUntypedText</code>.
   *
   * @param string the text to be matched
   * @param context a match context
   * @return false if there was an error, true otherwise
   */
  boolean matchTextBeforeStartTag(String string, MatchContext context);

  /**
   * An optimization of <code>matchTextBeforeStartTag</code>/<code>matchTextBeforeEndTag</code>.
   * Unlike these functions, <code>matchUntypedText</code> does not
   * need to examine the text.
   * If <code>isTextTyped</code> returns false, then in this state
   * text that consists of whitespace (' ', '\r', '\n', '\t') may be ignored and text that contains
   * non-whitespace characters may be processed using <code>matchUntypedText</code>.
   * Furthermore it is not necessary to collect up all the text between tags;
   * consecutive calls to <code>matchUntypedText</code> are allowed.
   * <code>matchUntypedText</code> must not be used unless <code>isTextTyped</code>
   * returns false.
   * @param context a match context
   * @return false if there was an error, true otherwise
   */
  boolean matchUntypedText(MatchContext context);

  /**
   * Return true if text may be typed in the current state, false otherwise.
   * If text may be typed, then a call to <code>matchText</code> must <em>not</em> be optimized
   * to <code>matchUntypedText</code>.
   *
   * @return true if text may be typed, false otherwise
   */
  boolean isTextTyped();

  /**
   * Match an EndTag event.
   *
   * @param name the element name
   * @param qName the elememt qname (may be empty or null if unknown)
   * @param context a match context
   * @return false if there was an error, true otherwise
   */
  boolean matchEndTag(Name name, String qName, MatchContext context);

  /**
   * Return the current error message.
   * The current error message is changed by any <code>match<var>E</var></code> method
   * that returns false.  Initially, the current error message is null.
   *
   * @return a string with the current error message, or null if there has not yet
   *         been an error.
   */
  String getErrorMessage();

  /**
   * Return true if the document is valid so far.
   * A document is valid so far if and only if no errors have yet been
   * encountered.
   *
   * @return true if the document is valid so far, false otherwise
   */
  boolean isValidSoFar();

  /**
   * Return a NameClass containing the names of elements whose start-tags are valid
   * in the current state. This must be called only in a state in
   * which a call to <code>matchStartTagOpen</code> would be allowed.
   *
   * @return a NameClass contains the names of elements whose start-tags are possible
   */
  NameClass possibleStartTagNames();

  /**
   * Return a NameClass containing the names of attributes that are valid
   * in the current state.  This must be called only in a state in
   * which a call to <code>matchAttributeName</code> would be allowed.
   *
   * @return a NameClass containing the names of attributes that are possible
   */
  NameClass possibleAttributeNames();

  /**
   * Return a Set containing the names of attributes that are required in the
   * current state. This must be called only in a state in
   * which a call to <code>matchAttributeName</code> would be allowed. Note
   * that in a schema such as attribute foo|bar { text } neither foo nor
   * bar are considered required attributes; an attribute name x is required
   * only if every matching pattern contains an attribute named x. Similarly,
   * this function provides no information about wildcard attribute names.
   * @return a non-null Set each member of which is a non-null Name corresponding
   * to the name of a required attribute
   * @see Name
   */
  Set<Name> requiredAttributeNames();
  Set<Name> requiredElementNames();
}
