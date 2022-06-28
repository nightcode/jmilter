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

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class ThrowablesTest {

  static class StackTraceException extends Exception {
    StackTraceException(String message) {
      super(message);
    }
  }

  @Test public void testGetRootCause() {
    StackTraceException rootCause = new StackTraceException("test root cause");
    IllegalStateException ise = new IllegalStateException(rootCause);
    RuntimeException re = new RuntimeException(ise);

    Assert.assertSame(rootCause, Throwables.getRootCause(re));
  }

  @Test public void testGetStackTrace() {
    StackTraceException ex = new StackTraceException("test message");
    
    String target = Throwables.getStackTrace(ex);

    StringBuilder sb = new StringBuilder()
        .append(Pattern.quote(ex.getClass().getName() + ": " + ex.getMessage())).append('\n')
        .append("\\sat ").append(ThrowablesTest.class.getName()).append('.').append("testGetStackTrace").append(".*\n");

    Assert.assertTrue(Pattern.compile(sb.toString()).matcher(target).find());
  }
}
