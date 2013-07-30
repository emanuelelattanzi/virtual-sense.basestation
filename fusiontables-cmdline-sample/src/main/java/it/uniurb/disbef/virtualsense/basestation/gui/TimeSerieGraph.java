package it.uniurb.disbef.virtualsense.basestation.gui;


import it.uniurb.disbef.virtualsense.basestation.Node;
import it.uniurb.disbef.virtualsense.basestation.Packet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ShapeUtilities;
 
public class TimeSerieGraph {
 
	private double avg;
	private int counter;
    
	public BufferedImage createTimeSeriesXYChart(Node nn, String value)
    { 
	
		TimeSeries series = null;
		String serieLabel = setSerieLabel(value);
		
		if(value.equals("People"))
         series = new TimeSeries( value+" in", Second.class );
		else 
			series = new TimeSeries( value, Second.class );
        TimeSeries series2 = new TimeSeries( "People out", Second.class );
        TimeSeries series3 = new TimeSeries( "People total", Second.class );
        
        addDataToSeries(series, series2, series3, nn, value);   
        avg = avg/counter;
        
        TimeSeriesCollection dataset=new TimeSeriesCollection();
        dataset.addSeries(series);
        if(value.equals("People")){
        	dataset.addSeries(series2);
        	dataset.addSeries(series3);
        }
 
        JFreeChart chart = ChartFactory.createTimeSeriesChart
        (value+": node "+nn.ID,    // Title
         "Time",                     // X-Axis label
         serieLabel,             // Y-Axis label
         dataset,               // Dataset
         true,                      // Show legend
         true,              //tooltips
         true              //url
        );
        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);
            renderer.setBaseShapesFilled(true);
        }
        setGraphMargin(value, plot, series, series2, series3);
        BufferedImage bImg = chart.createBufferedImage(550, 700);
        return bImg;
        
     
 
    }
 
    public void saveChart(JFreeChart chart)
    {
        String fileName="myTimeSeriesChart.jpg";
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
    
    public double standardDeviation(double mean, TimeSeries serie){
    	double result = 0;
    	
    	  double[] deviations = new double[serie.getItemCount()];
    	  
    	  // Taking the deviation of mean from each numbers
    	  for(int i = 0; i < deviations.length; i++) {
    	   deviations[i] = serie.getValue(i).doubleValue() - mean ;    	    
    	  }
    	 
    	  double[] squares = new double[serie.getItemCount()];
    	 
    	  // getting the squares of deviations
    	  for(int i =0; i< squares.length; i++) {
    	   squares[i] = deviations[i] * deviations[i];
    	  }
    	
    	  double sum = 0;
    	 
    	  // adding all the squares
    	  for(int i =0; i< squares.length; i++) {
    	   sum = sum + squares[i];
    	  }
    	 
    	 
    	  // dividing the numbers by one less than total numbers
    	  result = sum / (serie.getItemCount() - 1); 
    	  return Math.sqrt(result);
    }
    
    private String setSerieLabel(String value){
    	String serieLabel = value;
    	if(value.equals("CO2")) {
			serieLabel+=" [ppm]";
		}else if (value.equals("Counter")){
			serieLabel+=" [#]";
		}else if (value.equals("Noise")){
			serieLabel+=" [dB]";
		}else if (value.equals("People")){
			serieLabel+=" [#]";
		}else if (value.equals("Temp")){
			serieLabel+=" [C]";
		}else if (value.equals("Pressure")){
			serieLabel+=" [hPa]";
		}else if (value.equals("Light")){
			serieLabel+=" [Lx]";
		}
    	return serieLabel;
    }
    
    private void addDataToSeries(TimeSeries s1, TimeSeries s2, TimeSeries s3, Node nn, String value){
    	Iterator<Packet> it = nn.myPackets.iterator();
        
        while(it.hasNext()){
        	Packet p  = it.next();
        	if(value.equals("Counter")){
        		avg+=p.counter;
    			counter++;
        		s1.addOrUpdate(new Second(new Date(p.time)), p.counter);
        	}
        	if(value.equals("CO2") && p.co2 > 100) {
        		s1.addOrUpdate(new Second(new Date(p.time)), p.co2);
        		avg+=p.co2;
        		counter++;        		
        	}
        	if(value.equals("Noise") && p.noise != 0){
        		avg+=p.noise;
        		counter++;
        		s1.addOrUpdate(new Second(new Date(p.time)), p.noise);
        	}
        	if(value.equals("People")){
        		avg+=p.in;
    			counter++;
        		s1.addOrUpdate(new Second(new Date(p.time)), p.in);
        		s2.addOrUpdate(new Second(new Date(p.time)), p.out);
        		s3.addOrUpdate(new Second(new Date(p.time)), p.in - p.out);
        	}
        	if(value.equals("Temp") &&(p.temperature < 10000)  &&(p.temperature > 0)){
        		avg+=(((double)p.temperature)/100);
    			counter++;
        		s1.addOrUpdate(new Second(new Date(p.time)), (((double)p.temperature)/100));
        	}
        	if(value.equals("Pressure") && (p.pressure < 1400) && (p.pressure > 700)){
        		avg+=p.pressure;
    			counter++;
        		s1.addOrUpdate(new Second(new Date(p.time)), p.pressure);
        	}
        	if(value.equals("Light") && (p.luminosity > 15)){
        		avg+=p.luminosity;
    			counter++;
        		s1.addOrUpdate(new Second(new Date(p.time)), p.luminosity);
        	}
        	//System.out.println(" time: "+p.time+" counter: "+p.counter);
        }      
    
    }
    
    private void setGraphMargin(String value, XYPlot plot, TimeSeries s1, TimeSeries s2, TimeSeries s3){
        
    	 double deviation = 2*standardDeviation(avg, s1);
         if(!value.equals("People")) 
         	plot.getRangeAxis(0).setLowerBound(0);
         else {
         	double s1m = s1.getMaxY();
         	double s2m = s2.getMaxY();
         	double s3m = s3.getMaxY();
         	double max = Math.max(Math.max(s1m,s2m),s3m);
         	plot.getRangeAxis(0).setUpperBound(max);
         }
         if(value.equals("CO2")) {
         	double upperMargin = plot.getRangeAxis(0).getUpperBound();
         	plot.getRangeAxis(0).setUpperBound(upperMargin+100);
         }else if(value.equals("Pressure")){         	
         	plot.getRangeAxis(0).setUpperBound(1400);
         }else if(value.equals("Temp")){
         	double upperMargin = plot.getRangeAxis(0).getUpperBound();
         	plot.getRangeAxis(0).setUpperBound(upperMargin+deviation+10);
         }else {
         	double upperMargin = plot.getRangeAxis(0).getUpperBound();
         	plot.getRangeAxis(0).setUpperBound(upperMargin+deviation);
         }
         
         
         final IntervalMarker target = new IntervalMarker(avg-deviation, avg+deviation);
         DecimalFormat df2 = new DecimalFormat( "#,###,###,##0.00" );

         if(!value.equals("People")) {
         	target.setLabel("Mean: "+df2.format(avg));
         	target.setLabelFont(new Font("SansSerif", Font.ITALIC, 14));
         	target.setLabelAnchor(RectangleAnchor.LEFT);
         	target.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
         	target.setPaint(new Color(222, 222, 255, 128));
         	plot.addRangeMarker(target, Layer.BACKGROUND);
         }
    }
}