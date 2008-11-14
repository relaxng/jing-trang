package com.thaiopensource.xml.em;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileEntityManager extends EntityManager {
  public OpenEntity open(ExternalId xid, boolean isParameterEntity, String entityName) throws IOException {
    String systemId = xid.getSystemId();
    File file = new File(systemId);
    if (!file.isAbsolute()) {
      String baseUri = xid.getBaseUri();
      if (baseUri != null) {
	String dir = new File(baseUri).getParent();
	if (dir != null)
	  file = new File(dir, systemId);
      }
    }
    return openFile(file);
  }

  public OpenEntity open(String systemId) throws IOException {
    return openFile(new File(systemId));
  }

  private OpenEntity openFile(File file) throws IOException {
    return detectEncoding(new FileInputStream(file), file.toString());
  }


}
