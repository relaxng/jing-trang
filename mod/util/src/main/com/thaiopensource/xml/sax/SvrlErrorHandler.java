package com.thaiopensource.xml.sax;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.*;

public class SvrlErrorHandler implements ErrorHandler {
    private static final OutputStream DEFAULT_OUTPUT = System.out;
    private static final String ENCODING = "UTF-8";
    private static final String XPATH_SEPARATOR = " - ";

    private PrintWriter writer;

    public SvrlErrorHandler() {
        this(DEFAULT_OUTPUT);
    }

    public SvrlErrorHandler(OutputStream output) {
        writer = new PrintWriter(output);
        beginSVRL();
    }

    public SvrlErrorHandler(File file) {

        try {
            writer = new PrintWriter(file, ENCODING);
            beginSVRL();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        endSVRL();
        writer.close();
    }

    private void beginSVRL() {
        writer.println("<?xml version=\"1.0\" encoding=\"" + ENCODING + "\"?>");
        writer.println("<svrl:schematron-output xmlns:svrl=\"http://purl.oclc.org/dsdl/svrl\">");
        writer.println("\t<svrl:active-pattern/>");
        writer.println("\t<svrl:fired-rule context=\"\"/>");
        writer.flush();
    }

    private void endSVRL() {
        writer.println("</svrl:schematron-output>");
        writer.flush();
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        writeFailedAssert(exception.getMessage(), "WARNING");
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        writeFailedAssert(exception.getMessage(), "ERROR");
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        writeFailedAssert(exception.getMessage(), "FATAL");
    }

    private void writeFailedAssert(String message, String role) {
        int xpathSeparatorIndex = message.indexOf(XPATH_SEPARATOR);
        if (xpathSeparatorIndex == -1) {
            System.err.println("ERROR: message \"" + message + "\" does not contain XPath separator \"" + XPATH_SEPARATOR + "\"");
            return;
        }

        String xpath = message.substring(0, xpathSeparatorIndex);
        String text = message.substring(xpathSeparatorIndex + XPATH_SEPARATOR.length());
        writer.println("\t<svrl:failed-assert test=\"RelaxNG\" role=\"" + role + "\" location=\"" + xpath + "\">");
        writer.println("\t\t<svrl:text>" + xmlEncode(text) + "</svrl:text>");
        writer.println("\t</svrl:failed-assert>");
        writer.flush();
    }

    private static String xmlEncode(String text) {
        StringBuilder encoded = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            switch (c) {
                case '&':
                    encoded.append("&amp;");
                    break;
                case '<':
                    encoded.append("&lt;");
                    break;
                case '>':
                    encoded.append("&gt;");
                    break;
                case '"':
                    encoded.append("&quot;");
                    break;
                case '\'':
                    encoded.append("&apos;");
                    break;
                default:
                    encoded.append(c);
                    break;
            }
        }

        return encoded.toString();
    }
}
