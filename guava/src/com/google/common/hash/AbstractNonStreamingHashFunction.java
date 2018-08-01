/*
 * Copyright (C) 2011 The Guava Authors
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

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.checkerframework.checker.index.qual.LTLengthOf;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.common.value.qual.MinLen;

/**
 * Skeleton implementation of {@link HashFunction}, appropriate for non-streaming algorithms. All
 * the hash computation done using {@linkplain #newHasher()} are delegated to the {@linkplain
 * #hashBytes(byte[], int, int)} method.
 *
 * @author Dimitris Andreou
 */
@Immutable
abstract class AbstractNonStreamingHashFunction extends AbstractHashFunction {
  @Override
  public Hasher newHasher() {
    return newHasher(32);
  }

  @Override
  public Hasher newHasher(@NonNegative int expectedInputSize) {
    Preconditions.checkArgument(expectedInputSize >= 0);
    return new BufferingHasher(expectedInputSize);
  }

  @SuppressWarnings("value:argument.type.incompatible")// ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(input).array()
  //return an array of length 4.
  @Override
  public HashCode hashInt(int input) {
    return hashBytes(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(input).array());
  }

  @SuppressWarnings("value:argument.type.incompatible")// ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(input).array()
  //return an array of length 8.
  @Override
  public HashCode hashLong(long input) {
    return hashBytes(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(input).array());
  }

  @SuppressWarnings("value:argument.type.incompatible")// If `input` has min length of 1, since `buffer.array()`
  //returns the array that backs this buffer, the array also has min length of 1.
  @Override
  public HashCode hashUnencodedChars(@MinLen(1) CharSequence input) {
    int len = input.length();
    ByteBuffer buffer = ByteBuffer.allocate(len * 2).order(ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < len; i++) {
      buffer.putChar(input.charAt(i));
    }
    return hashBytes(buffer.array());
  }

  @SuppressWarnings("value:argument.type.incompatible")//If `input` has min of 1, since `getBytes(charset)`
  // the resultant byte array, the array returned also has min length of 1
  @Override
  public HashCode hashString(@MinLen(1) CharSequence input, Charset charset) {
    return hashBytes(input.toString().getBytes(charset));
  }

  @Override
  public abstract HashCode hashBytes(byte[] input, int off, int len);

  @SuppressWarnings("lowerbound:argument.type.incompatible")/* Since invariants: mark <= position <= limit <= capacity,
  and position is initialized as 0, `input.remaining()` return `limit - position` with lowest possible value as 0.
  */
  @Override
  public HashCode hashBytes(ByteBuffer input) {
    return newHasher(input.remaining()).putBytes(input).hash();
  }

  /** In-memory stream-based implementation of Hasher. */
  private final class BufferingHasher extends AbstractHasher {
    final ExposedByteArrayOutputStream stream;

    BufferingHasher(@NonNegative int expectedInputSize) {
      this.stream = new ExposedByteArrayOutputStream(expectedInputSize);
    }

    @Override
    public Hasher putByte(byte b) {
      stream.write(b);
      return this;
    }

    @Override
    public Hasher putBytes(byte[] bytes, @NonNegative @LTLengthOf(value = "#1", offset = "#3 - 1") int off, @NonNegative @LTLengthOf(value = "#1",offset = "#2 - 1") int len) {
      stream.write(bytes, off, len);
      return this;
    }

    @Override
    public Hasher putBytes(ByteBuffer bytes) {
      stream.write(bytes);
      return this;
    }

    @Override
    public HashCode hash() {
      return hashBytes(stream.byteArray(), 0, stream.length());
    }
  }

  // Just to access the byte[] without introducing an unnecessary copy
  private static final class ExposedByteArrayOutputStream extends ByteArrayOutputStream {
    ExposedByteArrayOutputStream(@NonNegative int expectedInputSize) {
      super(expectedInputSize);
    }

    @SuppressWarnings({"lowerbound:argument.type.incompatible", "lowerbound:assignment.type.incompatible"})/* Since invariants: mark <= position <= limit <= capacity,
    and position is initialized as 0, `input.remaining()` return `limit - position` with lowest possible value as 0. */
    void write(ByteBuffer input) {
      @NonNegative int remaining = input.remaining();
      if (count + remaining > buf.length) {
        buf = Arrays.copyOf(buf, count + remaining);
      }
      input.get(buf, count, remaining);
      count += remaining;
    }

    byte[] byteArray() {
      return buf;
    }

    int length() {
      return count;
    }
  }
}
