package com.thaiopensource.relaxng.output.dtd;

import java.io.Writer;
import java.io.IOException;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

class LocalOutputDirectory implements OutputDirectory {
  private File mainOutputFile;
  private String lineSeparator;
  private String ext;

  LocalOutputDirectory(File mainOutputFile) {
    this.mainOutputFile = mainOutputFile;
    this.lineSeparator = System.getProperty("line.separator");
    String name = mainOutputFile.getName();
    int dot = name.lastIndexOf('.');
    if (dot > 0)
      ext = name.substring(dot);
    else
      ext = null;
  }

  public Writer open(String sourceUri) throws IOException {
    File file;
    if (sourceUri == MAIN)
      file = mainOutputFile;
    else
      file = new File(mainOutputFile.getParentFile(), mapFilename(sourceUri));
    return new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)));
  }

  public String reference(String fromSourceUri, String toSourceUri) {
    return mapFilename(toSourceUri);
  }

  private String mapFilename(String sourceUri) {
    String filename = sourceUri.substring(sourceUri.lastIndexOf('/') + 1);
    if (ext != null) {
      int dot = filename.lastIndexOf('.');
      if (dot > 0)
        return filename.substring(0, dot) + ext;
    }
    return filename;
  }

  public String getLineSeparator() {
    return lineSeparator;
  }
}
