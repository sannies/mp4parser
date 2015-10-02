package org.mp4parser.tools;

import java.util.*;

/**
 * Created by sannies on 10.09.2014.
 */
public class RangeStartMap<K extends Comparable, V> implements Map<K, V> {
    TreeMap<K, V> base = new TreeMap<K, V>(new Comparator<K>() {
        public int compare(K o1, K o2) {
            return -o1.compareTo(o2);
        }
    });

    public RangeStartMap() {
    }

    public RangeStartMap(K k, V v) {
        this.put(k, v);
    }

    public int size() {
        return base.size();
    }

    public boolean isEmpty() {
        return base.isEmpty();
    }

    public boolean containsKey(Object key) {
        return base.get(key) != null;
    }

    public boolean containsValue(Object value) {
        return false;
    }

    public V get(Object k) {
        if (!(k instanceof Comparable)) {
            return null;
        }
        Comparable<K> key = (Comparable<K>) k;
        if (isEmpty()) {
            return null;
        }
        Iterator<K> keys = base.keySet().iterator();
        K a = keys.next();
        do {
            if (keys.hasNext()) {
                if (key.compareTo(a) < 0) {
                    a = keys.next();
                } else {
                    return base.get(a);
                }
            } else {
                return base.get(a);
            }
        } while (true);
    }

    public V put(K key, V value) {
        return base.put(key, value);
    }

    public V remove(Object k) {
        if (!(k instanceof Comparable)) {
            return null;
        }
        Comparable<K> key = (Comparable<K>) k;
        if (isEmpty()) {
            return null;
        }
        Iterator<K> keys = base.keySet().iterator();
        K a = keys.next();
        do {
            if (keys.hasNext()) {
                if (key.compareTo(a) < 0) {
                    a = keys.next();
                } else {
                    return base.remove(a);
                }
            } else {
                return base.remove(a);
            }
        } while (true);
    }


    public void putAll(Map<? extends K, ? extends V> m) {
        base.putAll(m);
    }


    public void clear() {
        base.clear();
    }


    public Set<K> keySet() {
        return base.keySet();
    }


    public Collection<V> values() {
        return base.values();
    }


    public Set<Entry<K, V>> entrySet() {
        return base.entrySet();
    }
}
