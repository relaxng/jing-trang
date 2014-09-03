package com.thaiopensource.xml.em;

import java.io.IOException;
import java.net.URL;

public class UriEntityManager extends EntityManager {
  public OpenEntity open(ExternalId xid, boolean isParameterEntity, String entityName) throws IOException {
    String systemId = xid.getSystemId();
    String baseUri = xid.getBaseUri();
    URL u;
    if (baseUri != null)
      u = new URL(new URL(baseUri), systemId);
    else
      u = new URL(systemId);
    return open(u);
  }

  public OpenEntity open(String uri) throws IOException {
    return open(new URL(uri));
  }

  private OpenEntity open(URL u) throws IOException {
    return detectEncoding(u.openStream(), u.toString());
  }
}
