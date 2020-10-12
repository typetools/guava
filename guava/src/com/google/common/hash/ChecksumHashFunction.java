/*
 * Copyright (C) 2012 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.common.hash;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.errorprone.annotations.Immutable;
import org.checkerframework.checker.index.qual.IndexOrHigh;
import org.checkerframework.checker.index.qual.LTLengthOf;
import org.checkerframework.checker.index.qual.NonNegative;

import java.io.Serializable;
import java.util.zip.Checksum;

/**
 * {@link HashFunction} adapter for {@link Checksum} instances.
 *
 * @author Colin Decker
 */
@Immutable
final class ChecksumHashFunction extends AbstractHashFunction implements Serializable {
  private final ImmutableSupplier<? extends Checksum> checksumSupplier;
  private final @NonNegative int bits;
  private final String toString;

  ChecksumHashFunction(
      ImmutableSupplier<? extends Checksum> checksumSupplier, @NonNegative int bits, String toString) {
    this.checksumSupplier = checkNotNull(checksumSupplier);
    checkArgument(bits == 32 || bits == 64, "bits (%s) must be either 32 or 64", bits);
    this.bits = bits;
    this.toString = checkNotNull(toString);
  }

  @Override
  public @NonNegative int bits() {
    return bits;
  }

  @Override
  public Hasher newHasher() {
    return new ChecksumHasher(checksumSupplier.get());
  }

  @Override
  public String toString() {
    return toString;
  }

  /** Hasher that updates a checksum. */
  private final class ChecksumHasher extends AbstractByteHasher {
    private final Checksum checksum;

    private ChecksumHasher(Checksum checksum) {
      this.checksum = checkNotNull(checksum);
    }

    @Override
    protected void update(byte b) {
      checksum.update(b);
    }

    @Override
    protected void update(byte[] bytes, @NonNegative @LTLengthOf(value = "#1",offset = "#3 - 1") int off, @NonNegative @LTLengthOf(value = "#1",offset = "#2 - 1") int len) {
      checksum.update(bytes, off, len);
    }

    @Override
    public HashCode hash() {
      long value = checksum.getValue();
      if (bits == 32) {
        /*
         * The long returned from a 32-bit Checksum will have all 0s for its second word, so the
         * cast won't lose any information and is necessary to return a HashCode of the correct
         * size.
         */
        return HashCode.fromInt((int) value);
      } else {
        return HashCode.fromLong(value);
      }
    }
  }

  private static final long serialVersionUID = 0L;
}
