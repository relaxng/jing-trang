package com.thaiopensource.xml.em;

import com.thaiopensource.xml.util.EncodingMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This class is used by the parser to access external entities.
 */
public abstract class EntityManager {
  /**
   * Opens an external entity with the specified external identifier.
   */
  public abstract OpenEntity open(ExternalId xid, boolean isParameterEntity, String entityName) throws IOException;

  /**
   * Open the top-level entity.
   * @param systemId
   * @return
   * @throws IOException
   */
  public abstract OpenEntity open(String systemId) throws IOException;

  protected OpenEntity detectEncoding(InputStream input, String systemId) throws IOException {
    EncodingDetectInputStream in = new EncodingDetectInputStream(input);
    String enc = in.detectEncoding();
    String javaEnc = EncodingMap.getJavaName(enc);
    return new OpenEntity(new BufferedReader(new InputStreamReader(in, javaEnc)),
			  systemId,
			  systemId,
			  enc);
  }
}
