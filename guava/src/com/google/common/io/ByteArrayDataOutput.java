/*
 * Copyright (C) 2009 The Guava Authors
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

import com.google.common.annotations.GwtIncompatible;
import java.io.DataOutput;
import java.io.IOException;
import org.checkerframework.checker.index.qual.IndexOrHigh;
import org.checkerframework.checker.index.qual.LTLengthOf;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.signedness.qual.PolySigned;

/**
 * An extension of {@code DataOutput} for writing to in-memory byte arrays; its methods offer
 * identical functionality but do not throw {@link IOException}.
 *
 * @author Jayaprabhakar Kadarkarai
 * @since 1.0
 */
@GwtIncompatible
@ElementTypesAreNonnullByDefault
public interface ByteArrayDataOutput extends DataOutput {
  @Override
  void write(int b);

  @Override
  void write(byte b[]);

  @Override
  void write(@PolySigned byte b[], @IndexOrHigh("#1") int off, @NonNegative @LTLengthOf(value = "#1", offset = "#2 - 1") int len);

  @Override
  void writeBoolean(boolean v);

  @Override
  void writeByte(int v);

  @Override
  void writeShort(int v);

  @Override
  void writeChar(int v);

  @Override
  void writeInt(int v);

  @Override
  void writeLong(long v);

  @Override
  void writeFloat(float v);

  @Override
  void writeDouble(double v);

  @Override
  void writeChars(String s);

  @Override
  void writeUTF(String s);

  /**
   * @deprecated This method is dangerous as it discards the high byte of every character. For
   *     UTF-8, use {@code write(s.getBytes(StandardCharsets.UTF_8))}.
   */
  @Deprecated
  @Override
  void writeBytes(String s);

  /** Returns the contents that have been written to this instance, as a byte array. */
  byte[] toByteArray();
}
