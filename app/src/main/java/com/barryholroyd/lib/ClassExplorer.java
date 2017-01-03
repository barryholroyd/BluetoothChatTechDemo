package com.barryholroyd.lib;

import java.util.List;
import java.util.Arrays;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import com.barryholroyd.lib.Separators;

/**
 * Print out information about a class.
 * <p>
 * Output is printed out using log(). By default, log()
 * uses System.out.println(), but that can be overridden
 * by extending ClassExplorer (e.g., for use with Android).
 *
 * The constructor can take either an object instance or class.
 */
public class ClassExplorer
{
  /** Label to use for the display */
  final private String label;

  /** Class of interest. */
  final private Class clazz;

  /** recurse through super classes */
  public boolean opt_recursive	= false;

  /** do not display super class members */
  public boolean opt_declared	= true;

  /** display interfaces */
  public boolean opt_interfaces	= false;

  /** display fields */
  public boolean opt_fields	= false;

  /** display methods */
  public boolean opt_methods	= false;

  /**
   * Constructor taking a class.
   *
   * @param _label	the label to use as a header.
   * @param _clazz	the class of interest.
   */
  public ClassExplorer(String _label, Class _clazz) {
    label = _label;
    clazz = _clazz;
  }

  /**
   * Constructor taking an object instance.
   *
   * @param _label	the label to use as a header.
   * @param _obj	the object of interest.
   */
  public ClassExplorer(String _label, Object _obj) {
    this(_label, _obj.getClass());
  }

  /** Display the list of flags and their values. */
  public void opts() {
    Separators.h2("Options:");
    log(String.format("  %-16s %b\n", "opt_recursive:", opt_recursive));
    log(String.format("  %-16s %b\n", "opt_declared:", opt_declared));
    log(String.format("  %-16s %b\n", "opt_interfaces:", opt_interfaces));
    log(String.format("  %-16s %b\n", "opt_fields:", opt_fields));
    log(String.format("  %-16s %b\n", "opt_methods:", opt_methods));
  }

  /** Display the class information. */
  public void display() {
    Separators.h2("Class Hierarchy: " + label);
    prHierarchy(clazz);
    if (!opt_recursive)	prClassSingle();
    else		prClassAll();
  }

  /** Print a concise version of the class hierarchy. */
  private void prHierarchy(Class clazz) {
    log(String.format("Class: %s", clazz.getName()));
    for (Class sc = clazz.getSuperclass() ; sc != null ;
               sc = sc.getSuperclass()) {
      log("  Superclass: " + sc.getName());
    }
  }

  /** Print information for a single class. */
  private void prClassSingle() {
    if (opt_interfaces)	prInterfaces(clazz);
    if (opt_fields)	prFields(clazz);
    if (opt_methods)	prMethods(clazz);
  }

  /** Print information all all classes in the hierarchy. */
  private void prClassAll() {
    for (Class sc = clazz ; sc != null ; sc = sc.getSuperclass()) {
      log("");
      Separators.h3(String.format("%s", sc.getName()));
      if (opt_interfaces)	prInterfaces(sc);
      if (opt_fields)		prFields(sc);
      if (opt_methods)		prMethods(sc);
    }
  }

  /**
   * Print the list of interfaces.
   *
   * TBD: Use getInterfaces() to get more detailed
   * information about each interface.
   *
   * @param clazz the class of interest.
   */
  private void prInterfaces(Class clazz) {
    log("Interfaces:");
    Type[] types = clazz.getGenericInterfaces();
    if (types.length == 0) {
      log("  <none>");
    }
    else {
      List<Type> typesList = Arrays.asList(types);
//      typesList.sort((a,b) -> a.getTypeName().compareTo(b.getTypeName()));
//      for (Type type : typesList) {
//        log(String.format("  %s", type.getTypeName()));
//      }
    }
  }
  
  /**
   * Print the list of fields.
   *
   * @param clazz the class of interest.
   */
  private void prFields(Class clazz) {
    Field[] fields;
    if (opt_declared) {
      log("Fields (declared)");
      fields = clazz.getDeclaredFields();
    }
    else {
      log("Fields:");
      fields = getPublicFields(clazz);
    }

    if (fields.length == 0) {
      log("  <none>");
    }
    else {
      List<Field> fieldsList= Arrays.asList(fields);
//      fieldsList.sort((a,b) -> a.getName().compareTo(b.getName()));
//      for (Field field : fieldsList) {
//        log(String.format("  %s %s",
//          field.getName(), field.getGenericType().getTypeName()));
//      }
    }
  }
  
  /**
   * Print the list of methods.
   *
   * @param clazz the class of interest.
   */
  private void prMethods(Class clazz) {
    Method[] methods;
    if (opt_declared) {
      log("Methods (declared):");
      methods = clazz.getDeclaredMethods();
    }
    else {
      log("Methods:");
      methods = getPublicMethods(clazz);
    }

    if (methods.length == 0) {
      log("  <none>");
    }
    else {
      List<Method> methodsList = Arrays.asList(methods);

	// I have to provide a comparator here.

//      methodsList.sort((a,b) -> a.getName().compareTo(b.getName()));
      for (Method method : methodsList) {
        log(String.format("  %s", method.getName()));
      }
    }
  }
  
  /**
   * Get the list of parameters for the specified method.
   *
   * @param method the method of interest.
   */
  private String getParams(Method method) {
    StringBuffer sb = new StringBuffer();
    Type[] types = method.getGenericParameterTypes();
    int last = types.length - 1;
    for (int i = 0  ; i < types.length ; i++) {
//      sb.append(types[i].getTypeName());
      if (i < last)
        sb.append(", ");
    }
    return sb.toString();
  }

  /**
   * Return an array containing the public fields for an object instance.
   * 
   * Included primarily as a hook for nominal unit testing. Probably
   * cleaner without this.
   * TBD: delete this method.
   *
   * @param clazz the class of interest.
   * @return	  the array of Methods.
   */
  static public Field[] getPublicFields(Class clazz) {
    return clazz.getFields();
  }

  /**
   * Return an array containing the public methods for an object instance.
   *
   * Included primarily as a hook for nominal unit testing. Probably
   * cleaner without this.
   * TBD: delete this method.
   * 
   * @param clazz the class of interest.
   * @return	  the array of Methods.
   */
  static public Method[] getPublicMethods(Class clazz) {
    return clazz.getMethods();
  }

  /**
   * Print output to standard out.
   * <p>
   * Override this method to send the output to another
   * destination. E.g., for Android it can be sent to the
   * Android log.
   *
   * @param s String to be displayed.
   */
  protected void log(String s) {
    System.out.println(s);
  }
}
