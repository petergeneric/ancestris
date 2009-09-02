/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
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
package genj.chart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.TableXYDataset;

/**
 * An indexed series is an implementation for index/value pairs and very
 * well suitable for simple category/value statistics. Example:
 * <pre>
 *  IndexedSeries series = new IndexedSeries("DaysInMonths", 12);
 *  series.set( 0,31);
 *  series.set( 1,28); // not a leap year
 *  series.set( 2,31);
 *  series.set( 3,30);
 *  series.set( 4,31);
 *  series.set( 5,30);
 *  series.set( 6,31);
 *  series.set( 7,31);
 *  series.set( 8,30);
 *  series.set( 9,31);
 *  series.set(10,30);
 *  series.set(11,31);
 * </pre>
 * When collecting statistical data, an instance can be created
 * and then used as means of collecting what's analysed:
 * <pre>
 *  IndexedSeries series = new IndexedSeries("BirthInMonths", 12);
 *  series.inc(5-1); // another birth in may 
 *  series.inc(12-1); // found a birth in december
 *  ...
 * </pre>
 */
public class IndexedSeries {

  /** name */
  private String name;
  
  /** values */
  private float[] values;
  
  /** start */
  private int start;
  
  /**
   * Create an indexed series with new name but indexes as in template
   * @param name the name of this index
   * @param template the template to copy indexe characteristics from
   */
  public IndexedSeries(String name, IndexedSeries template) {
    this(name, template.start, template.values.length);
  }
  
  /**
   * Create an indexed series with new name and number of indexes
   * @param name the name of this index
   * @param size number of indexes
   */
  public IndexedSeries(String name, int size) {
    this(name,0,size);
  }
  
  /**
   * Create an indexed series with new name and index characteristics
   * @param name the name of this index
   * @param start first index
   * @param size number of indexes
   */
  public IndexedSeries(String name, int start, int size) {
    this.name = name;
    this.start = start;
    this.values = new float[size];
  }
  
  /**
   * Accssor - set current name
   * @param set the new name
   */
  public void setName(String set) {
    name = set;
  }
  
  /**
   * Accssor - get current name
   */
  public String getName() {
    return name;
  }
  
  /**
   * Accessor - get current value at given index
   * @param i the index
   */
  public float get(int i) {
    // apply start offset
    i = i-start;
    // return
    return values[i];
  }
  
  /**
   * Accessor - set current value at given index
   * @param i the index
   * @param val the new value
   */
  public void set(int i, float val) {
    // apply start offset
    i = i-start;
    // check and ignore if out of bounds
    if (i<0||i>=values.length)
      return;
    // return
    values[i] = val;
  }
  
  /**
   * Accessor - increment current value at given index
   * @param i the index
   */
  public void inc(int i) {
    // apply start offset
    i = i-start;
    // check and ignore if out of bounds
    if (i<0||i>=values.length)
      return;
    // return
    values[i]++;
  }
  
  /**
   * Accessor - decrement current value at given index
   * @param i the index
   */
  public void dec(int i) {
    // apply start offset
    i = i-start;
    // check and ignore if out of bounds
    if (i<0||i>=values.length)
      return;
    // return
    values[i]--;
  }
  
  /**
   * Convenient converter to get a list of series from
   * a dynamic collection containing indexed series
   */
  public static IndexedSeries[] toArray(Collection c) {
    return (IndexedSeries[])c.toArray(new IndexedSeries[c.size()]);
  }

  /**
   * Wrap into something JFreeChart can use
   */
  /*package*/ static PieDataset asPieDataset(IndexedSeries series, String[] categories) {
    return new PieDatasetImpl(series, categories);
  }
  
  /**
   * Wrap into something JFreeChart can use
   */
  /*package*/ static CategoryDataset asCategoryDataset(IndexedSeries[] series, String[] categories) {
    return new CategoryDatasetImpl(series, categories);
  }
  
  /**
   * Wrap into something JFreeChart can use
   */
  /*package*/ static TableXYDataset asTableXYDataset(IndexedSeries[] series) {
    return new TableXYDatasetImpl(series);
  }
  
  /** 
   * Wrapper for jfreechart PieDataSet
   */
  private static class PieDatasetImpl extends AbstractDataset implements PieDataset {
    
    /** series */
    private IndexedSeries series;
    
    private String[] categories;
    
    /** constructor */
    private PieDatasetImpl(IndexedSeries series, String[] categories) {
      this.series = series;
      this.categories = categories;
    }

    /** key for position is category */
    public Comparable getKey(int i) {
      return categories[i];
    }

    public int getIndex(Comparable key) {
      for (int i=0;i<categories.length;i++) {
        if (categories[i].equals(key))
          return i;
      }
      throw new IllegalArgumentException();
    }

    public List getKeys() {
      return Arrays.asList(categories);
    }

    public Number getValue(Comparable cat) {
      return getValue(getIndex(cat));
    }

    public int getItemCount() {
      return categories.length;
    }

    public Number getValue(int i) {
      return new Float(series.get(i));
    }
    
  } //PieDatasetImpl
  
  /** 
   * Wrapper for jfreechart TableXYDataSet
   */
  private static class TableXYDatasetImpl extends AbstractXYDataset implements TableXYDataset {
    
    /** series */
    private IndexedSeries[] series;
    
    /** range */
    private int start, length;
    
    /**
     * Constructor
     */
    public TableXYDatasetImpl(IndexedSeries[] series) {
      
      this.series = series;
      
      if (series.length>0) {
        start = series[0].start;
        length = series[0].values.length;

        for (int i=1;i<series.length;i++) {
          if (series[i].start!=start||series[i].values.length!=length)
            throw new IllegalArgumentException("series can't be combined into table dataset");
        }
        
      }
    }
    
    /**
     * THE TableXYDataset requirement - one equal item count
     */
    public int getItemCount() {
      return length;
    }
    
    /**
     * # of series
     */
    public int getSeriesCount() {
      return series.length;
    }

    /**
     * series by index 
     */
    public String getSeriesName(int s) {
      return series[s].name;
    }

    /**
     * # of items in series
     */
    public int getItemCount(int s) {
      return length;
    }

    /**
     * item x for seriex
     */
    public Number getX(int s, int i) {
      return new Integer(start + i);
    }

    /**
     * item y for seriex
     */
    public Number getY(int s, int i) {
      return new Float(series[s].get(start+i));
    }

	@Override
	public Comparable getSeriesKey(int series) {
		
		// if the name can be considered as the series key ...
		  return this.series[series].name;
	}

  } //TableXYDatasetImpl
  
  /** 
   * Wrapper for jfreechart Category DataSet
   */
  private static class CategoryDatasetImpl extends AbstractDataset implements CategoryDataset {
    
    /** series */
    private IndexedSeries[] series;
    
    /** categories */
    private String[] categories;
    
    /** constructor */
    private CategoryDatasetImpl(IndexedSeries[] series, String[] categories) {
      this.series = series;
      this.categories = categories;
      
      for (int i=0;i<series.length;i++) {
        if (series[i].values.length!=categories.length)
          throw new IllegalArgumentException("series doesn't match categories");
      }
    }

    /** row for index */
    public Comparable getRowKey(int row) {
      return series[row].name;
    }

    /** index for row */
    public int getRowIndex(Comparable row) {
      for (int i=0; i<series.length; i++) 
        if (series[i].name.equals(row)) 
          return i;
      throw new IllegalArgumentException();
    }

    /** all row keys  */
    public List getRowKeys() {
      ArrayList result = new ArrayList();
      for (int i=0;i<series.length;i++)
        result.add(series[i].name);
      return result;
    }

    /** column for index */
    public Comparable getColumnKey(int col) {
      return categories[col];
    }

    /** index for column */
    public int getColumnIndex(Comparable col) {
      for (int i=0; i<categories.length; i++) 
        if (categories[i].equals(col)) return i;
      throw new IllegalArgumentException();
    }

    /** all columns */
    public List getColumnKeys() {
      return Arrays.asList(categories);
    }

    /** value for col, row */
    public Number getValue(Comparable row, Comparable col) {
      return getValue(getRowIndex(row), getColumnIndex(col));
    }

    /** value for col, row */
    public Number getValue(int row, int col) {
      return new Float(series[row].get(col));
    }
    
    /** #rows */
    public int getRowCount() {
      return series.length;
    }

    /** #cols */
    public int getColumnCount() {
      return categories.length;
    }
    
  } //Wrapper

} //CategorySeries