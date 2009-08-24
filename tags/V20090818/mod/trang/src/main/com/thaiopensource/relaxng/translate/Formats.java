package com.thaiopensource.relaxng.translate;

import com.thaiopensource.relaxng.input.InputFormat;
import com.thaiopensource.relaxng.input.xml.XmlInputFormat;
import com.thaiopensource.relaxng.input.dtd.DtdInputFormat;
import com.thaiopensource.relaxng.input.parse.sax.SAXParseInputFormat;
import com.thaiopensource.relaxng.input.parse.compact.CompactParseInputFormat;
import com.thaiopensource.relaxng.output.OutputFormat;
import com.thaiopensource.relaxng.output.rnc.RncOutputFormat;
import com.thaiopensource.relaxng.output.xsd.XsdOutputFormat;
import com.thaiopensource.relaxng.output.rng.RngOutputFormat;
import com.thaiopensource.relaxng.output.dtd.DtdOutputFormat;

public class Formats {
  private Formats() {
  }

  static public InputFormat createInputFormat(String name) {
    if (name.equalsIgnoreCase("rng"))
      return new SAXParseInputFormat();
    if (name.equalsIgnoreCase("rnc"))
      return new CompactParseInputFormat();
    if (name.equalsIgnoreCase("dtd"))
      return new DtdInputFormat();
    if (name.equalsIgnoreCase("xml"))
      return new XmlInputFormat();
   return null;
  }

  static public OutputFormat createOutputFormat(String name) {
    if (name.equalsIgnoreCase("dtd"))
      return new DtdOutputFormat();
    else if (name.equalsIgnoreCase("rng"))
      return new RngOutputFormat();
    else if (name.equalsIgnoreCase("xsd"))
      return new XsdOutputFormat();
    else if (name.equalsIgnoreCase("rnc"))
      return new RncOutputFormat();
    return null;
  }
}
