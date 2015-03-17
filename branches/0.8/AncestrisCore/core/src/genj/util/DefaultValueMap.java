/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2010 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A map that never returns nulls
 */
public class DefaultValueMap<Key,Value> implements Map<Key, Value> {

  private Object defaultValue;
  private Map<Key,Value> delegate;
 
  public DefaultValueMap(Map<Key,Value> delegate, Value defaultValue) {
    this.delegate = delegate;
    this.defaultValue = defaultValue;
    getDefault();
  }
  
  @SuppressWarnings("unchecked")
  protected Value getDefault() {
    try {
      return (Value)defaultValue.getClass().getMethod("clone").invoke(defaultValue);
    } catch (Throwable t) {
      throw new IllegalArgumentException("default value must be cloneable", t);
    }
  }

  public void clear() {
    delegate.clear();
  }

  public boolean containsKey(Object key) {
    return delegate.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return delegate.containsValue(value);
  }

  public Set<java.util.Map.Entry<Key, Value>> entrySet() {
    return delegate.entrySet();
  }

  @SuppressWarnings("unchecked")
  public Value get(Object key) {
    Value val = delegate.get(key);
    if (val==null) {
      val = getDefault();
      delegate.put((Key)key, val);
    }
    return val;
  }

  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  public Set<Key> keySet() {
    return delegate.keySet();
  }

  public Value put(Key key, Value value) {
    return delegate.put(key, value);
  }

  public void putAll(Map<? extends Key, ? extends Value> m) {
    delegate.putAll(m);
  }

  public Value remove(Object key) {
    return delegate.remove(key);
  }

  public int size() {
    return delegate.size();
  }

  public Collection<Value> values() {
    return delegate.values();
  }

}
