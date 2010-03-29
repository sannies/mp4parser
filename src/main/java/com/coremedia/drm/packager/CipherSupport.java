/*  
 * Copyright 2008 CoreMedia AG, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an AS IS BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package com.coremedia.drm.packager;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;

/**
 * Shortcut for creating ciphers.
 */
public final class CipherSupport {
  private CipherSupport() {
  }

  /**
   * Returns a cipher for the AES algorithm (never <code>null</code>).
   *
   * @return uninitialized AES/CBC/PKCS5Padding Cipher
   */
  public static Cipher createAES128CBCCipher() {
    return createCipher("AES/CBC/PKCS5Padding");
  }


  /**
   * Returns a cipher for the AES algorithm (never <code>null</code>).
   *
   * @return uninitialized AES/CTR/NoPadding Cipher
   */
  public static Cipher createAES128CTRCipher() {
    return createCipher("AES/CTR/NoPadding");
  }

  private static Cipher createCipher(String algo) {
    try {
      return Cipher.getInstance(algo);
    } catch (GeneralSecurityException e) {
      throw new Error(e); //cannot happen if BouncyCastle is available
    }
  }
}
