/*
 * Copyright (C) 2013 The Guava Authors
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

package com.google.common.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndexes;

import com.google.common.annotations.GwtIncompatible;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import org.checkerframework.checker.index.qual.GTENegativeOne;
import org.checkerframework.checker.index.qual.IndexOrHigh;
import org.checkerframework.checker.index.qual.LTEqLengthOf;
import org.checkerframework.checker.index.qual.LTLengthOf;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * A {@link Reader} that reads the characters in a {@link CharSequence}. Like {@code StringReader},
 * but works with any {@link CharSequence}.
 *
 * @author Colin Decker
 */
// TODO(cgdecker): make this public? as a type, or a method in CharStreams?
@GwtIncompatible
final class CharSequenceReader extends Reader {

  private CharSequence seq;
  private int pos;
  private int mark;

  /** Creates a new reader wrapping the given character sequence. */
  public CharSequenceReader(CharSequence seq) {
    this.seq = checkNotNull(seq);
  }

  private void checkOpen() throws IOException {
    if (seq == null) {
      throw new IOException("reader closed");
    }
  }

  private boolean hasRemaining() {
    return remaining() > 0;
  }

  @SuppressWarnings("return.type.incompatible") // The method is private and every place it is used returns a non-negative value.
  private @NonNegative int remaining() {
    return seq.length() - pos;
  }

  @Override
  @SuppressWarnings("argument.type.incompatible") // pos is a valid index for seq because the loop stops at charsToRead steps, which cannot exceed the limit of target or seq.
  public synchronized @GTENegativeOne int read(CharBuffer target) throws IOException {
    checkNotNull(target);
    checkOpen();
    if (!hasRemaining()) {
      return -1;
    }
    int charsToRead = Math.min(target.remaining(), remaining());
    for (int i = 0; i < charsToRead; i++) {
      target.put(seq.charAt(pos++));
    }
    return charsToRead;
  }

  @Override
  @SuppressWarnings({"return.type.incompatible", "argument.type.incompatible"}) /* charAt returns a char, which is known to be non-negative Ascii.
  pos is a valid index for seq because hasRemaining() would otherwise return false */
  public synchronized @GTENegativeOne int read() throws IOException {
    checkOpen();
    return hasRemaining() ? seq.charAt(pos++) : -1;
  }

  @Override
  @SuppressWarnings({"argument.type.incompatible", "return.type.incompatible"}) /*
  #1. pos is a valid index for seq because the loop stops at charsToRead steps, which cannot exceed the limit of seq.
  #2. charsToRead is at most equal to len, which is known to be below the length of cbuf */
  public synchronized @GTENegativeOne @LTEqLengthOf("#1") int read(char[] cbuf, @IndexOrHigh("#1") int off, @NonNegative @LTLengthOf(value = "#1", offset = "#2 - 1") int len) throws IOException {
    checkPositionIndexes(off, off + len, cbuf.length);
    checkOpen();
    if (!hasRemaining()) {
      return -1;
    }
    int charsToRead = Math.min(len, remaining());
    for (int i = 0; i < charsToRead; i++) {
      cbuf[off + i] = seq.charAt(pos++); // #1
    }
    return charsToRead; // #2
  }

  @Override
  public synchronized @NonNegative long skip(@NonNegative long n) throws IOException {
    checkArgument(n >= 0, "n (%s) may not be negative", n);
    checkOpen();
    int charsToSkip = (int) Math.min(remaining(), n); // safe because remaining is an int
    pos += charsToSkip;
    return charsToSkip;
  }

  @Override
  public synchronized boolean ready() throws IOException {
    checkOpen();
    return true;
  }

  @Override
  public boolean markSupported() {
    return true;
  }

  @Override
  public synchronized void mark(@NonNegative int readAheadLimit) throws IOException {
    checkArgument(readAheadLimit >= 0, "readAheadLimit (%s) may not be negative", readAheadLimit);
    checkOpen();
    mark = pos;
  }

  @Override
  public synchronized void reset() throws IOException {
    checkOpen();
    pos = mark;
  }

  @Override
  public synchronized void close() throws IOException {
    seq = null;
  }
}
