/*
 * Copyright (C) 2012 The Guava Authors
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

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Predicate;
import java.util.List;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Implementation of {@link Multimaps#filterKeys(ListMultimap, Predicate)}.
 *
 * @author Louis Wasserman
 */
@GwtCompatible
// FilterKeyListMultimap may accept @Nullable Keys if the implementation provided for keyPredicate
// allows @Nullable input
final class FilteredKeyListMultimap<K extends @NonNull Object, V> extends FilteredKeyMultimap<K, V>
    implements ListMultimap<K, V> {
  FilteredKeyListMultimap(ListMultimap<K, V> unfiltered, Predicate<? super K> keyPredicate) {
    super(unfiltered, keyPredicate);
  }

  @Override
  public ListMultimap<K, V> unfiltered() {
    return (ListMultimap<K, V>) super.unfiltered();
  }

  @Override
  @SuppressWarnings("nullness:override.param.invalid") // This method may accept @Nullable value
  // if the provided keyPredicate allows @Nullable inputs
  public List<V> get(K key) {
    return (List<V>) super.get(key);
  }

  @Override
  public List<V> removeAll(@Nullable Object key) {
    return (List<V>) super.removeAll(key);
  }

  @Override
  public List<V> replaceValues(K key, Iterable<? extends V> values) {
    return (List<V>) super.replaceValues(key, values);
  }
}
