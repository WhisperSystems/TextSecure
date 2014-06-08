/**
 * Copyright (C) 2013-2014 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.textsecure.crypto.ratchet;

import javax.crypto.spec.SecretKeySpec;

public class MessageKeys {

  private final SecretKeySpec cipherKey;
  private final SecretKeySpec macKey;
  private final int           counter;

  public MessageKeys(SecretKeySpec cipherKey, SecretKeySpec macKey, int counter) {
    this.cipherKey = cipherKey;
    this.macKey    = macKey;
    this.counter   = counter;
  }

  public SecretKeySpec getCipherKey() {
    return cipherKey;
  }

  public SecretKeySpec getMacKey() {
    return macKey;
  }

  public int getCounter() {
    return counter;
  }
}
