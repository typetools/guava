/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Objects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.framework.qual.AnnotatedFor;

/**
 * A map which forwards all its method calls to another map. Subclasses should override one or more
 * methods to modify the behavior of the backing map as desired per the <a
 * href="http://en.wikipedia.org/wiki/Decorator_pattern">decorator pattern</a>.
 *
 * <p><b>Warning:</b> The methods of {@code ForwardingMap} forward <i>indiscriminately</i> to the
 * methods of the delegate. For example, overriding {@link #put} alone <i>will not</i> change the
 * behavior of {@link #putAll}, which can lead to unexpected behavior. In this case, you should
 * override {@code putAll} as well, either providing your own implementation, or delegating to the
 * provided {@code standardPutAll} method.
 *
 * <p><b>{@code default} method warning:</b> This class does <i>not</i> forward calls to {@code
 * default} methods. Instead, it inherits their default implementations. When those implementations
 * invoke methods, they invoke methods on the {@code ForwardingMap}.
 *
 * <p>Each of the {@code standard} methods, where appropriate, use {@link Objects#equal} to test
 * equality for both keys and values. This may not be the desired behavior for map implementations
 * that use non-standard notions of key equality, such as a {@code SortedMap} whose comparator is
 * not consistent with {@code equals}.
 *
 * <p>The {@code standard} methods and the collection views they return are not guaranteed to be
 * thread-safe, even when all of the methods that they depend on are thread-safe.
 *
 * @author Kevin Bourrillion
 * @author Jared Levy
 * @author Louis Wasserman
 * @since 2.0
 */
@AnnotatedFor({"nullness"})
@GwtCompatible
@ElementTypesAreNonnullByDefault
public abstract class ForwardingMap<K extends @Nullable Object, V extends @Nullable Object>
    extends ForwardingObject implements Map<K, V> {
  // TODO(lowasser): identify places where thread safety is actually lost

  /** Constructor for use by subclasses. */
  protected ForwardingMap() {}

  @Override
  protected abstract Map<K, V> delegate();

  @Pure
  @Override
  @SuppressWarnings("index:overriding.return")
  public @NonNegative int size() {
    return delegate().size();
  }

  @Pure
  @Override
  public boolean isEmpty() {
    return delegate().isEmpty();
  }

  @CanIgnoreReturnValue
  @Override
  @SuppressWarnings("nullness:argument") // Suppressed due to annotations on remove in Java.Map
  @CheckForNull
  public V remove(@CheckForNull @UnknownSignedness Object key) {
    return delegate().remove(key);
  }

  @Override
  public void clear() {
    delegate().clear();
  }

  @Pure
  @Override
  @SuppressWarnings("nullness:argument") // Suppressed due to annotations on containsKey in Java.Map
  public boolean containsKey(@CheckForNull @UnknownSignedness Object key) {
    return delegate().containsKey(key);
  }

  @Pure
  @Override
  @SuppressWarnings("nullness:argument") // Suppressed due to annotations on containsValue in Java.Map
  public boolean containsValue(@CheckForNull @UnknownSignedness Object value) {
    return delegate().containsValue(value);
  }

  @Override
  @SuppressWarnings("nullness:argument") // Suppressed due to annotations on get in Java.Map
  @CheckForNull
  public V get(@CheckForNull @UnknownSignedness Object key) {
    return delegate().get(key);
  }

  @CanIgnoreReturnValue
  @Override
  @CheckForNull
  public V put(@ParametricNullness K key, @ParametricNullness V value) {
    return delegate().put(key, value);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> map) {
    delegate().putAll(map);
  }

  @SideEffectFree
  @Override
  public Set<@KeyFor({"this"}) K> keySet() {
    return delegate().keySet();
  }

  @SideEffectFree
  @Override
  public Collection<V> values() {
    return delegate().values();
  }

  @SideEffectFree
  @Override
  public Set<Entry<@KeyFor({"this"}) K, V>> entrySet() {
    return delegate().entrySet();
  }

  @Pure
  @Override
  public boolean equals(@CheckForNull Object object) {
    return object == this || delegate().equals(object);
  }

  @Pure
  @Override
  public int hashCode(@UnknownSignedness ForwardingMap<K, V> this) {
    return delegate().hashCode();
  }

  /**
   * A sensible definition of {@link #putAll(Map)} in terms of {@link #put(Object, Object)}. If you
   * override {@link #put(Object, Object)}, you may wish to override {@link #putAll(Map)} to forward
   * to this implementation.
   *
   * @since 7.0
   */
  protected void standardPutAll(Map<? extends K, ? extends V> map) {
    Maps.putAllImpl(this, map);
  }

  /**
   * A sensible, albeit inefficient, definition of {@link #remove} in terms of the {@code iterator}
   * method of {@link #entrySet}. If you override {@link #entrySet}, you may wish to override {@link
   * #remove} to forward to this implementation.
   *
   * <p>Alternately, you may wish to override {@link #remove} with {@code keySet().remove}, assuming
   * that approach would not lead to an infinite loop.
   *
   * @since 7.0
   */
  @Beta
  @CheckForNull
  protected V standardRemove(@CheckForNull Object key) {
    Iterator<Entry<K, V>> entryIterator = entrySet().iterator();
    while (entryIterator.hasNext()) {
      Entry<K, V> entry = entryIterator.next();
      if (Objects.equal(entry.getKey(), key)) {
        V value = entry.getValue();
        entryIterator.remove();
        return value;
      }
    }
    return null;
  }

  /**
   * A sensible definition of {@link #clear} in terms of the {@code iterator} method of {@link
   * #entrySet}. In many cases, you may wish to override {@link #clear} to forward to this
   * implementation.
   *
   * @since 7.0
   */
  protected void standardClear() {
    Iterators.clear(entrySet().iterator());
  }

  /**
   * A sensible implementation of {@link Map#keySet} in terms of the following methods: {@link
   * ForwardingMap#clear}, {@link ForwardingMap#containsKey}, {@link ForwardingMap#isEmpty}, {@link
   * ForwardingMap#remove}, {@link ForwardingMap#size}, and the {@link Set#iterator} method of
   * {@link ForwardingMap#entrySet}. In many cases, you may wish to override {@link
   * ForwardingMap#keySet} to forward to this implementation or a subclass thereof.
   *
   * @since 10.0
   */
  @Beta
  protected class StandardKeySet extends Maps.KeySet<K, V> {
    /** Constructor for use by subclasses. */
    public StandardKeySet() {
      super(ForwardingMap.this);
    }
  }

  /**
   * A sensible, albeit inefficient, definition of {@link #containsKey} in terms of the {@code
   * iterator} method of {@link #entrySet}. If you override {@link #entrySet}, you may wish to
   * override {@link #containsKey} to forward to this implementation.
   *
   * @since 7.0
   */
  @Beta
  protected boolean standardContainsKey(@CheckForNull Object key) {
    return Maps.containsKeyImpl(this, key);
  }

  /**
   * A sensible implementation of {@link Map#values} in terms of the following methods: {@link
   * ForwardingMap#clear}, {@link ForwardingMap#containsValue}, {@link ForwardingMap#isEmpty},
   * {@link ForwardingMap#size}, and the {@link Set#iterator} method of {@link
   * ForwardingMap#entrySet}. In many cases, you may wish to override {@link ForwardingMap#values}
   * to forward to this implementation or a subclass thereof.
   *
   * @since 10.0
   */
  @Beta
  protected class StandardValues extends Maps.Values<K, V> {
    /** Constructor for use by subclasses. */
    public StandardValues() {
      super(ForwardingMap.this);
    }
  }

  /**
   * A sensible definition of {@link #containsValue} in terms of the {@code iterator} method of
   * {@link #entrySet}. If you override {@link #entrySet}, you may wish to override {@link
   * #containsValue} to forward to this implementation.
   *
   * @since 7.0
   */
  protected boolean standardContainsValue(@CheckForNull Object value) {
    return Maps.containsValueImpl(this, value);
  }

  /**
   * A sensible implementation of {@link Map#entrySet} in terms of the following methods: {@link
   * ForwardingMap#clear}, {@link ForwardingMap#containsKey}, {@link ForwardingMap#get}, {@link
   * ForwardingMap#isEmpty}, {@link ForwardingMap#remove}, and {@link ForwardingMap#size}. In many
   * cases, you may wish to override {@link #entrySet} to forward to this implementation or a
   * subclass thereof.
   *
   * @since 10.0
   */
  @Beta
  protected abstract class StandardEntrySet extends Maps.EntrySet<K, V> {
    /** Constructor for use by subclasses. */
    public StandardEntrySet() {}

    @Override
    Map<K, V> map() {
      return ForwardingMap.this;
    }
  }

  /**
   * A sensible definition of {@link #isEmpty} in terms of the {@code iterator} method of {@link
   * #entrySet}. If you override {@link #entrySet}, you may wish to override {@link #isEmpty} to
   * forward to this implementation.
   *
   * @since 7.0
   */
  protected boolean standardIsEmpty() {
    return !entrySet().iterator().hasNext();
  }

  /**
   * A sensible definition of {@link #equals} in terms of the {@code equals} method of {@link
   * #entrySet}. If you override {@link #entrySet}, you may wish to override {@link #equals} to
   * forward to this implementation.
   *
   * @since 7.0
   */
  protected boolean standardEquals(@CheckForNull Object object) {
    return Maps.equalsImpl(this, object);
  }

  /**
   * A sensible definition of {@link #hashCode} in terms of the {@code iterator} method of {@link
   * #entrySet}. If you override {@link #entrySet}, you may wish to override {@link #hashCode} to
   * forward to this implementation.
   *
   * @since 7.0
   */
  protected int standardHashCode() {
    return Sets.hashCodeImpl(entrySet());
  }

  /**
   * A sensible definition of {@link #toString} in terms of the {@code iterator} method of {@link
   * #entrySet}. If you override {@link #entrySet}, you may wish to override {@link #toString} to
   * forward to this implementation.
   *
   * @since 7.0
   */
  protected String standardToString() {
    return Maps.toStringImpl(this);
  }
}
