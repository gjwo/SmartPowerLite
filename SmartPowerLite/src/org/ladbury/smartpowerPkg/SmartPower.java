package org.ladbury.smartpowerPkg;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;

import javax.swing.UIManager;

import org.ladbury.dataServicePkg.DataService;
import org.ladbury.meterPkg.Meter;
import org.ladbury.meterPkg.Meter.MeterType;
import org.ladbury.meterPkg.Metric;
import org.ladbury.meterPkg.Metric.MetricType;
import org.ladbury.persistentData.PersistentData;
import org.ladbury.userInterfacePkg.UiFrame;

import static org.ladbury.dataServicePkg.DataService.DEFAULT_API_URL;

/**
 * SmartPower.java:	Applet
 * 
 * This Applet processes readings from a domestic energy monitor in order to better
 * understand domestic power consumption by turning raw readings into a more understandable form.
 * Ultimately the readings are associated with devices defined by the user so that the behaviour
 * that causes power consumption in the home can be understood and modified if desired.
 * 
 * The intention is to recognise devices and their usage patterns to give a comprehensive understanding
 * with minimal manual intervention. Given the lack of uniqueness of device power signatures, some
 * intervention will almost always be required to work out what is happening
 * 
 * @author GJWood
 * @version 1.1 2012/11/29 Incorporating handling of Owl meter
 * @version 1.2 2013/11/19 Incorporating handling of Onzo meter
 */
public class SmartPower extends Applet implements Runnable {

	private static final long serialVersionUID = 1L;
	public enum RunState {
		IDLE, LOAD_DATA, OPEN_FILE, PROCESS_FILE, PROCESS_READINGS, SAVE_FILE, PROCESS_EDGES, PROCESS_EVENTS, STOP
	}

    private static final String PARAM_MEASUREMENT_FILE = "measurement file";

    private boolean 	fStandAlone = true;    //	fStandAlone will be set to true if applet is run stand alone
    private	String 		displayString = null;
	private Thread 		threadSmartPower = null; //Thread object for the applet
	private RunState	state = RunState.IDLE;
    
    // Application Specific data (not persistent)
    private static	SmartPower	spMain = null; //This is the root access point for all data in the package, the only static.
    private	UiFrame 			frame = null;
    private	FileAccess 			file = null;
    private DataService         dataService = null;
    private MetricType			currentMetricType = MetricType.UNDEFINED;
//    private Meter				meter = null;
    
    //Application Specific data (persistent)
	private final PersistentData data ;

    // STANDALONE APPLICATION SUPPORT
    // The GetParameter() method is a replacement for the getParameter() method
    // defined by Applet. This method returns the value of the specified parameter;
    // unlike the original getParameter() method, this method works when the applet
    // is run as a stand alone application, as well as when run within an HTML page.
    // This method is called by GetParameters().
    //---------------------------------------------------------------------------
    private String GetParameter(String strName, String args[]) {
        if (args == null) {
            // Running within an HTML page, so call original getParameter().
            //-------------------------------------------------------------------
            return getParameter(strName);
        }

        // Running as stand alone application, so parameter values are obtained from
        // the command line. The user specifies them as follows:
        //
        //	JView SmartPower param1=<val> param2=<"val with spaces"> ...
        //-----------------------------------------------------------------------
        int i;
        String strArg = strName + "=";
        String strValue = null;
        int nLength = strArg.length();

        try {
            for (i = 0; i < args.length; i++) {
                String strParam = args[i].substring(0, nLength);

                if (strArg.equalsIgnoreCase(strParam)) {
                    // Found matching parameter on command line, so extract its value.
                    // If in double quotes, remove the quotes.
                    //---------------------------------------------------------------
                    strValue = args[i].substring(nLength);
                    if (strValue.startsWith("\"")) {
                        strValue = strValue.substring(1);
                        if (strValue.endsWith("\"")) {
                            strValue = strValue.substring(0,
                                strValue.length() - 1);
                        }
                    }
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return strValue;
    }

    // STANDALONE APPLICATION SUPPORT
    // 	The GetParameters() method retrieves the values of each of the applet's
    // parameters and stores them in variables. This method works both when the
    // applet is run as a standalone application and when it's run within an HTML
    // page.  When the applet is run as a standalone application, this method is
    // called by the main() method, which passes it the command-line arguments.
    // When the applet is run within an HTML page, this method is called by the
    // init() method with args == null.
    //---------------------------------------------------------------------------
    private void GetParameters(String args[]) {
        // Query values of all Parameters
        //--------------------------------------------------------------
        String param;

        // measurement file : Parameter description
        //--------------------------------------------------------------
        param = GetParameter(PARAM_MEASUREMENT_FILE, args);
         if (param != null) {
             String readingsFile = param;
             readingsFile = readingsFile +""; //Suppress warning
           
        }
    }

    // STANDALONE APPLICATION SUPPORT
    // 	The main() method acts as the applet's entry point when it is run
    // as a standalone application. It is ignored if the applet is run from
    // within an HTML page.
    //----------------------------------------------------------------------
    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        SmartPower applet_SmartPower = new SmartPower();
        SmartPower.spMain = applet_SmartPower;
        //frame.add("Center", applet_SmartPower);
        applet_SmartPower.fStandAlone = true;
        applet_SmartPower.GetParameters(args);
        applet_SmartPower.init();
        applet_SmartPower.start();
    }

    // SmartPower Class Constructor
    //----------------------------------------------------------------------
	public SmartPower() {

        frame = new UiFrame("Graham's power analysis program");
        dataService = new DataService(DEFAULT_API_URL);
        // create persistent objects, data loaded in init()
        data = new PersistentData(); // set up entity manager etc

        //Pack frames that have useful preferred size info, e.g. from their layout
        //Validate frames that have preset sizes
        boolean packFrame = false;
        // Centre the frame
        if (packFrame) frame.pack();
        else frame.validate();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;
        frame.setLocation( (screenSize.width - frameSize.width) / 2,
                          (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);

        displayString = "no line";
        file = new FileAccess();

    }

    // APPLET INFO SUPPORT:
    //		The getAppletInfo() method returns a string describing the applet's
    // author, copyright date, or miscellaneous information.
    //--------------------------------------------------------------------------
    public String getAppletInfo() {
        return "Name: SmartPower\r\n" +
            "Author: G.J.Wood Copyright 2012,2013\r\n" +
            "Created with Eclipse Indigo & Juno";
    }

    // PARAMETER SUPPORT
    //		The getParameterInfo() method returns an array of strings describing
    // the parameters understood by this applet.
    //
    // SmartPower Parameter Information:
    //  { "Name", "Type", "Description" },
    //--------------------------------------------------------------------------
    public String[][] getParameterInfo() {
        String paramReadingsFile = "readingsfile";
        return new String[][]{
            {
                    paramReadingsFile, "String",
            "The name of the input file of meter readings"}
            ,
        };
    }

    // The init() method is called by the AWT when an applet is first loaded or
    // reloaded.  Override this method to perform whatever initialisation your
    // applet needs, such as initialising data structures, loading images or
    // fonts, creating frame windows, setting the layout manager, or adding UI
    // components.
    //--------------------------------------------------------------------------
    public void init() {
        if (!fStandAlone) {
            GetParameters(null);
        }
        data.loadPersistentData(); // load the data using entity manager
        currentMetricType = MetricType.UNDEFINED;
        setupDefaultOnzoMeter();
        setupDefaultPMon10Meter();
    }

    void setupDefaultOnzoMeter()
    {
        Meter mtr;
        Metric mtc;
        if(data.getMeters()==null ||data.getMeters().size()<=0){//add initial entry if none exist
            mtr = new Meter(MeterType.ONZO);
            data.getMeters().softAdd(mtr);
        }
        if (data.getMetrics()==null ||data.getMetrics().size()<=0){//add initial entries if none exist
            //Onzo metrics ENERGY_LOW_RES, ENERGY_HIGH_RES, POWER_REAL_STANDARD, POWER_REAL_FINE, POWER_REACTIVE_STANDARD
            mtr = data.getMeters().get(0);

            mtc = new Metric(mtr,MetricType.ENERGY_LOW_RES);
            data.getMetrics().softAdd(mtc);
            mtr.setMetric(MetricType.ENERGY_LOW_RES, mtc);

            mtc = new Metric(mtr,MetricType.ENERGY_HIGH_RES);
            data.getMetrics().softAdd(mtc);
            mtr.setMetric(MetricType.ENERGY_HIGH_RES, mtc);

            mtc = new Metric(mtr,MetricType.POWER_REAL_STANDARD);
            data.getMetrics().softAdd(mtc);
            mtr.setMetric(MetricType.POWER_REAL_STANDARD, mtc);

            mtc = new Metric(mtr,MetricType.POWER_REAL_FINE);
            data.getMetrics().softAdd(mtc);
            mtr.setMetric(MetricType.POWER_REAL_FINE, mtc);

            mtc = new Metric(mtr,MetricType.POWER_REACTIVE_STANDARD);
            data.getMetrics().softAdd(mtc);
            mtr.setMetric(MetricType.POWER_REACTIVE_STANDARD, mtc);
        }
    }

    void setupDefaultPMon10Meter()
    {
        Meter mtr;
        Metric mtc;
        if(data.getMeters()==null ||data.getMeters().size()<=0){//add initial entry if none exist
            mtr = new Meter(MeterType.PMON10);
            data.getMeters().softAdd(mtr);
        }
        if (data.getMetrics()==null ||data.getMetrics().size()<=0){//add initial entries if none exist
            //Onzo metrics ENERGY_LOW_RES, ENERGY_HIGH_RES, POWER_REAL_STANDARD, POWER_REAL_FINE, POWER_REACTIVE_STANDARD

            mtr = data.getMeters().get(0);

            mtc = new Metric(mtr,MetricType.POWER_REAL);
            data.getMetrics().softAdd(mtc);
            mtr.setMetric(MetricType.POWER_REAL, mtc);

            mtc = new Metric(mtr,MetricType.POWER_APPARENT);
            data.getMetrics().softAdd(mtc);
            mtr.setMetric(MetricType.POWER_APPARENT, mtc);

            mtc = new Metric(mtr,MetricType.POWER_REACTIVE);
            data.getMetrics().softAdd(mtc);
            mtr.setMetric(MetricType.POWER_REACTIVE, mtc);

            mtc = new Metric(mtr,MetricType.VOLTAGE_RMS);
            data.getMetrics().softAdd(mtc);
            mtr.setMetric(MetricType.VOLTAGE_RMS, mtc);

            mtc = new Metric(mtr,MetricType.CURRENT);
            data.getMetrics().softAdd(mtc);
            mtr.setMetric(MetricType.CURRENT, mtc);
        }
    }

    // Place additional applet clean up code here.  destroy() is called when
    // when you applet is terminating and being unloaded.
    //-------------------------------------------------------------------------
    public void destroy() {
        // Place applet cleanup code here
    }

    // SmartPower Paint Handler
    //--------------------------------------------------------------------------
    public void paint(Graphics g) {
        // Place applet paint code here
        g.drawString("Running: " + Math.random(), 10, 20);
        g.drawString("Filename: " + this.file.inputFilename(), 10, 40);
        g.drawString("Directory name: " +this.file.inputPathname(), 10, 60);
        g.drawString("File State: " + this.file.state(), 10, 80);
        g.drawString(displayString, 10, 90);
    }

    //		The start() method is called when the page containing the applet
    // first appears on the screen. The AppletWizard's initial implementation
    // of this method starts execution of the applet's thread.
    //--------------------------------------------------------------------------
    public void start() {
        if (this.threadSmartPower == null) {
            this.threadSmartPower = new Thread(this);
            this.threadSmartPower.start();
        }
        //Place additional applet start code here
    }

    //		The stop() method is called when the page containing the applet is
    // no longer on the screen. The AppletWizard's initial implementation of
    // this method stops execution of the applet's thread.
    //--------------------------------------------------------------------------
    public void stop() {
        if (this.threadSmartPower != null) {
            this.change_state(RunState.STOP);
            //this.threadSmartPower = null;
        }
    }

    // THREAD SUPPORT
    //		The run() method is called when the applet's thread is started. If
    // your applet performs any ongoing activities without waiting for user
    // input, the code for implementing that behaviour typically goes here. For
    // example, for an applet that performs animation, the run() method controls
    // the display of images.
    //--------------------------------------------------------------------------
    public void run() {
    	int j = 0;
     	Meter m;
     	Metric tempMtc;
     	m = this.data.getMeters().get(0);
        while (this.get_state() != RunState.STOP) {
            try {
                switch (this.get_state()) {
                    case IDLE:
                        this.frame.displayLog(".");
                        j++;
                        if (j == 79){ this.frame.displayLog("\n\r"); j=0;}
                        repaint();
                        Thread.sleep(5000);
                        break;
                    case OPEN_FILE: //this state triggered by the user opening a file
                        this.frame.displayLog("\n\rRun: Opening file\n\r");
                        repaint();
                        this.file.setInputFilename(this.frame.getFileDialog().getFile());
                        this.file.setInputPathname(this.frame.getFileDialog().getDirectory());
                        if ( !(this.file.inputFilename() == null | this.file.inputPathname() == null)) {
                            this.file.openInput();
                            //frame.displayLog("Run: back from open\n");
                            switch(m.getType()){
                            case OWLCM160:
                            	break;
                            case ONZO:
                            	break;
                            default:	
                            }	
                            this.change_state(RunState.PROCESS_FILE);
                        } else {
                        	this.change_state(RunState.STOP);
                        }
                        break;
                    case PROCESS_FILE: 
                        this.frame.displayLog("Run: Processing file\n\r");
                        repaint();
                        this.currentMetricType = this.file.identifyTypeFromFilename( this.file.inputFilename());
                        if (this.currentMetricType != MetricType.UNDEFINED){
                        	// Reinitialise this type of metric in the meter
                        	tempMtc = m.getMetric(currentMetricType);
                        	m.removeMetric(currentMetricType);//clear out any old data
                        	data.getMetrics().remove(tempMtc);
                        	tempMtc = new Metric(m,currentMetricType); //create and save new data
                        	data.getMetrics().softAdd(tempMtc);
                        	m.addMetric(currentMetricType);
                        	m.setMetric(currentMetricType, tempMtc);
                        	// read the new file into the newly initialised metric
                        	this.file.processFile(currentMetricType);
                        	this.file.closeInput();
                        	this.change_state(RunState.PROCESS_READINGS);
                        } else{ 
                            this.frame.displayLog("Run: ERROR Metric type not identified (ignoring file)\n\r");                	
                            this.change_state(RunState.IDLE);
                        }
                        break;
                    case PROCESS_READINGS:
                        this.frame.displayLog("Run: Processing readings\n\r");                	
                        repaint();
                        switch(m.getType()){
                        case OWLCM160:
                        	break;
                        case ONZO:
                            if (this.currentMetricType != MetricType.UNDEFINED){
                            	m.getMetric(currentMetricType).removeRedundantData();
                            } else this.frame.displayLog("Run: ERROR Metric type not identified (Cannot Process)\n\r");                	
                        	break;
                        default:
                        	break;
                        }
                        this.frame.displayLog("Run: Completed processing readings\n\r");                	
                        repaint();
                        this.change_state(RunState.IDLE);
                        //System.gc(); // kick off the garbage collector
                        break;
                    case PROCESS_EDGES:
                        this.frame.displayLog("Run: Processing edges\n\r");                	
                        switch(m.getType()){
                        case OWLCM160:
                        	break;
                        case ONZO:
                            if (this.currentMetricType != MetricType.UNDEFINED){
                            	int lastReadingsCount = m.getMetric(currentMetricType).size();
                            	int readingsCount = lastReadingsCount-1; //force first run
                            	while (lastReadingsCount > readingsCount){
                            		lastReadingsCount = readingsCount;
                            		this.frame.displayLog("Run: Squelching " + lastReadingsCount + " readings \n\r");                	
                            		m.getMetric(currentMetricType).squelchTransitions();
                            		readingsCount = m.getMetric(currentMetricType).size();
                                }
                            }else this.frame.displayLog("Run: ERROR Metric type not identified (Cannot Squelch)\n\r");
                        	break;
                        default:
                        	break;
                        }
                        this.frame.displayLog("Run: Completed processing edges\n\r");                	
                    	this.change_state(RunState.IDLE);
                    	//System.gc(); // kick off the garbage collector
                    	break;
                    case PROCESS_EVENTS:
                        this.frame.displayLog("Run: Processing Events\n\r");
                        repaint();
                        switch(m.getType()){
                        case OWLCM160:
                        	break;
                        case ONZO:
                            if (this.currentMetricType != MetricType.UNDEFINED){
                            	Processing.matchAndSaveActivity(getCurrentMeter().getMetric(MetricType.POWER_REAL_FINE));
                            } else this.frame.displayLog("Run: ERROR Metric type not identified (Cannot process device activity)\n\r");
                        	break;
                        default:
                        	break;
                        }
                     	this.change_state(RunState.IDLE);
                    	//System.gc(); // kick off the garbage collector
                    	break;
                    case SAVE_FILE: //this state triggered by user selecting save file
                        this.frame.displayLog("\n\rRun: Saving files\n\r");
                        repaint();
                        switch(m.getType()){
                        case OWLCM160:
                            //this.file.OutputCSVFiles();
                          	break;
                        case ONZO:
                        	if (this.currentMetricType != MetricType.UNDEFINED){                  	
                        		this.file.setOutputFilename(this.frame.getFileDialog().getFile());
                        		this.file.setOutputPathname(this.frame.getFileDialog().getDirectory());
                        		if ( !(this.file.outputFilename() == null | this.file.outputPathname() == null)) {
                        			this.file.OutputMetricAsCSVFile(MeterType.ONZO, 
                        					getCurrentMeter().getMetric(currentMetricType));
                        			this.file.OutputActivityAsCSVFile(	getCurrentMeter().getMetric(currentMetricType).getName(),
                                										getData().getActivity());
                        		}
                            } else {this.frame.displayLog("Run: ERROR Metric type not identified (Cannot output files)\n\r");}
                            break;
                        default:	
                        }	
                        //frame.displayLog("Run: back from open\n");
                        this.change_state(RunState.IDLE);
                        //System.gc(); // kick off the garbage collector
                        break;
                    default:
                        repaint();
                        Thread.sleep(1000);
                }

            }
            catch (InterruptedException e) {
                // Place exception-handling code here in case an
                //       InterruptedException is thrown by Thread.sleep(),
                //		 meaning that another thread has interrupted this one
                this.frame.displayLog("!");
                e.printStackTrace();
            	//System.out.println(e.toString());
            }
        }
        System.exit(0);
    }
    
    //
    // Access Methods
    //
	public static SmartPower getMain() {
		return SmartPower.spMain; //needed to access all other dynamic data without specific access methods
	}
    public synchronized void change_state(RunState new_state) {
    	this.state = new_state;
    	//this.threadSmartPower.interrupt(); // this caused persistence to fail
    }
    private synchronized RunState get_state() {
        return (this.state);
    }
    public void display(String s) {
        this.displayString = s;
        System.out.println(this.displayString);
        repaint();
    }
	public  UiFrame getFrame() {
		return this.frame;
	}
	protected  FileAccess getFile() {
		return this.file;
	}
	
	public MetricType getMetricType() {
		return currentMetricType;
	}

	public void setMetricType(MetricType mtcT) {
		this.currentMetricType = mtcT;
	}

	//
	// Access method for persistent data repository
	//
	public PersistentData getData(){
		return this.data;
	}
	private Meter getCurrentMeter(){
		return getData().getMeters().get(0);
	}
}
