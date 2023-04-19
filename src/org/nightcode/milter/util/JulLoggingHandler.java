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

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum JulLoggingHandler implements Log.LoggingHandler {

  DEBUG(Level.FINER),
  INFO(Level.INFO),
  WARN(Level.WARNING),
  ERROR(Level.WARNING),
  FATAL(Level.SEVERE);

  static Logger getLogger(Class<?> clazz) {
    return CLASS_LOGGER.get(clazz);
  }

  static final ClassValue<Logger> CLASS_LOGGER = new ClassValue<Logger>() {
    @Override protected Logger computeValue(Class<?> type) {
      return Logger.getLogger(type.getName());
    }
  };

  private final Level level;

  JulLoggingHandler(Level level) {
    this.level = level;
  }

  @Override public void log(@NotNull Class<?> clazz, String message) {
    getLogger(clazz).log(level, message);
  }

  @Override public void log(@NotNull Class<?> clazz, String message, @Nullable Throwable thrown) {
    getLogger(clazz).log(level, message, thrown);
  }

  @Override public void log(@NotNull Class<?> clazz, String message, Object... params) {
    getLogger(clazz).log(level, message.replaceAll("\\{}", "%s"), params);
  }

  @Override public void log(@NotNull Class<?> clazz, Supplier<String> supplier, @Nullable Throwable thrown) {
    getLogger(clazz).log(level, thrown, supplier);
  }
}
