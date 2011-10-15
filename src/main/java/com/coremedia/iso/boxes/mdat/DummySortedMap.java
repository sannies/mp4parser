package com.coremedia.iso.boxes.mdat;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A SortedSet that contains just one value.
 */
public class DummySortedMap<K, V> implements SortedMap<K, V> {
    SortedSet<K> keys = new TreeSet<K>() {
    };
    V value;

    public DummySortedMap(SortedSet<K> keys, V value) {
        this.keys = keys;
        this.value = value;
    }

    public DummySortedMap(V value) {
        this.value = value;
    }

    public Comparator<? super K> comparator() {
        return null;  // I don't have any
    }

    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return new DummySortedMap<K, V>(keys.subSet(fromKey, toKey), value);
    }

    public SortedMap<K, V> headMap(K toKey) {
        return new DummySortedMap<K, V>(keys.headSet(toKey), value);
    }

    public SortedMap<K, V> tailMap(K fromKey) {
        return new DummySortedMap<K, V>(keys.tailSet(fromKey), value);
    }

    public K firstKey() {
        return keys.first();
    }

    public K lastKey() {
        return keys.last();
    }

    public int size() {
        return keys.size();
    }

    public boolean isEmpty() {
        return keys.isEmpty();
    }

    public boolean containsKey(Object key) {
        return keys.contains(key);
    }

    public boolean containsValue(Object value) {
        return this.value == value;
    }

    public V get(Object key) {
        return keys.contains(key) ? value : null;
    }

    public V put(K key, V value) {
        assert this.value == value;
        keys.add(key);
        return this.value;
    }

    public V remove(Object key) {
        V v = get(key);
        keys.remove(key);
        return v;
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        for (K k : m.keySet()) {
            assert m.get(k) == value;
            this.keys.add(k);
        }
    }

    public void clear() {
        keys.clear();
    }

    public Set<K> keySet() {
        return keys;
    }

    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
