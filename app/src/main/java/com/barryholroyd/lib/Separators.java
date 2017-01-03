package com.barryholroyd.lib;

import static java.lang.System.out;

/**
 * Create text separators of various sorts.
 *
 * Different levels have different characters and string lengths
 * (decreasing levels have decreasing prominence).
 * <ul>
 *   <li>separators are one-liners.
 *   <li>headers are three-liners.
 * </ul>
 * Externally, use '#' for the highest level separator.
 * <p>
 * Internally, the levels are as follows.
 * <ol>
 *   <li>*
 *   <li>=
 *   <li>-
 * </ol>
 */
public class Separators
{
  /** Selection of separator options. */
  public enum SeparatorStyle { S1, S2, S3 }

  /** Selection of separator options. */
  public enum HeaderStyle { H1, H2, H3 }

  /**
   * Utility method to print out a one-line separator.
   * 
   * @param ss	the style of the separator (char and string length).
   */
  static public void sep(SeparatorStyle ss) {
    switch (ss) {
      case S1: s1(); break;
      case S2: s2(); break;
      case S3: s3(); break;
    }
  }

  /**
   * Utility method to print out a header (three-line separator).
   * 
   * @param ss	the style of the separator (char and string length).
   * @param s	the String to use as the header.
   */
  static public void hdr(HeaderStyle ss, String s) {
    switch (ss) {
      case H1: h1(s); break;
      case H2: h2(s); break;
      case H3: h3(s); break;
    }
  }

  /** Print level 1 separator. */
  static public void s1() { out.print(getS1()); }
  /** Print level 2 separator. */
  static public void s2() { out.print(getS2()); }
  /** Print level 3 separator. */
  static public void s3() { out.print(getS3()); }

  /**
   * Print level 1 header.
   * 
   * @param s	header to print.
   */
  static public void h1(String s) { out.print(getH1(s)); }

  /**
   * Print level 2 header.
   * 
   * @param s	header to print.
   */
  static public void h2(String s) { out.print(getH2(s)); }

  /**
   * Print level 3 header.
   * 
   * @param s	header to print.
   */
  static public void h3(String s) { out.print(getH3(s)); }

  /**
   * Get level 1 separator.
   * 
   * @return separator.
   */
  static public String getS1() { return starOp('*', 79) + "\n"; }
  /**
   * Get level 2 separator.
   * 
   * @return separator.
   */
  static public String getS2() { return starOp('=', 60) + "\n"; }
  /**
   * Get level 3 separator.
   * 
   * @return separator.
   */
  static public String getS3() { return starOp('-', 40) + "\n"; }

  /**
   * Utility method to print out a header (three-line separator).
   * 
   * @param ss	the style of the separator (char and string length).
   * @param s	the String to use as the header.
   * @return    the separator string.
   */
  static public String getHdr(HeaderStyle ss, String s) {
    switch (ss) {
      case H1: return getH1(s);
      case H2: return getH2(s);
      case H3: return getH3(s);
    }
    return null; // for the compiler
  }

  /**
   * Get level 1 header.
   * 
   * @param s	header string to include.
   * @return	header.
   */
  static public String getH1(String s) {
    return String.format("%s%s\n%s", getS1(), s, getS1());
  }
  /**
   * Get level 2 header.
   * 
   * @param s	header string to include.
   * @return	header.
   */
  static public String getH2(String s) {
    return String.format("%s%s\n%s", getS2(), s, getS2());
  }
  /**
   * Get level 3 header.
   * 
   * @param s	header string to include.
   * @return	header.
   */
  static public String getH3(String s) {
    return String.format("%s%s\n%s", getS3(), s, getS3());
  }

  /**
   * Return a string of 'c' characters that is "cnt" long.
   *
   * @param c	the character to use.
   * @param cnt	the number of that character to include.
   * @return    the string.
   */
  static private String starOp(char c, int cnt) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0 ; i < cnt ; i++)
      sb.append(c);
    return sb.toString();
  }
}
