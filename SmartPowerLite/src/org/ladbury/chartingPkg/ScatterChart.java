package org.ladbury.chartingPkg;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.JFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.ladbury.smartpowerPkg.Processing;
import org.ladbury.smartpowerPkg.SmartPower;
import org.ladbury.smartpowerPkg.Timestamped;

public class ScatterChart extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6449681550421101285L;

	/**
     * Time based Histogram Constructor
     *
     * @param title  the frame title.
     */
    public ScatterChart(final String frameTitle) {
        super(frameTitle);
        String chartTitle;    
        Timestamp t1 = new Timestamp(0);
   	
        // get data
    	final XYDataset data1 = Processing.getDeviceActivityScatterData();
		DateFormat df = new SimpleDateFormat(Timestamped.DATE_AND_DAYFORMAT);
		df.setTimeZone(java.util.TimeZone.getTimeZone("GMT")); // makes sure we don't get an extra hour!
		if( SmartPower.getMain().getData().getActivity().size()>0){
			t1 = SmartPower.getMain().getData().getActivity().get(0).start(); 
	        chartTitle = "Device Activity "+ df.format(t1);			
		} else  chartTitle = "No Device Activity";

        
        // set up chart
		final XYItemRenderer renderer= new XYLineAndShapeRenderer(false,true);
        final DateAxis domainAxis = new DateAxis("Duration");
        domainAxis.setTimeZone(java.util.TimeZone.getTimeZone("GMT")); // makes sure we don't get an extra hour!
        final ValueAxis rangeAxis = new NumberAxis("Consumption(W)");     
        final XYPlot plot = new XYPlot(data1, domainAxis, rangeAxis, renderer);       
        plot.setDomainPannable(true);
        final JFreeChart chart = new JFreeChart(chartTitle, plot);

        //Display chart in Frame
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);
    }
}