/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2022, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/
package us.fatehi.utility;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.regex.Pattern;

@UtilityMarker
public final class Utility {

  public static String commonPrefix(final String string1, final String string2) {
    if (string1 == null || string2 == null) {
      return "";
    }
    final int index = indexOfDifference(string1, string2);
    if (index < 1) {
      return "";
    } else {
      return string1.substring(0, index).toLowerCase();
    }
  }

  /**
   * Roughly converts database object names so that they can be compared with others in a
   * case-insensitive way. This code is not meant to "correct" from a Unicode perspective, but a
   * quick and dirty way of stripping out quote characters, and lower-casing them for comparison.
   *
   * @param text Text to convert
   * @return Text that can be compared
   */
  public static String convertForComparison(final String text) {
    if (text == null || text.length() == 0) {
      return "";
    }

    final StringBuilder builder = new StringBuilder(text.length());
    for (int i = 0; i < text.length(); i++) {
      final char ch = text.charAt(i);
      if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '.') {
        builder.append(Character.toLowerCase(ch));
      }
    }

    final String textWithoutQuotes = builder.toString();
    return textWithoutQuotes;
  }

  /**
   * Checks if the text is all lowercase.
   *
   * @param text Text to check.
   * @return Whether the string is all lowercase.
   */
  public static boolean hasNoUpperCase(final String text) {
    return text != null && text.equals(text.toLowerCase());
  }

  /**
   * Checks if the text is null or empty.
   *
   * @param text Text to check.
   * @return Whether the string is blank.
   */
  public static boolean isBlank(final CharSequence text) {
    if (text == null || text.length() == 0) {
      return true;
    }

    for (int i = 0; i < text.length(); i++) {
      if (!Character.isWhitespace(text.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks if a class is available on the classpath.
   *
   * @param className Class to check
   * @return True if the class is available, false otherwise
   */
  public static boolean isClassAvailable(final String className) {
    try {
      Class.forName(className, false, Utility.class.getClassLoader());
      return true;
    } catch (final Exception e) {
      return false;
    }
  }

  /**
   * Checks if the text contains an integer only.
   *
   * @param text Text to check.
   * @return Whether the string is an integer.
   */
  public static boolean isIntegral(final CharSequence text) {
    if (text == null || text.length() == 0) {
      return false;
    }

    for (int i = 0; i < text.length(); i++) {
      final char ch = text.charAt(i);
      if (!Character.isDigit(ch) && ch != '+' && ch != '-') {
        return false;
      }
    }
    return true;
  }

  public static String join(final Collection<String> collection, final String separator) {
    if (collection == null || collection.isEmpty()) {
      return null;
    }

    final StringJoiner joiner = new StringJoiner(separator);
    joiner.setEmptyValue("");
    for (final String string : collection) {
      joiner.add(string);
    }

    return joiner.toString();
  }

  public static String join(final Map<?, ?> map, final String separator) {
    if (map == null || map.isEmpty()) {
      return null;
    }

    final StringJoiner joiner = new StringJoiner(separator);
    for (final Entry<?, ?> entry : map.entrySet()) {
      joiner.add(String.format("%s=%s", entry.getKey(), entry.getValue()));
    }

    return joiner.toString();
  }

  /**
   * Checks if the text is null or empty, and throws an exception if it is.
   *
   * @param text Text to check.
   * @return Provided string, if not blank.
   * @throws IllegalArgumentException If the provided string is blank
   */
  public static String requireNotBlank(final String text, final String message) {
    if (isBlank(text)) {
      throw new IllegalArgumentException(message);
    }
    return text;
  }

  public static String toSnakeCase(final String identifier) {
    if (isBlank(identifier)) {
      return identifier;
    }
    final Pattern identifyCamelCase = Pattern.compile("([A-Z])");
    final String snakeCaseIdentifier =
        identifyCamelCase.matcher(identifier).replaceAll("_$1").toLowerCase();
    return snakeCaseIdentifier;
  }

  public static String trimToEmpty(final String text) {
    if (isBlank(text)) {
      return "";
    } else {
      return text;
    }
  }

  private static int indexOfDifference(final String string1, final String string2) {
    if (string1 == null || string2 == null) {
      return 0;
    }
    int i;
    for (i = 0; i < string1.length() && i < string2.length(); ++i) {
      if (string1.charAt(i) != string2.charAt(i)) {
        break;
      }
    }
    if (i < string2.length() || i < string1.length()) {
      return i;
    }
    return -1;
  }

  private Utility() {
    // Prevent instantiation
  }
}
