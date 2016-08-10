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
    private Element curElement;

    private class Element {
        private Element parent;
        private String name;

        private boolean isClosed;
        private int position;
        private Map<String, Integer> sib2pos;

        public Element(Element parent, String name) {
            this.parent = parent;
            this.name = name;

            isClosed = false;
            position = 1;
            sib2pos = new HashMap<String, Integer>();
        }

        public Element sibling(String sibName) {
            isClosed = false;

            if (name.equals(sibName)) {
                position++;
            } else {
                sib2pos.put(name, position);
                name = sibName;
                position = 1;
                Integer sibPosition = sib2pos.remove(sibName);
                if (sibPosition != null) position += sibPosition;
            }

            return this;
        }

        public void close() {
            isClosed = true;
        }

        public String toXPath() {
            StringBuilder xpath = new StringBuilder("/" + name + "[" + position + "]");
            if (parent != null) xpath.insert(0, parent.toXPath());
            return xpath.toString();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            Element element = (Element) object;

            if (position != element.position) return false;
            if (parent != null ? !parent.equals(element.parent) : element.parent != null) return false;
            return name.equals(element.name);
        }

        @Override
        public int hashCode() {
            int result = parent != null ? parent.hashCode() : 0;
            result = 31 * result + name.hashCode();
            result = 31 * result + position;
            return result;
        }
    }

    public RngXpathValidator(Pattern pattern, ValidatorPatternBuilder builder, ErrorHandler eh) {
        super(pattern, builder, eh);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        super.startPrefixMapping(prefix, uri);
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        curElement = null;
        super.endDocument();
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (curElement == null) curElement = new Element(null, qName);
        else if (curElement.isClosed) curElement = curElement.sibling(qName);
        else curElement = new Element(curElement, qName);

        super.startElement(namespaceURI, localName, qName, atts);
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (curElement.isClosed) curElement = curElement.parent;
        curElement.close();

        super.endElement(namespaceURI, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
    }

    @Override
    public void reset() {
        curElement = null;
        super.reset();
    }

    @Override
    protected void check(boolean ok) throws SAXException {
        if (!ok) eh.error(new SAXParseException(curElement.toXPath() + " - " + matcher.getErrorMessage(), locator));
    }
}
