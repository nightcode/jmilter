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

/**
 *
 * @param <E> element type
 */
public final class IntMap<E> {

  private static class Core {
    final int shift;
    final int length;
    final int[] keys;
    final Object[] values;

    Core(int shift) {
      this.shift = shift;
      length = 1 << (32 - shift);
      keys = new int[length];
      values = new Object[length];
    }
  }

  private static final int MAGIC = 0xB46394CD;
  private static final int MAX_SHIFT = 29;
  private static final int THRESHOLD = (int) (1L << 31);

  private volatile Core core = new Core(MAX_SHIFT);
  private volatile int size;

  /**
   * Removes all of the mappings from this map.
   * The map will be empty after this call returns.
   *
   * NOTE: needs external synchronization
   */
  public void clear() {
    size = 0;
    core = new Core(MAX_SHIFT);
  }

  /**
   * Returns the value to which the specified key is mapped,
   * or {@code null} if this map contains no mapping for the key.
   *
   * @param key the key whose associated value is to be returned
   * @return the value to which the specified key is mapped, or
   *         {@code null} if this map contains no mapping for the key
   */
  public E get(int key) {
    Core c = core;
    int i = (key * MAGIC) >>> c.shift;
    int k;
    while (key != (k = c.keys[i])) {
      if (k == 0) {
        return null;
      }
      if (i == 0) {
        i = c.length;
      }
      i--;
    }
    return (E) c.values[i];
  }

  /**
   * Associates the specified value with the specified key in this map.
   * If the map previously contained a mapping for the key, the old
   * value is replaced.
   *
   * NOTE: needs external synchronization
   *
   * @param key key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   */
  public void put(int key, E value) {
    if (putInternal(core, key, value)) {
      if (++size >= (THRESHOLD >>> core.shift)) {
        rehash();
      }
    }
  }

  private boolean putInternal(Core core, int key, E value) {
    int i = (key * MAGIC) >>> core.shift;
    int k;
    while (key != (k = core.keys[i])) {
      if (k == 0) {
        core.keys[i] = key;
        core.values[i] = value;
        return true;
      }
      if (i == 0) {
        i = core.length;
      }
      i--;
    }
    core.values[i] = value;
    return false;
  }

  private void rehash() {
    Core oldCore = core;
    Core newCore = new Core(oldCore.shift - 1);
    for (int i = 0; i < oldCore.length; i++) {
      if (oldCore.keys[i] != 0) {
        putInternal(newCore, oldCore.keys[i], (E) oldCore.values[i]);
      }
    }
    core = newCore;
  }
}
