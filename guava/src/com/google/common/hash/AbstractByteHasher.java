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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndexes;

import com.google.common.primitives.Chars;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.checkerframework.checker.index.qual.LTLengthOf;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.common.value.qual.ArrayLen;
import org.checkerframework.common.value.qual.IntRange;

/**
 * Abstract {@link Hasher} that handles converting primitives to bytes using a scratch {@code
 * ByteBuffer} and streams all bytes to a sink to compute the hash.
 *
 * @author Colin Decker
 */
@CanIgnoreReturnValue
@ElementTypesAreNonnullByDefault
abstract class AbstractByteHasher extends AbstractHasher {
  private final ByteBuffer scratch = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);

  /** Updates this hasher with the given byte. */
  protected abstract void update(byte b);

  @SuppressWarnings("upperbound:argument")// `0 + b.length - 1` is < b.length.
  /** Updates this hasher with the given bytes. */
  protected void update(byte[] b) {
    update(b, 0, b.length);
  }

  /** Updates this hasher with {@code len} bytes starting at {@code off} in the given buffer. */
  protected void update(byte[] b, @NonNegative @LTLengthOf(value = "#1",offset = "#3 - 1") int off, @NonNegative @LTLengthOf(value = "#1",offset = "#2 - 1") int len) {
    for (int i = off; i < off + len; i++) {
      update(b[i]);
    }
  }

  /** Updates this hasher with bytes from the given buffer. */
  @SuppressWarnings({"lowerbound:argument","upperbound:argument"}) /*
  Invariants: position <= limit <= capacity( is length of buffer).
  Since `b.remaining()` return limit - position, and `b.arrayOffset() + b.position()`
  return offset("buffer's backing array of the first element of the buffer")  + position,
  `b.remaining() + b.arrayOffset() + b.position() - 1 < b.array().length`. */
  protected void update(ByteBuffer b) {
    if (b.hasArray()) {
      update(b.array(), b.arrayOffset() + b.position(), b.remaining());
      Java8Compatibility.position(b, b.limit());
    } else {
      for (int remaining = b.remaining(); remaining > 0; remaining--) {
        update(b.get());
      }
    }
  }

  /** Updates the sink with the given number of bytes from the buffer. */
  @SuppressWarnings({"upperbound:argument"}) /*
  Since `ByteBuffer scratch = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);`, scratch.array() will
  return an array with length of 8.
  */
  private Hasher update(@IntRange(from = 0, to = 8) int bytes) {
    try {
      update(scratch.array(), 0, bytes);
    } finally {
      Java8Compatibility.clear(scratch);
    }
    return this;
  }

  @Override
  public Hasher putByte(byte b) {
    update(b);
    return this;
  }

  @Override
  public Hasher putBytes(byte[] bytes) {
    checkNotNull(bytes);
    update(bytes);
    return this;
  }

  @Override
  public Hasher putBytes(byte[] bytes, @NonNegative @LTLengthOf(value = "#1",offset = "#3 - 1") int off, @NonNegative @LTLengthOf(value = "#1",offset = "#2 - 1") int len) {
    checkPositionIndexes(off, off + len, bytes.length);
    update(bytes, off, len);
    return this;
  }

  @Override
  public Hasher putBytes(ByteBuffer bytes) {
    update(bytes);
    return this;
  }

  @Override
  public Hasher putShort(short s) {
    scratch.putShort(s);
    return update(Shorts.BYTES);
  }

  @Override
  public Hasher putInt(int i) {
    scratch.putInt(i);
    return update(Ints.BYTES);
  }

  @Override
  public Hasher putLong(long l) {
    scratch.putLong(l);
    return update(Longs.BYTES);
  }

  @Override
  public Hasher putChar(char c) {
    scratch.putChar(c);
    return update(Chars.BYTES);
  }
}
