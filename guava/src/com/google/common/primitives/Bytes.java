/*
 * Copyright (C) 2008 The Guava Authors
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

package com.google.common.primitives;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndexes;

import com.google.common.annotations.GwtCompatible;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;
import javax.annotation.CheckForNull;
import org.checkerframework.checker.index.qual.HasSubsequence;
import org.checkerframework.checker.index.qual.IndexFor;
import org.checkerframework.checker.index.qual.IndexOrHigh;
import org.checkerframework.checker.index.qual.IndexOrLow;
import org.checkerframework.checker.index.qual.LTEqLengthOf;
import org.checkerframework.checker.index.qual.LTLengthOf;
import org.checkerframework.checker.index.qual.LessThan;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.index.qual.SubstringIndexFor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signedness.qual.PolySigned;
import org.checkerframework.checker.signedness.qual.Signed;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;
import org.checkerframework.common.value.qual.MinLen;
import org.checkerframework.framework.qual.AnnotatedFor;
import org.checkerframework.framework.qual.CFComment;

/**
 * Static utility methods pertaining to {@code byte} primitives, that are not already found in
 * either {@link Byte} or {@link Arrays}, <i>and interpret bytes as neither signed nor unsigned</i>.
 * The methods which specifically treat bytes as signed or unsigned are found in {@link SignedBytes}
 * and {@link UnsignedBytes}.
 *
 * <p>See the Guava User Guide article on <a
 * href="https://github.com/google/guava/wiki/PrimitivesExplained">primitive utilities</a>.
 *
 * @author Kevin Bourrillion
 * @since 1.0
 */
// TODO(kevinb): how to prevent warning on UnsignedBytes when building GWT
// javadoc?
@AnnotatedFor({"signedness"})
@GwtCompatible
@ElementTypesAreNonnullByDefault
public final class Bytes {
  private Bytes() {}

  /**
   * Returns a hash code for {@code value}; equal to the result of invoking {@code ((Byte)
   * value).hashCode()}.
   *
   * <p><b>Java 8 users:</b> use {@link Byte#hashCode(byte)} instead.
   *
   * @param value a primitive {@code byte} value
   * @return a hash code for the value
   */
  @SuppressWarnings("signedness:cast.unsafe") // UnknownSignedness byte to Signed int is ok for hashing
  public static int hashCode(@UnknownSignedness byte value) {
    return (@Signed int) value;
  }

  /**
   * Returns {@code true} if {@code target} is present as an element anywhere in {@code array}.
   *
   * @param array an array of {@code byte} values, possibly empty
   * @param target a primitive {@code byte} value
   * @return {@code true} if {@code array[i] == target} for some value of {@code i}
   */
  public static boolean contains(@PolySigned byte[] array, @PolySigned byte target) {
    for (byte value : array) {
      if (value == target) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the index of the first appearance of the value {@code target} in {@code array}.
   *
   * @param array an array of {@code byte} values, possibly empty
   * @param target a primitive {@code byte} value
   * @return the least index {@code i} for which {@code array[i] == target}, or {@code -1} if no
   *     such index exists.
   */
  public static @IndexOrLow("#1") int indexOf(@PolySigned byte[] array, @PolySigned byte target) {
    return indexOf(array, target, 0, array.length);
  }

  // TODO(kevinb): consider making this public
  private static @IndexOrLow("#1") @LessThan("#4") int indexOf(@PolySigned byte[] array, @PolySigned byte target, @IndexOrHigh("#1") int start, @IndexOrHigh("#1") int end) {
    for (int i = start; i < end; i++) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the start position of the first occurrence of the specified {@code target} within
   * {@code array}, or {@code -1} if there is no such occurrence.
   *
   * <p>More formally, returns the lowest index {@code i} such that {@code Arrays.copyOfRange(array,
   * i, i + target.length)} contains exactly the same elements as {@code target}.
   *
   * @param array the array to search for the sequence {@code target}
   * @param target the array to search for as a sub-sequence of {@code array}
   */
  @SuppressWarnings("substringindex:return") // https://github.com/kelloggm/checker-framework/issues/206, 207 and 208
  public static @LTEqLengthOf("#1") @SubstringIndexFor(value = "#1", offset = "#2.length - 1") int indexOf(@PolySigned byte[] array, @PolySigned byte[] target) {
    checkNotNull(array, "array");
    checkNotNull(target, "target");
    if (target.length == 0) {
      return 0;
    }

    outer:
    for (int i = 0; i < array.length - target.length + 1; i++) {
      for (int j = 0; j < target.length; j++) {
        if (array[i + j] != target[j]) {
          continue outer;
        }
      }
      return i;
    }
    return -1;
  }

  /**
   * Returns the index of the last appearance of the value {@code target} in {@code array}.
   *
   * @param array an array of {@code byte} values, possibly empty
   * @param target a primitive {@code byte} value
   * @return the greatest index {@code i} for which {@code array[i] == target}, or {@code -1} if no
   *     such index exists.
   */
  public static @IndexOrLow("#1") int lastIndexOf(@PolySigned byte[] array, @PolySigned byte target) {
    return lastIndexOf(array, target, 0, array.length);
  }

  // TODO(kevinb): consider making this public
  private static @IndexOrLow("#1") @LessThan("#4") int lastIndexOf(@PolySigned byte[] array, @PolySigned byte target, @IndexOrHigh("#1") int start, @IndexOrHigh("#1") int end) {
    for (int i = end - 1; i >= start; i--) {
      if (array[i] == target) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the values from each provided array combined into a single array. For example, {@code
   * concat(new byte[] {a, b}, new byte[] {}, new byte[] {c}} returns the array {@code {a, b, c}}.
   *
   * @param arrays zero or more {@code byte} arrays
   * @return a single array containing all the values from the source arrays, in order
   */
  /*
   * New array has size that is sum of array lengths.
   * length is a sum of lengths of arrays.
   * pos is increased the same way as length, so pos points to a valid
   * range of length array.length in result.
   */
  @SuppressWarnings("signedness:enhancedfor")
  public static @PolySigned byte[] concat(@PolySigned byte[]... arrays) {
    int length = 0;
    for (byte[] array : arrays) {
      length += array.length;
    }
    @PolySigned byte[] result = new byte[length];
    int pos = 0;
    for (@PolySigned byte[] array : arrays) {
      System.arraycopy(array, 0, result, pos, array.length);
      pos += array.length;
    }
    return result;
  }

  /**
   * Returns an array containing the same values as {@code array}, but guaranteed to be of a
   * specified minimum length. If {@code array} already has a length of at least {@code minLength},
   * it is returned directly. Otherwise, a new array of size {@code minLength + padding} is
   * returned, containing the values of {@code array}, and zeroes in the remaining places.
   *
   * @param array the source array
   * @param minLength the minimum length the returned array must guarantee
   * @param padding an extra amount to "grow" the array by if growth is necessary
   * @throws IllegalArgumentException if {@code minLength} or {@code padding} is negative
   * @return an array containing the values of {@code array}, with guaranteed minimum length {@code
   *     minLength}
   */
  public static @PolySigned byte[] ensureCapacity(@PolySigned byte[] array, @NonNegative int minLength, @NonNegative int padding) {
    checkArgument(minLength >= 0, "Invalid minLength: %s", minLength);
    checkArgument(padding >= 0, "Invalid padding: %s", padding);
    return (array.length < minLength) ? Arrays.copyOf(array, minLength + padding) : array;
  }

  /**
   * Returns an array containing each value of {@code collection}, converted to a {@code byte} value
   * in the manner of {@link Number#byteValue}.
   *
   * <p>Elements are copied from the argument collection as if by {@code collection.toArray()}.
   * Calling this method is as thread-safe as calling that method.
   *
   * @param collection a collection of {@code Number} instances
   * @return an array containing the same values as {@code collection}, in the same order, converted
   *     to primitives
   * @throws NullPointerException if {@code collection} or any of its elements is null
   * @since 1.0 (parameter was {@code Collection<Byte>} before 12.0)
   */
  @SuppressWarnings("signedness:return") // Signed byte to PolySigned byte is ok
  public static <T extends Number> @PolySigned byte[] toArray(Collection<@PolySigned T> collection) {
    if (collection instanceof ByteArrayAsList) {
      return ((ByteArrayAsList) collection).toByteArray();
    }

    @PolySigned Object[] boxedArray = collection.toArray();
    int len = boxedArray.length;
    @PolySigned byte[] array = new byte[len];
    for (int i = 0; i < len; i++) {
      // checkNotNull for GWT (do not optimize)
      array[i] = ((Number) checkNotNull(boxedArray[i])).byteValue();
    }
    return array;
  }

  /**
   * Returns a fixed-size list backed by the specified array, similar to {@link
   * Arrays#asList(Object[])}. The list supports {@link List#set(int, Object)}, but any attempt to
   * set a value to {@code null} will result in a {@link NullPointerException}.
   *
   * <p>The returned list maintains the values, but not the identities, of {@code Byte} objects
   * written to or read from it. For example, whether {@code list.get(0) == list.get(0)} is true for
   * the returned list is unspecified.
   *
   * @param backingArray the array to back the list
   * @return a list view of the array
   */
  @SuppressWarnings({"signedness:argument", "signedness:return"}) // non-generic container class
  public static List<@PolySigned Byte> asList(@PolySigned byte... backingArray) {
    if (backingArray.length == 0) {
      return Collections.emptyList();
    }
    return new ByteArrayAsList(backingArray);
  }

  @CFComment({"signedness: A non-generic container class permits only signed values.",
              "Clients must suppress warnings when storing unsigned values."})
  @GwtCompatible
  private static class ByteArrayAsList extends AbstractList<Byte>
      implements RandomAccess, Serializable {
    @HasSubsequence(subsequence="this", from="this.start", to="this.end")
    final byte @MinLen(1) [] array;
    final @IndexFor("array") @LessThan("this.end") int start;
    final @IndexOrHigh("array") int end;

    ByteArrayAsList(byte @MinLen(1)[] array) {
      this(array, 0, array.length);
    }

    @SuppressWarnings(
        "index" // these three fields need to be initialized in some order, and any ordering
    // leads to the first two issuing errors - since each field is dependent on
    // at least one of the others
    )
    ByteArrayAsList(byte @MinLen(1)[] array, @IndexFor("#1") @LessThan("#3") int start, @IndexOrHigh("#1") int end) {
      this.array = array;
      this.start = start;
      this.end = end;
    }

    @Override
    public @Positive @LTLengthOf(value = {"this","array"}, offset = {"-1","start - 1"}) int size() { // INDEX: Annotation on a public method refers to private member.
      return end - start;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public Byte get(@IndexFor("this") int index) {
      checkElementIndex(index, size());
      return array[start + index];
    }

    @Override
    public boolean contains(@CheckForNull @UnknownSignedness Object target) {
      // Overridden to prevent a ton of boxing
      return (target instanceof Byte) && Bytes.indexOf(array, (Byte) target, start, end) != -1;
    }

    @Override
    public @IndexOrLow("this") int indexOf(@CheckForNull @UnknownSignedness Object target) {
      // Overridden to prevent a ton of boxing
      if (target instanceof Byte) {
        int i = Bytes.indexOf(array, (Byte) target, start, end);
        if (i >= 0) {
          return i - start;
        }
      }
      return -1;
    }

    @Override
    public @IndexOrLow("this") int lastIndexOf(@CheckForNull @UnknownSignedness Object target) {
      // Overridden to prevent a ton of boxing
      if (target instanceof Byte) {
        int i = Bytes.lastIndexOf(array, (Byte) target, start, end);
        if (i >= 0) {
          return i - start;
        }
      }
      return -1;
    }

    @Override
    public Byte set(@IndexFor("this") int index, Byte element) {
      checkElementIndex(index, size());
      byte oldValue = array[start + index];
      // checkNotNull for GWT (do not optimize)
      array[start + index] = checkNotNull(element);
      return oldValue;
    }

    @Override
    @SuppressWarnings("index") // needs https://github.com/kelloggm/checker-framework/issues/229
    public List<Byte> subList(@IndexOrHigh("this") int fromIndex, @IndexOrHigh("this") int toIndex) {
      int size = size();
      checkPositionIndexes(fromIndex, toIndex, size);
      if (fromIndex == toIndex) {
        return Collections.emptyList();
      }
      return new ByteArrayAsList(array, start + fromIndex, start + toIndex);
    }

    @Override
    public boolean equals(@CheckForNull @UnknownSignedness Object object) {
      if (object == this) {
        return true;
      }
      if (object instanceof ByteArrayAsList) {
        ByteArrayAsList that = (ByteArrayAsList) object;
        int size = size();
        if (that.size() != size) {
          return false;
        }
        for (int i = 0; i < size; i++) {
          if (array[start + i] != that.array[that.start + i]) {
            return false;
          }
        }
        return true;
      }
      return super.equals(object);
    }

    @Override
    public int hashCode(@UnknownSignedness ByteArrayAsList this) {
      int result = 1;
      for (int i = start; i < end; i++) {
        result = 31 * result + Bytes.hashCode(array[i]);
      }
      return result;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder(size() * 5);
      builder.append('[').append(array[start]);
      for (int i = start + 1; i < end; i++) {
        builder.append(", ").append(array[i]);
      }
      return builder.append(']').toString();
    }

    byte[] toByteArray() {
      return Arrays.copyOfRange(array, start, end);
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Reverses the elements of {@code array}. This is equivalent to {@code
   * Collections.reverse(Bytes.asList(array))}, but is likely to be more efficient.
   *
   * @since 23.1
   */
  public static void reverse(byte[] array) {
    checkNotNull(array);
    reverse(array, 0, array.length);
  }

  /**
   * Reverses the elements of {@code array} between {@code fromIndex} inclusive and {@code toIndex}
   * exclusive. This is equivalent to {@code
   * Collections.reverse(Bytes.asList(array).subList(fromIndex, toIndex))}, but is likely to be more
   * efficient.
   *
   * @throws IndexOutOfBoundsException if {@code fromIndex < 0}, {@code toIndex > array.length}, or
   *     {@code toIndex > fromIndex}
   * @since 23.1
   */
  public static void reverse(byte[] array, @IndexOrHigh("#1") int fromIndex, @IndexOrHigh("#1") int toIndex) {
    checkNotNull(array);
    checkPositionIndexes(fromIndex, toIndex, array.length);
    for (int i = fromIndex, j = toIndex - 1; i < j; i++, j--) {
      byte tmp = array[i];
      array[i] = array[j];
      array[j] = tmp;
    }
  }
}
