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

package org.nightcode.milter.client;

import java.util.ArrayList;
import java.util.List;

public final class Macros {

  static final class Pair {
    final String key;
    final String value;

    Pair(String key, String value) {
      this.key   = key;
      this.value = value;
    }
  }

  public static final class Builder {
    private final List<Pair> pairs = new ArrayList<>();

    public Builder add(String key, String value) {
      pairs.add(new Pair(key, value));
      return this;
    }

    public Macros build() {
      return new Macros(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final List<Pair> pairs;

  private Macros(Builder builder) {
    this.pairs = builder.pairs;
  }

  List<Pair> pairs() {
    return pairs;
  }
}
