package com.thaiopensource.validate.auto;

import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.AbstractSchemaReader;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.ResolverFactory;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.InputStream;

public class AutoSchemaReader extends AbstractSchemaReader {
  private final SchemaReceiverFactory srf;

  public AutoSchemaReader() {
    this(new SchemaReceiverLoader());
  }

  public AutoSchemaReader(SchemaReceiverFactory srf) {
    this.srf = srf == null ? new SchemaReceiverLoader() : srf;
  }

  public Schema createSchema(SAXSource source, PropertyMap properties)
          throws IOException, SAXException, IncorrectSchemaException {
    if (properties.get(SchemaReceiverFactory.PROPERTY) != srf) {
      PropertyMapBuilder builder = new PropertyMapBuilder(properties);
      builder.put(SchemaReceiverFactory.PROPERTY, srf);
      properties = builder.toPropertyMap();
    }
    SAXResolver resolver = ResolverFactory.createResolver(properties);
    InputSource in = resolver.open(source.getInputSource());
    InputSource in2 = new InputSource();
    in2.setSystemId(in.getSystemId());
    in2.setPublicId(in.getPublicId());
    in2.setEncoding(in.getEncoding());
    Rewindable rewindable;
    if (in.getCharacterStream() != null) {
      RewindableReader rewindableReader = new RewindableReader(in.getCharacterStream());
      in.setCharacterStream(rewindableReader);
      in2.setCharacterStream(rewindableReader);
      rewindable = rewindableReader;    
    }
    else {
      InputStream byteStream = in.getByteStream();
      RewindableInputStream rewindableByteStream = new RewindableInputStream(byteStream);
      in.setByteStream(rewindableByteStream);
      in2.setByteStream(rewindableByteStream);
      rewindable = rewindableByteStream;
    }
    SchemaReceiver sr = new AutoSchemaReceiver(properties, rewindable);
    XMLReader xr = source.getXMLReader();
    if (xr == null)
      xr = resolver.createXMLReader();
    ErrorHandler eh = properties.get(ValidateProperty.ERROR_HANDLER);
    if (eh != null)
      xr.setErrorHandler(eh);
    SchemaFuture sf = sr.installHandlers(xr);
    try {
      try {
        xr.parse(in);
        return sf.getSchema();
      }
      catch (ReparseException e) {
        rewindable.rewind();
        rewindable.willNotRewind();
        return e.reparse(new SAXSource(xr, in2));
      }
      finally {
        rewindable.willNotRewind();
      }
    }
    catch (SAXException e) {
      // Work around broken SAX parsers that catch and wrap runtime exceptions thrown by handlers
      Exception nested = e.getException();
      if (nested instanceof RuntimeException)
        sf.unwrapException((RuntimeException)nested);
      throw e;
    }
    catch (RuntimeException e) {
      throw sf.unwrapException(e);
    }
  }

  public Option getOption(String uri) {
    return srf.getOption(uri);
  }
}
