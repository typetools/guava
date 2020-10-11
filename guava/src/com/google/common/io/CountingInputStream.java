/*
 * Copyright (C) 2007 The Guava Authors
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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtIncompatible;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.checkerframework.checker.index.qual.GTENegativeOne;
import org.checkerframework.checker.index.qual.IndexOrHigh;
import org.checkerframework.checker.index.qual.LTEqLengthOf;
import org.checkerframework.checker.index.qual.LTLengthOf;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 * An {@link InputStream} that counts the number of bytes read.
 *
 * @author Chris Nokleberg
 * @since 1.0
 */
@Beta
@GwtIncompatible
public final class CountingInputStream extends FilterInputStream {

  private long count;
  private long mark = -1;

  /**
   * Wraps another input stream, counting the number of bytes read.
   *
   * @param in the input stream to be wrapped
   */
  public CountingInputStream(InputStream in) {
    super(checkNotNull(in));
  }

  /** Returns the number of bytes read. */
  public long getCount() {
    return count;
  }

  @Override
  public @GTENegativeOne int read() throws IOException {
    int result = in.read();
    if (result != -1) {
      count++;
    }
    return result;
  }

  @Override
  public @GTENegativeOne @LTEqLengthOf("#1") int read(byte[] b, @IndexOrHigh("#1") int off, @NonNegative @LTLengthOf(value = "#1", offset = "#2 - 1") int len) throws IOException {
    int result = in.read(b, off, len);
    if (result != -1) {
      count += result;
    }
    return result;
  }

  @Override
  public @NonNegative long skip(long n) throws IOException {
    long result = in.skip(n);
    count += result;
    return result;
  }

  @Override
  public synchronized void mark(@NonNegative int readlimit) {
    in.mark(readlimit);
    mark = count;
    // it's okay to mark even if mark isn't supported, as reset won't work
  }

  @Override
  public synchronized void reset() throws IOException {
    if (!in.markSupported()) {
      throw new IOException("Mark not supported");
    }
    if (mark == -1) {
      throw new IOException("Mark not set");
    }

    in.reset();
    count = mark;
  }
}
