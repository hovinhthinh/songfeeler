package util.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;

/**
 * Created by thinhhv on 10/10/2014.
 */

public class ScatterPlotter extends ApplicationFrame {

	/**
	 * The data: 0: horizontal; 1: vertical
	 */
	private float[][] data;

	public ScatterPlotter(String title, String xName, String yName, float[] x, float[] y) {
		super(title);
		data = new float[2][];
		data[0] = x;
		data[1] = y;

		XYSeriesCollection dataSet = new XYSeriesCollection();
		XYSeries series = new XYSeries("series_0");
		for (int i = 0; i < x.length; i++) {
			series.add(x[i], y[i]);
		}
		dataSet.addSeries(series);

		final JFreeChart chart = ChartFactory.createScatterPlot(title, xName, yName, dataSet);

		// force aliasing of the rendered content..
		chart.getRenderingHints().put
				(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		final ChartPanel panel = new ChartPanel(chart, true);

		setContentPane(panel);
	}

	public void plot() {
		this.pack();
		RefineryUtilities.centerFrameOnScreen(this);
		this.setVisible(true);
	}
}