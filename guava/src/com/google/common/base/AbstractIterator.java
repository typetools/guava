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

package com.google.common.base;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Note this class is a copy of {@link com.google.common.collect.AbstractIterator} (for dependency
 * reasons).
 */
@GwtCompatible
abstract class AbstractIterator<T> implements Iterator<T> {
  private State state = State.NOT_READY;

  protected AbstractIterator() {}

  private enum State {
    READY,
    NOT_READY,
    DONE,
    FAILED,
  }

  private @Nullable T next;

  protected abstract T computeNext();

  @CanIgnoreReturnValue
  protected final @Nullable T endOfData() {
    state = State.DONE;
    return null;
  }

  @Override
  public final boolean hasNext() {
    checkState(state != State.FAILED);
    switch (state) {
      case READY:
        return true;
      case DONE:
        return false;
      default:
    }
    return tryToComputeNext();
  }

  private boolean tryToComputeNext() {
    state = State.FAILED; // temporary pessimism
    next = computeNext();
    if (state != State.DONE) {
      state = State.READY;
      return true;
    }
    return false;
  }

  @Override
  @SuppressWarnings("return.type.incompatible") // warning is being issued for difference in super
  // bound for next and the expected return type. If type is qualified with @Nullable annotation
  // during instantiation then there should be no discrepancy as both will have @Nullable as lower
  // bound. While in case it is qualified to be @NonNull, result will always be allocated a non-null
  // value for it is being called after hasNext method. Which ensures that next contains reference to
  // next element. In case there is no next element hasNext returns false thus leading to exception:
  // NoSuchElementException
  public final T next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    state = State.NOT_READY;
    T result = next;
    next = null;
    return result;
  }

  @Override
  public final void remove() {
    throw new UnsupportedOperationException();
  }
}
