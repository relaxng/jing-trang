package com.thaiopensource.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class Service<T> {
  private final Class<T> serviceClass;
  private final Enumeration<URL> configFiles;
  private Iterator<String> classNames = null;
  private final List<T> providers = new ArrayList<T>();
  private Loader loader;

  private class ProviderIterator implements Iterator<T> {
    private int nextIndex = 0;

    public boolean hasNext() {
      return nextIndex < providers.size() || moreProviders();
    }

    public T next() {
      try {
	return providers.get(nextIndex++);
      }
      catch (IndexOutOfBoundsException e) {
	throw new NoSuchElementException();
      }
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private static class Singleton<T> implements Enumeration<T> {
    private T obj;
    private Singleton(T obj) {
      this.obj = obj;
    }

    public boolean hasMoreElements() {
      return obj != null;
    }

    public T nextElement() {
      if (obj == null)
	throw new NoSuchElementException();
      T tem = obj;
      obj = null;
      return tem;
    }
  }

  // JDK 1.1
  private static class Loader {
    Enumeration<URL> getResources(String resName) {
      ClassLoader cl = Loader.class.getClassLoader();
      URL url;
      if (cl == null)
	url = ClassLoader.getSystemResource(resName);
      else
	url = cl.getResource(resName);
      return new Singleton<URL>(url);
    }

    Class<?> loadClass(String name) throws ClassNotFoundException {
      return Class.forName(name);
    }
  }

  // JDK 1.2+
  private static class Loader2 extends Loader {
    private ClassLoader cl;

    Loader2() {
      cl = Loader2.class.getClassLoader();
      // If the thread context class loader has the class loader
      // of this class as an ancestor, use the thread context class
      // loader.  Otherwise, the thread context class loader
      // probably hasn't been set up properly, so don't use it.
      ClassLoader clt = Thread.currentThread().getContextClassLoader();
      for (ClassLoader tem = clt; tem != null; tem = tem.getParent())
	if (tem == cl) {
	  cl = clt;
	  break;
	}
    }

    Enumeration<URL> getResources(String resName) {
      try {
        Enumeration<URL> resources = cl.getResources(resName);
        if (resources.hasMoreElements())
	  return resources;
        // Some application servers apparently do not implement findResources
        // in their class loaders, so fall back to getResource.
        return new Singleton<URL>(cl.getResource(resName));
      }
      catch (IOException e) {
	return new Singleton<URL>(null);
      }
    }

    Class<?> loadClass(String name) throws ClassNotFoundException {
      return Class.forName(name, true, cl);
    }
  }

  static public <T> Service<T> newInstance(Class<T> cls) {
    return new Service<T>(cls);
  }

  private Service(Class<T> cls) {
    try {
      loader = new Loader2();
    }
    catch (NoSuchMethodError e) {
      loader = new Loader();
    }
    serviceClass = cls;
    String resName = "META-INF/services/" + serviceClass.getName();
    configFiles = loader.getResources(resName);
  }

  public Iterator<T> getProviders() {
    return new ProviderIterator();
  }

  synchronized private boolean moreProviders() {
    for (;;) {
      while (classNames == null) {
	if (!configFiles.hasMoreElements())
	  return false;
	classNames = parseConfigFile(configFiles.nextElement());
      }
      while (classNames.hasNext()) {
	String className = classNames.next();
	try {
	  Class<?> cls = loader.loadClass(className);
	  Object obj = cls.newInstance();
	  if (serviceClass.isInstance(obj)) {
	    providers.add(serviceClass.cast(obj));
	    return true;
	  }
	}
	catch (ClassNotFoundException e) { }
	catch (InstantiationException e) { }
	catch (IllegalAccessException e) { }
	catch (LinkageError e) { }
      }
      classNames = null;
    }
  }

  private static final int START = 0;
  private static final int IN_NAME = 1;
  private static final int IN_COMMENT = 2;

  private static Iterator<String> parseConfigFile(URL url) {
    try {
      InputStream in = url.openStream();
      Reader r;
      try {
	r = new InputStreamReader(in, "UTF-8");
      }
      catch (UnsupportedEncodingException e) {
	r = new InputStreamReader(in, "UTF8");
      }
      r = new BufferedReader(r);
      List<String> tokens = new ArrayList<String>();
      StringBuilder tokenBuf = new StringBuilder();
      int state = START;
      for (;;) {
	int n = r.read();
	if (n < 0)
	  break;
	char c = (char)n;
	switch (c) {
	case '\r':
	case '\n':
	  state = START;
	  break;
	case ' ':
	case '\t':
	  break;
	case '#':
	  state = IN_COMMENT;
	  break;
	default:
	  if (state != IN_COMMENT) {
	    state = IN_NAME;
	    tokenBuf.append(c);
	  }
	  break;
	}
	if (tokenBuf.length() != 0 && state != IN_NAME) {
	  tokens.add(tokenBuf.toString());
	  tokenBuf.setLength(0);
	}
      }
      if (tokenBuf.length() != 0)
	tokens.add(tokenBuf.toString());
      return tokens.iterator();
    }
    catch (IOException e) {
      return null;
    }
  }


  public static void main(String[] args) throws ClassNotFoundException {
    Service<?> svc = Service.newInstance(Class.forName(args[0]));
    for (Iterator<?> iter = svc.getProviders(); iter.hasNext();)
      System.out.println(iter.next().getClass().getName());
  }
}
