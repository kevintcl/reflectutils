/**
 * $Id$
 * $URL$
 * ArrayOrderedMap.java - genericdao - May 5, 2008 2:16:35 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2
 * 
 * A copy of the Apache License, Version 2 has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.azeckoski.reflectutils.map;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A map which keeps track of the order the entries are added 
 * and allows retrieval of entries in the order they were entered as well,
 * this is NOT safe for multi-threaded access,
 * this is backed by a {@link HashMap} and {@link ArrayList}
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ArrayOrderedMap<K, V> extends HashMap<K, V> implements OrderedMap<K, V> {
    public static final long serialVersionUID = 1l;

    private ArrayList<K> list;

    public ArrayOrderedMap() {
        this(10);
    }

    public ArrayOrderedMap(int initialCapacity) {
        super(initialCapacity);
        list = new ArrayList<K>(initialCapacity);
    }

    public ArrayOrderedMap(Map<K, V> map) {
        this(map.size());
        for (Entry<K, V> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }


    private String name = "entity";
    public String getName() {
        return name;
    }
    /**
     * @param name the name to use when encoding this map of entities
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return a list of all the keys in this map in the order they were entered
     */
    public List<K> getKeys() {
        return new ArrayList<K>(list);
    }

    /* (non-Javadoc)
     * @see org.azeckoski.reflectutils.map.OrderedMap#getValues()
     */
    public List<V> getValues() {
        return new ArrayList<V>( values() );
    }

    /**
     * @return a list of all the entries in this map in the order they were created
     */
    public List<Entry<K, V>> getEntries() {
        ArrayList<Entry<K, V>> entries = new ArrayList<Entry<K,V>>();
        for (K key : list) {
            Entry<K, V> entry = new SimpleEntry<K,V>(key, this.get(key));
            entries.add(entry);
        }
        return entries;
    }

    /**
     * Get an entry based on the position it is in the map (based on the order entries were created)
     * @param position the position in the map (must be less that the size)
     * @return the entry at that position
     * @throws IllegalArgumentException if the position is greater than the map size
     */
    public Entry<K, V> getEntry(int position) {
        if (position >= list.size()) {
            throw new IllegalArgumentException("Value is too large for the map size: " + list.size());
        }
        K key = list.get(position);
        Entry<K, V> entry = new SimpleEntry<K,V>(key, this.get(key));
        return entry;
    }

    @Override
    public V put(K key, V value) {
        V v = super.put(key, value);
        if (v != null) {
            // displaced
            list.remove(key);
        }
        list.add(key);
        return v;
    }

    @Override
    public V remove(Object key) {
        V v = super.remove(key);
        if (v != null) {
            list.remove(key);
        }
        return v;
    }

    @Override
    public void clear() {
        super.clear();
        list.clear();
    }

    public Enumeration<K> keys() {
        return new KeyIterator();
    }

    public Enumeration<V> elements() {
        return new ValueIterator();
    }

    transient Set<K> keySet;
    transient Set<Map.Entry<K,V>> entrySet;
    transient Collection<V> values;

    @Override
    public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet());
    }

    @Override
    public Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null) ? vs : (values = new Values());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es = entrySet;
        return (es != null) ? es : (entrySet = (Set<Map.Entry<K,V>>) (Set) new EntrySet());
    }

    // Iterator support

    abstract class CoreIterator {

        int currentPos = -1;
        Entry<K, V> lastReturned = null;
        private List<Entry<K, V>> entries = ArrayOrderedMap.this.getEntries();

        public boolean hasMore() {
            if ((currentPos + 1) < entries.size()) {
                return true;
            }
            return false;
        }

        public Entry<K, V> getNext() {
            currentPos++;
            try {
                lastReturned = entries.get(currentPos);
            } catch (RuntimeException e) {
                throw new NoSuchElementException("There are no more items available to get, the last one was reached");
            }
            return lastReturned;
        }

        public void removeCurrent() {
            if (currentPos < 0) {
                throw new IllegalArgumentException("Have not called next yet, cannot remove from this iterator");
            }
            entries.remove(currentPos);
            ArrayOrderedMap.this.remove(lastReturned.getKey());
        }

        // shared methods
        public boolean hasNext() {          return hasMore(); }
        public boolean hasMoreElements() {  return hasMore(); }
        public void remove() {              removeCurrent(); }

    }

    final class KeyIterator extends CoreIterator implements Iterator<K>, Enumeration<K> {
        public K next() {                   return super.getNext().getKey(); }
        public K nextElement() {            return next(); }
    }

    final class ValueIterator extends CoreIterator implements Iterator<V>, Enumeration<V> {
        public V next() {                   return super.getNext().getValue(); }
        public V nextElement() {            return next(); }
    }

    final class EntryIterator extends CoreIterator implements Iterator<Entry<K,V>>, Enumeration<Entry<K,V>> {
        public Entry<K,V> next() {                   return super.getNext(); }
        public Entry<K,V> nextElement() {            return next(); }
    }

    // All below copied from CHM

    final class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return new KeyIterator();
        }
        public int size() {
            return ArrayOrderedMap.this.size();
        }
        public boolean contains(Object o) {
            return ArrayOrderedMap.this.containsKey(o);
        }
        public boolean remove(Object o) {
            return ArrayOrderedMap.this.remove(o) != null;
        }
        public void clear() {
            ArrayOrderedMap.this.clear();
        }
        public Object[] toArray() {
            Collection<K> c = new ArrayList<K>();
            for (Iterator<K> i = iterator(); i.hasNext(); )
                c.add(i.next());
            return c.toArray();
        }
        public <T> T[] toArray(T[] a) {
            Collection<K> c = new ArrayList<K>();
            for (Iterator<K> i = iterator(); i.hasNext(); )
                c.add(i.next());
            return c.toArray(a);
        }
    }

    final class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator();
        }
        public int size() {
            return ArrayOrderedMap.this.size();
        }
        public boolean contains(Object o) {
            return ArrayOrderedMap.this.containsValue(o);
        }
        public void clear() {
            ArrayOrderedMap.this.clear();
        }
        public Object[] toArray() {
            Collection<V> c = new ArrayList<V>();
            for (Iterator<V> i = iterator(); i.hasNext(); )
                c.add(i.next());
            return c.toArray();
        }
        public <T> T[] toArray(T[] a) {
            Collection<V> c = new ArrayList<V>();
            for (Iterator<V> i = iterator(); i.hasNext(); )
                c.add(i.next());
            return c.toArray(a);
        }
    }

    @SuppressWarnings("unchecked")
    final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<K,V> e = (Map.Entry<K,V>)o;
            V v = ArrayOrderedMap.this.get(e.getKey());
            return v != null && v.equals(e.getValue());
        }
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<K,V> e = (Map.Entry<K,V>)o;
            return ArrayOrderedMap.this.remove(e.getKey()) != null;
        }
        public int size() {
            return ArrayOrderedMap.this.size();
        }
        public void clear() {
            ArrayOrderedMap.this.clear();
        }
        public Object[] toArray() {
            // Since we don't ordinarily have distinct Entry objects, we
            // must pack elements using exportable SimpleEntry
            Collection<Map.Entry<K,V>> c = new ArrayList<Map.Entry<K,V>>(size());
            for (Iterator<Map.Entry<K,V>> i = iterator(); i.hasNext(); )
                c.add(new SimpleEntry<K,V>(i.next()));
            return c.toArray();
        }
        public <T> T[] toArray(T[] a) {
            Collection<Map.Entry<K,V>> c = new ArrayList<Map.Entry<K,V>>(size());
            for (Iterator<Map.Entry<K,V>> i = iterator(); i.hasNext(); )
                c.add(new SimpleEntry<K,V>(i.next()));
            return c.toArray(a);
        }

    }


    /**
     * This duplicates java.util.AbstractMap.SimpleEntry until this class
     * is made accessible.
     */
    static final class SimpleEntry<K,V> implements Entry<K,V> {
        K key;
        V value;

        public SimpleEntry(K key, V value) {
            this.key   = key;
            this.value = value;
        }

        public SimpleEntry(Entry<K,V> e) {
            this.key   = e.getKey();
            this.value = e.getValue();
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @SuppressWarnings("unchecked")
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry e = (Map.Entry)o;
            return eq(key, e.getKey()) && eq(value, e.getValue());
        }

        public int hashCode() {
            return ((key   == null)   ? 0 :   key.hashCode()) ^
            ((value == null)   ? 0 : value.hashCode());
        }

        public String toString() {
            return key + "=" + value;
        }

        static boolean eq(Object o1, Object o2) {
            return (o1 == null ? o2 == null : o1.equals(o2));
        }
    }

}
