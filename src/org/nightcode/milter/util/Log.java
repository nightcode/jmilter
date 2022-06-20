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

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.nightcode.milter.util.Log.PrintStreamLoggingHandler.ERR;
import static org.nightcode.milter.util.Log.PrintStreamLoggingHandler.OUT;

public enum Log {
  ;

  public interface LoggingHandler {

    default void log(@NotNull Class<?> clazz, String message) {
      log(clazz, () -> message, null);
    }

    default void log(@NotNull Class<?> clazz, Throwable thrown) {
      log(clazz, () -> "", thrown);
    }

    default void log(@NotNull Class<?> clazz, Supplier<String> supplier) {
      log(clazz, supplier, null);
    }

    default void log(@NotNull Class<?> clazz, String message, @Nullable Throwable thrown) {
      log(clazz, () -> message, thrown);
    }

    void log(@NotNull Class<?> clazz, Supplier<String> supplier, @Nullable Throwable thrown);
  }

  private static final AtomicReference<LoggingHandler> DEBUG = new AtomicReference<>(OUT);
  private static final AtomicReference<LoggingHandler> INFO  = new AtomicReference<>(OUT);
  private static final AtomicReference<LoggingHandler> WARN  = new AtomicReference<>(ERR);
  private static final AtomicReference<LoggingHandler> ERROR = new AtomicReference<>(ERR);
  private static final AtomicReference<LoggingHandler> FATAL = new AtomicReference<>(ERR);

  public static LoggingHandler debug() {
    return DEBUG.get();
  }

  public static LoggingHandler error() {
    return ERROR.get();
  }

  public static LoggingHandler fatal() {
    return FATAL.get();
  }

  public static LoggingHandler info() {
    return INFO.get();
  }

  public static LoggingHandler warn() {
    return WARN.get();
  }

  public static void setLoggingHandler(LoggingHandler debug,
                                       LoggingHandler info,
                                       LoggingHandler warn,
                                       LoggingHandler error,
                                       LoggingHandler fatal) {
    DEBUG.set(debug);
    INFO.set(info);
    WARN.set(warn);
    ERROR.set(error);
    FATAL.set(fatal);
  }

  enum PrintStreamLoggingHandler implements LoggingHandler {
    ERR() {
      @Override public void log(@NotNull Class<?> clazz, Supplier<String> message, @Nullable Throwable thrown) {
        log(System.err, clazz, message, thrown);
      }
    },
    OUT() {
      @Override public void log(@NotNull Class<?> clazz, Supplier<String> message, @Nullable Throwable thrown) {
        log(System.out, clazz, message, thrown);
      }
    };

    void log(@NotNull PrintStream stream, Class<?> clazz, Supplier<String> message, @Nullable Throwable thrown) {
      synchronized (stream) {
        stream.printf("%s [%s/%s]: %s\n", LocalDateTime.now(), Thread.currentThread().getName(), clazz.getSimpleName(), message.get());
        if (thrown != null) {
          thrown.printStackTrace(stream);
        }
      }
    }
  }
}
