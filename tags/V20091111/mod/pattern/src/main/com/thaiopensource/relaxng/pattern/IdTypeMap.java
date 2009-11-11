package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

public interface IdTypeMap {
  int getIdType(Name elementName, Name attributeName);
}
