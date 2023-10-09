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

package org.nightcode.milter;

public enum MacroPlace {

  SMFIM_CONNECT(0),  /* connect */
  SMFIM_HELO   (1),  /* HELO/EHLO */
  SMFIM_ENVFROM(2),  /* MAIL From */
  SMFIM_ENVRCPT(3),  /* RCPT To */
  SMFIM_DATA   (4),  /* DATA */
  SMFIM_EOM    (5),  /* end of message */
  SMFIM_EOH    (6);  /* end of header */

  public static final int MAX_MACROS_ENTRIES = 7;

  private final int index;

  MacroPlace(int index) {
    this.index = index;
  }

  public int index() {
    return index;
  }
}
