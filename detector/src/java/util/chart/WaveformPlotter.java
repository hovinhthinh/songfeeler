package util.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.SamplingXYLineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;

/**
 * Created by thinhhv on 03/08/2014.
 */

public class WaveformPlotter extends ApplicationFrame {

	/**
	 * Creates a new waveform plotter given the waveforms to plot.
	 */
	public WaveformPlotter(String title, String xName, String yName, int[] x, int[] y) {
		super(title);
		XYSeries series = new XYSeries(title);
		for (int i = 0; i < x.length; ++i) series.add(x[i], y[i]);
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(series);
		JFreeChart chart = ChartFactory.createXYLineChart(title, xName, yName, dataset, PlotOrientation.VERTICAL, true, true, false);

		chart.setBackgroundPaint(Color.white);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);

		SamplingXYLineRenderer r = new SamplingXYLineRenderer();
		plot.setRenderer(0, r);

		ChartPanel panel = new ChartPanel(chart);
		panel.setFillZoomRectangle(true);
		panel.setRangeZoomable(false);
		panel.setMouseWheelEnabled(true);

		setContentPane(panel);
	}

	/**
	 * Open the GUI to show the plot.
	 */
	public void plot() {
		this.pack();
		RefineryUtilities.centerFrameOnScreen(this);
		this.setVisible(true);
	}


}