package util.chart;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;

/**
 * Created by thinhhv on 02/08/2014.
 */

public class FastScatterPlotter extends ApplicationFrame {

	/**
	 * The data: 0: horizontal; 1: vertical
	 */
	private float[][] data;

	public FastScatterPlotter(String title, String xName, String yName, float[] x, float[] y) {
		super(title);
		data = new float[2][];
		data[0] = x;
		data[1] = y;

		final NumberAxis xAxis = new NumberAxis(xName);
		xAxis.setAutoRangeIncludesZero(true);
		final NumberAxis yAxis = new NumberAxis(yName);
		yAxis.setAutoRangeIncludesZero(true);

		final FastScatterPlot plot = new FastScatterPlot(this.data, xAxis, yAxis);
		final JFreeChart chart = new JFreeChart(title, plot);

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