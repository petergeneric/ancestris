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
 * 
 * $Revision: 1.8 $ $Author: badisgood $ $Date: 2009-08-24 09:33:28 $
 */
package genj.chart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.NumberFormat;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.ui.RectangleInsets;

/**
 * A Chart in GenJ can be created by instantiating an object of this
 * type and then displaying it as standard JComponent. The constructors
 * each address a different charting type.
 */
public class Chart extends JPanel {
  
  /**
   * Initializer
   */
  private void init(String title, Plot plot, boolean legend) {
    setLayout(new BorderLayout());
    ChartPanel panel = new ChartPanel(new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend));
//    panel.setHorizontalZoom(true);
    panel.setDomainZoomable(true);
    
//    panel.setVerticalZoom(true);
    panel.setRangeZoomable(true);
    
    add(panel, BorderLayout.CENTER);
  }
  
  /**
   * Constructor for a chart with indexed series of data, shown on a 2d pane
   * as one plot per series or one horizontal stripe per series (stacked).
   * @param title the title of the chart
   * @param labelAxisX a label for the x-axis
   * @param labelAxisY a label for the y-axis
   * @param series one or more indexed series to show
   * @param stacked whether to stack horizontal stripes one for each series instead of showing one plot per series
   */
  public Chart(String title, String labelAxisX, String labelAxisY, IndexedSeries[] series, NumberFormat format, boolean stacked) {

    // prepare chart setup
    NumberAxis xAxis = new NumberAxis(labelAxisX);
    xAxis.setAutoRangeIncludesZero(false);
    
    NumberAxis yAxis = new NumberAxis(labelAxisY);
    yAxis.setNumberFormatOverride(format);
    
    XYItemRenderer renderer = stacked ? new StackedXYAreaRenderer2() : new XYAreaRenderer();
    XYPlot plot = new XYPlot(IndexedSeries.asTableXYDataset(series), xAxis, yAxis, renderer);

    // init
    init(title, plot, true);
    
    // done
  }
  
  /**
   * Constructor for a chart with x/y series of data shown on a 2d pane
   * as one plot per series. Note: The difference between this and
   * the first constructor is that of indexed series (where all x-values
   * are shared indexes) and arbitrary series.
   * @param title the title of the chart
   * @param labelAxisX a label for the x-axis
   * @param labelAxisY a label for the y-axis
   * @param series one or more x/y series to show
   * @param format a number format to use for x-values
   * @param shapes whether to show little shape indicators for each x/y pair additionally to the plot
   */
  public Chart(String title, String labelAxisX, String labelAxisY, XYSeries[] series, NumberFormat format, boolean shapes) {
    
    // prepare chart setup
    NumberAxis xAxis = new NumberAxis(labelAxisX);
    xAxis.setAutoRangeIncludesZero(false);
    
    NumberAxis yAxis = new NumberAxis(labelAxisY);
    yAxis.setNumberFormatOverride(format);
    
    XYItemRenderer renderer = new StandardXYItemRenderer(shapes ? StandardXYItemRenderer.SHAPES_AND_LINES : StandardXYItemRenderer.LINES);
    
    XYPlot plot = new XYPlot(XYSeries.toXYDataset(series), xAxis, yAxis, renderer);

    // init
    init(title, plot, true);
    
    // done
  }
  
  /**
   * Constructor for a pie chart with one series of data containing the
   * values for the pieces of the pie.
   * @param title the title of the pie chart
   * @param series one series defining the pie/pieces
   * @param categories a category string per index in series
   * @param legend whether to show a legend
   */
  public Chart(String title, IndexedSeries series, String[] categories, boolean legend) {
    
    PiePlot plot = new PiePlot(IndexedSeries.asPieDataset(series, categories));
    plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {1}"));
    plot.setInsets(new RectangleInsets(0, 5, 5, 10));
    
    init(title, plot, legend);
    
  }
  
  /**
   * Constructor for a chart with indexed series and categories where
   * each category groups the values of all series.
   * Example:
   * <pre>
   *   X     Y          Z
   *   |Y   X|Z   Y     |
   *   ||Z  |||  X|Z  XY|
   *   Jan  Feb  Mar  Apr
   * </pre>
   * or
   * <pre>
   *         X
   *    X    X
   *    X    Y         X
   *    X    Y    X    Y
   *    Y    Y    Y    Z
   *    Y    Z    Y    Z
   *    Z    Z    Z    Z
   *   Jan  Feb  Mar  Apr
   * </pre>
   * @param title the title of the chart
   * @param labelCatAxis a label for category-axis
   * @param series one or more indexed series to show
   * @param categories the categories to show 
   * @param format a number format to use for y-values
   * @param isStacked whether to stack series per categories instead of placing them side by side
   * @param isVertical whether to show the chart vertical instead of horizontal
   */
  public Chart(String title, String labelCatAxis, IndexedSeries[] series, String[] categories, NumberFormat format, boolean isStacked, boolean isVertical) {

    // wrap into JFreeChart
    CategoryAxis categoryAxis = new CategoryAxis(labelCatAxis);
    NumberAxis valueAxis = new NumberAxis();
    valueAxis.setNumberFormatOverride(format);

    BarRenderer renderer;
    if (isStacked) {
      renderer = new StackedBarRenderer();
    } else {
      renderer = new BarRenderer();
    }
    
    // TODO Charts - colors are hardcoded atm
    renderer.setSeriesPaint(0, Color.BLUE);
    renderer.setSeriesPaint(1, Color.RED);
    
    // prepare plot
    CategoryPlot plot = new CategoryPlot(IndexedSeries.asCategoryDataset(series, categories), categoryAxis, valueAxis, renderer);
    plot.setOrientation(!isVertical ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL);

    // init
    init(title, plot, true);
    
    // done
  }

} //Chart
