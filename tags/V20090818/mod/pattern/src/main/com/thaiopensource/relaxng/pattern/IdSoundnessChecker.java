package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;
import com.thaiopensource.xml.util.StringSplitter;
import org.relaxng.datatype.Datatype;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdSoundnessChecker {
  private final IdTypeMap idTypeMap;
  private final ErrorHandler eh;
  private final Map<String, Entry> map = new HashMap<String, Entry>();

  private static class Entry {
    Locator idLoc;
    List<LocatorImpl> idrefLocs;
    boolean hadId;
  }

  public IdSoundnessChecker(IdTypeMap idTypeMap, ErrorHandler eh) {
    this.idTypeMap = idTypeMap;
    this.eh = eh;
  }

  public void reset() {
    map.clear();
  }

  public void endDocument() throws SAXException {
    for (String token : map.keySet()) {
      Entry entry = map.get(token);
      if (!entry.hadId) {
        for (LocatorImpl idrefLoc : entry.idrefLocs)
          error("missing_id", token, idrefLoc);
      }
    }
  }

  public void attribute(Name elementName, Name attributeName, String value, Locator locator)
          throws SAXException {
    int idType = idTypeMap.getIdType(elementName, attributeName);
    if (idType != Datatype.ID_TYPE_NULL) {
      String[] tokens = StringSplitter.split(value);
      switch (idType) {
      case Datatype.ID_TYPE_ID:
        if (tokens.length == 1)
          id(tokens[0], locator);
        else if (tokens.length == 0)
          error("id_no_tokens", locator);
        else
          error("id_multiple_tokens", locator);
        break;
      case Datatype.ID_TYPE_IDREF:
        if (tokens.length == 1)
          idref(tokens[0], locator);
        else if (tokens.length == 0)
          error("idref_no_tokens", locator);
        else
          error("idref_multiple_tokens", locator);
        break;
      case Datatype.ID_TYPE_IDREFS:
        if (tokens.length > 0) {
          for (int j = 0; j < tokens.length; j++)
            idref(tokens[j], locator);
        }
        else
          error("idrefs_no_tokens", locator);
        break;
      }
    }
  }

  private void id(String token, Locator locator) throws SAXException {
    Entry entry = map.get(token);
    if (entry == null) {
      entry = new Entry();
      map.put(token, entry);
    }
    else if (entry.hadId) {
      error("duplicate_id", token, locator);
      error("first_id", token, entry.idLoc);
      return;
    }
    entry.idLoc = new LocatorImpl(locator);
    entry.hadId = true;
  }

  private void idref(String token, Locator locator) {
    Entry entry = map.get(token);
    if (entry == null) {
      entry = new Entry();
      map.put(token, entry);
    }
    if (entry.hadId)
      return;
    if (entry.idrefLocs == null)
      entry.idrefLocs = new ArrayList<LocatorImpl>();
    entry.idrefLocs.add(new LocatorImpl(locator));
  }

  private void error(String key, Locator locator) throws SAXException {
    eh.error(new SAXParseException(SchemaBuilderImpl.localizer.message(key), locator));
  }

  private void error(String key, String arg, Locator locator) throws SAXException {
    eh.error(new SAXParseException(SchemaBuilderImpl.localizer.message(key, arg),
                                   locator));
  }
}
