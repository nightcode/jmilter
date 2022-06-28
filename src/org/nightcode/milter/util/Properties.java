/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nightcode.milter.util;

public enum Properties {
  ;

  public static boolean getBoolean(String name, boolean def) {
    String val = System.getProperty(name);
    if (val == null) {
      return def;
    }
    return Boolean.parseBoolean(val);
  }

  public static int getInt(String name, int def) {
    String val = System.getProperty(name);
    if (val == null) {
      return def;
    }
    return Integer.parseInt(val);
  }

  public static long getLong(String name, long def) {
    String val = System.getProperty(name);
    if (val == null) {
      return def;
    }
    return Long.parseLong(val);
  }

  public static String getString(String name, String def) {
    return System.getProperty(name, def);
  }
}
