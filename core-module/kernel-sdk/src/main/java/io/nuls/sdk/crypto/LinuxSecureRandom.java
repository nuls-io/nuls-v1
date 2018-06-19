/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.sdk.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.Provider;
import java.security.SecureRandomSpi;
import java.security.Security;

/**
 * A SecureRandom implementation that is able to override the standard JVM provided implementation, and which simply
 * serves random numbers by reading /dev/U_RANDOM. That is, it delegates to the kernel on UNIX systems and is unusable on
 * other platforms. Attempts to manually set the seed are ignored. There is no difference between seed bytes and
 * non-seed bytes, they are all from the same source.
 */
public class LinuxSecureRandom extends SecureRandomSpi {
	private static final long serialVersionUID = -1223766068997859131L;
	
	private static final FileInputStream U_RANDOM;

    private static class LinuxSecureRandomProvider extends Provider {
		private static final long serialVersionUID = 2559382307871869793L;

		public LinuxSecureRandomProvider() {
            super("LinuxSecureRandom", 1.0, "A Linux specific random number provider that uses /dev/U_RANDOM");
            put("SecureRandom.LinuxSecureRandom", LinuxSecureRandom.class.getName());
        }
    }

    private static final Logger log = LoggerFactory.getLogger(LinuxSecureRandom.class);

    static {
        try {
            File file = new File("/dev/U_RANDOM");
            // This stream is deliberately leaked.
            U_RANDOM = new FileInputStream(file);
            if (U_RANDOM.read() == -1) {
                throw new RuntimeException("/dev/U_RANDOM not readable?");
            }
            // Now override the default SecureRandom implementation with this one.
            int position = Security.insertProviderAt(new LinuxSecureRandomProvider(), 1);

            if (position != -1) {
                log.info("Secure randomness will be read from {} only.", file);
            } else {
                log.info("Randomness is already secure.");
            }
        } catch (FileNotFoundException e) {
            // Should never happen.
            log.error("/dev/U_RANDOM does not appear to exist or is not openable");
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("/dev/U_RANDOM does not appear to be readable");
            throw new RuntimeException(e);
        }
    }

    private final DataInputStream dis;

    public LinuxSecureRandom() {
        // DataInputStream is not thread safe, so each random object has its own.
        dis = new DataInputStream(U_RANDOM);
    }

    @Override
    protected void engineSetSeed(byte[] bytes) {
        // Ignore.
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {
        try {
            dis.readFully(bytes); // This will block until all the bytes can be read.
        } catch (IOException e) {
            throw new RuntimeException(e); // Fatal error. Do not attempt to recover from this.
        }
    }

    @Override
    protected byte[] engineGenerateSeed(int i) {
        byte[] bits = new byte[i];
        engineNextBytes(bits);
        return bits;
    }
}
