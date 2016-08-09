package com.thaiopensource.validate.rng.impl;

import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.relaxng.pattern.ValidatorPatternBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Relax NG validator which outputs XPath locators.
 */
public class RngXpathValidator extends RngValidator {

    private class Element {
        private Element parent;
        private String name;
        private boolean isClosed;

        public Element(Element parent, String name) {
            this.parent = parent;
            this.name = name;
            isClosed = false;
        }

        public void close() {
            isClosed = true;
        }

        public String toXPath(Map<Element, Integer> element2numOccurrences) {
            StringBuilder xpath = new StringBuilder("/" + name + "[" + element2numOccurrences.get(this) + "]");
            if (parent != null) xpath.insert(0, parent.toXPath(element2numOccurrences));
            return xpath.toString();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            Element element = (Element) object;

            if (parent != null ? !parent.equals(element.parent) : element.parent != null) return false;
            return name.equals(element.name);
        }

        @Override
        public int hashCode() {
            int result = parent != null ? parent.hashCode() : 0;
            result = 31 * result + name.hashCode();
            return result;
        }
    }

    private Map<Element, Integer> element2numOccurrences;
    private Element current;

    public RngXpathValidator(Pattern pattern, ValidatorPatternBuilder builder, ErrorHandler eh) {
        super(pattern, builder, eh);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        super.startPrefixMapping(prefix, uri);
    }

    @Override
    public void startDocument() throws SAXException {
        element2numOccurrences = new HashMap<Element, Integer>();
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        element2numOccurrences = null;
        current = null;
        super.endDocument();
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (current == null) current = new Element(null, qName);
        else if (!current.isClosed) current = new Element(current, qName);
        else current = new Element(current.parent, qName);

        int numOccurrences = element2numOccurrences.getOrDefault(current, 0);
        element2numOccurrences.put(current, numOccurrences + 1);

        super.startElement(namespaceURI, localName, qName, atts);
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (current.isClosed) current = current.parent;
        current.close();

        super.endElement(namespaceURI, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
    }

    @Override
    public void reset() {
        element2numOccurrences = null;
        current = null;
        super.reset();
    }

    @Override
    protected void check(boolean ok) throws SAXException {
        if (!ok) eh.error(new SAXParseException(current.toXPath(element2numOccurrences) + " - " + matcher.getErrorMessage(), locator));
    }
}
