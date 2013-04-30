// Copyright (c) Pengyu Yang. All rights reserved

package org.joyframework;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItemStream;

public class Part {

  final public String ContentType;
  final public String Name;
  final public InputStream Stream;

  public Part(final FileItemStream st) {
    ContentType = st.getContentType();
    Name = st.getName();
    try {
      Stream = st.openStream();
    }
    catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}

