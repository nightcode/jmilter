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

package org.nightcode.milter.samples;

import java.net.SocketAddress;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.nightcode.milter.AbstractMilterHandler;
import org.nightcode.milter.MilterContext;
import org.nightcode.milter.MilterException;
import org.nightcode.milter.codec.MilterPacket;
import org.nightcode.milter.Actions;
import org.nightcode.milter.util.Hexs;
import org.nightcode.milter.util.Log;
import org.nightcode.milter.ProtocolSteps;

import static java.lang.String.format;

public class AddHeaderMilterHandler extends AbstractMilterHandler {

  private static final Hexs HEX = Hexs.hex();

  AddHeaderMilterHandler(Actions milterActions, ProtocolSteps milterProtocolSteps) {
    super(milterActions, milterProtocolSteps);
  }

  @Override public void connect(MilterContext context, String hostname, int family, int port, @Nullable SocketAddress address)
      throws MilterException {
    Log.debug().log(getClass(), format("<CONNECT> hostname: %s, family: %s, port: %s, address: %s", hostname, family, port, address));
    super.connect(context, hostname, family, port, address);
  }

  @Override public void helo(MilterContext context, String helohost) throws MilterException {
    Log.debug().log(getClass(), "<HELO> helohost: " + helohost);
    super.helo(context, helohost);
  }

  @Override public void envfrom(MilterContext context, List<String> from) throws MilterException {
    Log.debug().log(getClass(), "<ENVFROM> from: " + from);
    super.envfrom(context, from);
  }

  @Override public void envrcpt(MilterContext context, List<String> recipients) throws MilterException {
    Log.debug().log(getClass(), "<ENVRCPT> recipients: " + recipients);
    super.envrcpt(context, recipients);
  }

  @Override public void header(MilterContext context, String headerName, String headerValue) throws MilterException {
    Log.debug().log(getClass(), format("<HEADER> headerName: %s, headerValue: %s", headerName, headerValue));
    super.header(context, headerName, headerValue);
  }

  @Override public void eoh(MilterContext context) throws MilterException {
    Log.debug().log(getClass(), "<EOH>");
    super.eoh(context);
  }

  @Override public void body(MilterContext context, String bodyChunk) throws MilterException {
    Log.debug().log(getClass(), "<BODY> bodyChunk: " + bodyChunk);
    super.body(context, bodyChunk);
  }

  @Override public void eob(MilterContext context, @Nullable String bodyChunk) throws MilterException {
    Log.debug().log(getClass(), "<EOB> bodyChunk: " + bodyChunk);

    messageModificationService.addHeader(context, "X-Received", "Tue, 31 Oct 2018 17:56:00 -0700 (PDT)");

    super.eob(context, bodyChunk);
  }

  @Override public void abort(MilterContext context, MilterPacket packet) throws MilterException {
    Log.debug().log(getClass(), "<ABORT> abort: " + packet);
    super.abort(context, packet);
  }

  @Override public void quit(MilterContext context) {
    Log.debug().log(getClass(), "<QUIT>");
  }

  @Override public void quitNc(MilterContext context) {
    Log.debug().log(getClass(), "<QUIT_NC>");
  }

  @Override public void data(MilterContext context, byte[] payload) throws MilterException {
    Log.debug().log(getClass(), "<DATA>");
    super.data(context, payload);
  }

  @Override public void optneg(MilterContext context, int mtaProtocolVersion, Actions mtaActions,
                               ProtocolSteps mtaProtocolSteps) throws MilterException {
    Log.debug().log(getClass(), format("<NEGOTIATE> %s, %s, %s", mtaProtocolVersion, mtaActions, mtaProtocolSteps));
    super.optneg(context, mtaProtocolVersion, mtaActions, mtaProtocolSteps);
  }

  @Override public void unknown(MilterContext context, byte[] payload) throws MilterException {
    Log.debug().log(getClass(), format("<UNKNOWN> unknown: %s", payload.length > 0 ? HEX.fromByteArray(payload) : "NULL"));
    super.unknown(context, payload);
  }
}
