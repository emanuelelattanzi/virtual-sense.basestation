package it.uniurb.disbef.virtualsense.basestation.gui;


import it.uniurb.disbef.virtualsense.basestation.Node;
import it.uniurb.disbef.virtualsense.basestation.Packet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.ShapeUtilities;
 
public class TimeSerieGraph {
 
    
	public BufferedImage createTimeSeriesXYChart(Node nn, String value)
    {
 
        TimeSeries series = new TimeSeries( "", Second.class );
        System.out.println("Creating graph for "+nn.myPackets.size()+" packets ");
 
       
        Iterator<Packet> it = nn.myPackets.iterator();
        
        while(it.hasNext()){
        	Packet p  = it.next();
        	if(value.equals("Counter"))
        		series.addOrUpdate(new Second(new Date(p.time)), p.counter);
        	if(value.equals("CO2"))
        		series.addOrUpdate(new Second(new Date(p.time)), p.co2);
        	if(value.equals("Noise"))
        		series.addOrUpdate(new Second(new Date(p.time)), p.noise);
        	//System.out.println(" time: "+p.time+" counter: "+p.counter);
        }
        
        TimeSeriesCollection dataset=new TimeSeriesCollection();
        dataset.addSeries(series);
 
        JFreeChart chart = ChartFactory.createTimeSeriesChart
        (value+": of the node "+nn.ID,    // Title
         "Time",                     // X-Axis label
         value+" value",             // Y-Axis label
         dataset,               // Dataset
         true,                      // Show legend
         true,              //tooltips
         false              //url
        );
 
        
          Shape shape  = new Ellipse2D.Double(0,0,5,5);
        
        
       // saveChart(chart);
        final XYPlot plot = chart.getXYPlot();
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setBaseShape(shape);
        renderer.setBasePaint(Color.red);
        
        
       
        
        renderer.setSeriesShape(0, shape);
        BufferedImage bImg = chart.createBufferedImage(550, 700);
        return bImg;
        
     
 
    }
 
    public void saveChart(JFreeChart chart)
    {
        String fileName="C:/Users/kushal/Desktop/myTimeSeriesChart.jpg";
        try {
            /**
             * This utility saves the JFreeChart as a JPEG
             * First Parameter: FileName
             * Second Parameter: Chart To Save
             * Third Parameter: Height Of Picture
             * Fourth Parameter: Width Of Picture
             */
            ChartUtilities.saveChartAsJPEG(new File(fileName), chart, 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Problem occurred creating chart.");
        }
    }    
}