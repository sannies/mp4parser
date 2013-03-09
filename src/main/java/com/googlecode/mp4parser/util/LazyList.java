package com.googlecode.mp4parser.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This lazy list tries to postpone the size() call as much as possible.
 */
public class LazyList<E> extends AbstractList<E> {

    private static final Logger LOG = Logger.getLogger(LazyList.class);

    List<E> underlying;
    Iterator<E> elementSource;

    public LazyList(List<E> underlying, Iterator<E> elementSource) {
        this.underlying = underlying;
        this.elementSource = elementSource;

    }

    public List<E> getUnderlying() {
        return underlying;
    }

    private void blowup() {
        LOG.logDebug("blowup running");
        while (elementSource.hasNext()) {
            underlying.add(elementSource.next());
        }

    }

    public E get(int i) {
        if (underlying.size() > i) {
            return underlying.get(i);
        } else {
            if (elementSource.hasNext()) {
                underlying.add(elementSource.next());
                return get(i);
            } else {
                throw new NoSuchElementException();
            }
        }

    }

    public Iterator<E> iterator() {
        return new Iterator<E>() {
            int pos = 0;

            public boolean hasNext() {
                return pos < underlying.size() || elementSource.hasNext();
            }

            public E next() {
                if (pos < underlying.size()) {
                    return underlying.get(pos++);
                } else {
                    underlying.add(elementSource.next());
                    return next();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    @Override
    public int size() {
        LOG.logDebug("potentially expensive size() call");
        blowup();
        return underlying.size();
    }
}
