package org.ladbury.smartpowerPkg;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.UIManager;

//import org.ladbury.dataServicePkg.DataService;
import org.ladbury.meterPkg.Meter;
import org.ladbury.meterPkg.Meter.MeterType;
import org.ladbury.meterPkg.Metric;
import org.ladbury.meterPkg.MetricType;
import org.ladbury.meterPkg.TimedRecord;
import org.ladbury.persistentData.PersistentData;
import org.ladbury.userInterfacePkg.UiFrame;


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
 * @version 1.3 2017/09/10 Incorporating handling of PMon10 meter
 */
public class SmartPower extends Applet implements Runnable {

    private static final long serialVersionUID = 1L;
    public enum RunState {
        IDLE, OPEN_FILE, PROCESS_FILE, PROCESS_READINGS, SAVE_FILE, PROCESS_EDGES, PROCESS_EVENTS, STOP
    }

    private static final String PARAM_MEASUREMENT_FILE = "measurement file";

    private boolean 	fStandAlone = true;    //	fStandAlone will be set to true if applet is run stand alone
    private	String 		displayString = null;
    private Thread 		threadSmartPower = null; //Thread object for the applet
    private RunState	state = RunState.IDLE;

    // Application Specific data (not persistent)
    private static	SmartPower	spMain = null; //This is the root access point for all data in the package, the only static.
    private final   UiFrame 			frame;
    private final   FileAccess 			file;
//    private         DataService         dataService;
    private volatile MetricType	currentMetricType;
    private volatile Meter      currentMeter;

    //Application Specific data (persistent)
    private final PersistentData data ;

    // STANDALONE APPLICATION SUPPORT
    // The GetParameter() method is a replacement for the getParameter() method
    // defined by Applet. This method returns the value of the specified parameter;
    // unlike the original getParameter() method, this method works when the applet
    // is run as a stand alone application, as well as when run within an HTML page.
    // This method is called by GetParameters().
    //---------------------------------------------------------------------------
    @SuppressWarnings("SameParameterValue")
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
    @SuppressWarnings("UnusedAssignment")
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
        SmartPower.spMain = new SmartPower();
        SmartPower.spMain.fStandAlone = true;
        SmartPower.spMain.GetParameters(args);
        SmartPower.spMain.init();
        SmartPower.spMain.start();
    }

    // SmartPower Class Constructor
    //----------------------------------------------------------------------
    private SmartPower() {
        currentMetricType = MetricType.UNDEFINED;
        currentMeter = null;
        frame = new UiFrame("Graham's power analysis program");
//        dataService = new DataService();
        // create persistent objects, data loaded in init()
        data = new PersistentData(); // set up entity manager etc
        frame.validate();
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
                "Created with Eclipse Indigo & Juno, & IntelliJ Idea";
    }

    // PARAMETER SUPPORT
    //		The getParameterInfo() method returns an array of strings describing
    // the parameters understood by this applet.
    //
    // SmartPower Parameter Information:
    //  { "Name", "Type", "Description" },
    //--------------------------------------------------------------------------
    @SuppressWarnings("SpellCheckingInspection")
    public String[][] getParameterInfo() {
        String paramReadingsFile = "readingsfile";
        return new String[][]{
                {
                        paramReadingsFile, "String",
                        "The name of the input file of meter readings"}
                ,
        };
    }

    /**
     * The init() method is called by the AWT when an applet is first loaded or
     * reloaded.  Override this method to perform whatever initialisation your
     * applet needs, such as initialising data structures, loading images or
     * fonts, creating frame windows, setting the layout manager, or adding UI
     * components.
     */
    public void init() {
        if (!fStandAlone) {
            GetParameters(null);
        }
        data.loadPersistentData(); // load the data using entity manager
        currentMetricType = MetricType.UNDEFINED;
        currentMeter = null;
//        dataService = new DataService();
    }

    /*
     * Place additional applet clean up code here.  destroy() is called when
     * when you applet is terminating and being unloaded.
     */
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

    /**
     * The start() method is called when the page containing the applet
     * first appears on the screen. The AppletWizard's initial implementation
     * of this method starts execution of the applet's thread.
     */
    public void start() {
        if (this.threadSmartPower == null) {
            this.threadSmartPower = new Thread(this);
            this.threadSmartPower.start();
        }
        //Place additional applet start code here
    }

    /**
     * The stop() method is called when the page containing the applet is
     * no longer on the screen. The AppletWizard's initial implementation of
     * this method stops execution of the applet's thread.
     */
    public void stop() {
        if (this.threadSmartPower != null) {
            this.change_state(RunState.STOP);
            //this.threadSmartPower = null;
        }
    }

    /**
     * THREAD SUPPORT
     * The run() method is called when the applet's thread is started. If
     * your applet performs any ongoing activities without waiting for user
     * input, the code for implementing that behaviour typically goes here.
     */
    public void run() {
        int j = 0;
        Metric tempMtc;
        try {
            while (this.get_state() != RunState.STOP) {
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
                        if ( !((this.file.inputFilename() == null) | (this.file.inputPathname() == null)))
                        {
                            this.file.openInput();
                            this.change_state(RunState.PROCESS_FILE);
                        } else
                        {
                            this.frame.displayLog("Run: Couldn't open file\n\r");
                            this.change_state(RunState.IDLE);
                        }
                        break;
                    case PROCESS_FILE:
                        this.frame.displayLog("Run: Processing file\n\r");
                        repaint();
                        //The file name defines the type of metric to be processed
                        this.currentMetricType = this.file.identifyTypeFromFilename( this.file.inputFilename());
                        //Metric types are unique to a meter type, therefor identify the meter and its name(cludge)
                        String meterName;
                        MeterType meterType = Meter.getMeterTypeFromMetricType(currentMetricType);
                        switch (meterType)
                        {
                            case OWLCM160: meterName = "Owl whole house"; break;
                            case ONZO: meterName = "Onzo whole house"; break;
                            default: meterName = ""; // shouldn't get PMon10 here, as input comes from DataService
                        }
                        currentMeter = getOrCreateMeter(meterType,meterName);

                        if (this.currentMetricType != MetricType.UNDEFINED){ //we have a valid input file name
                            // Reinitialise this type of metric in the meter
                            tempMtc = currentMeter.getMetric(currentMetricType);
                            currentMeter.removeMetric(currentMetricType);//clear out any old data
                            data.getMetrics().remove(tempMtc);
                            tempMtc = new Metric(currentMeter,currentMetricType); //create and save new data
                            data.getMetrics().softAdd(tempMtc);
                            currentMeter.addMetric(currentMetricType);
                            currentMeter.setMetric(currentMetricType, tempMtc);
                            // read the new file into the newly initialised metric

                            this.file.processFile(currentMeter, currentMeter.getMetric(currentMetricType));
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
                        switch(currentMeter.getType()){
                            case OWLCM160:
                                break;
                            case PMON10:
                            case ONZO:
                                if (this.currentMetricType != MetricType.UNDEFINED){
                                    currentMeter.getMetric(currentMetricType).removeRedundantData();
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
                        switch(currentMeter.getType()){
                            case OWLCM160:
                                break;
                            case PMON10:
                            case ONZO:
                                if (this.currentMetricType != MetricType.UNDEFINED){
                                    int lastReadingsCount = currentMeter.getMetric(currentMetricType).size();
                                    int readingsCount = lastReadingsCount-1; //force first run
                                    while (lastReadingsCount > readingsCount){
                                        lastReadingsCount = readingsCount;
                                        this.frame.displayLog("Run: Squelching " + lastReadingsCount + " readings \n\r");
                                        currentMeter.getMetric(currentMetricType).squelchTransitions();
                                        readingsCount = currentMeter.getMetric(currentMetricType).size();
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
                        switch(currentMeter.getType()){
                            case OWLCM160:
                                break;
                            case PMON10:
                            case ONZO:
                                if (this.currentMetricType != MetricType.UNDEFINED){
                                    Processing.matchAndSaveActivity(currentMeter.getMetric(MetricType.POWER_REAL_FINE));
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
                        switch(currentMeter.getType()){
                            case OWLCM160:
                                //this.file.OutputCSVFiles();
                                break;
                            case PMON10:
                            case ONZO:
                                if (this.currentMetricType != MetricType.UNDEFINED){
                                    this.file.setOutputFilename(this.frame.getFileDialog().getFile());
                                    this.file.setOutputPathname(this.frame.getFileDialog().getDirectory());
                                    if ( !(this.file.outputFilename() == null | this.file.outputPathname() == null)) {
                                        this.file.OutputMetricAsCSVFile(currentMeter.getMetric(currentMetricType));
                                        this.file.OutputActivityAsCSVFile(  currentMeter.getMetric(currentMetricType).getName(),
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
                    case STOP: break; //do nothing the loop will exit
                    default:
                        repaint();
                        Thread.sleep(1000);
                }
            }
        }
        catch (InterruptedException e) {
            // Place exception-handling code here in case an
            //       InterruptedException is thrown by Thread.sleep(),
            //		 meaning that another thread has interrupted this one
            this.frame.displayLog("!");
            e.printStackTrace();
            System.exit(1);
            //System.out.println(e.toString());
        }
        System.exit(0);
    }

    /**
     * getOrCreateMeter     Looks for the named meter of the the specified type, if it isn't found
     *                      it is created and stored before being returned
     * @param meterType     What sort of meter to look for / create
     * @param meterName     The Meter's name (found or created)
     * @return              A reference to the Meter
     */
    public Meter getOrCreateMeter(Meter.MeterType meterType, String meterName)
    {
        for(Meter meter:data.getMeters())
        {
            if (meter.getType() == meterType)
            {
                if (meter.name().equalsIgnoreCase(meterName))
                {
                    return meter;
                }
            }
        }
        //no meter found, add one
        Meter newMeter = new Meter(meterType); // sets up all possible metrics
        newMeter.setName(meterName);
        data.getMeters().softAdd(newMeter);
        return newMeter;
    }

    /**
     * displayCurrentReadings   puts the set of readings for the current metric on the log window
     */
    public void displayCurrentReadings()
    {
        frame.displayLog("Meter: "+ currentMeter.getType()+ " "+ currentMeter.name()+"\n");
        frame.displayLog("Metric: "+ currentMetricType+"\n");
        Metric metric = currentMeter.getMetric(currentMetricType);
        List<TimedRecord> readings = metric.getReadings();
        for (TimedRecord timedRecord:readings)
        {
            frame.displayLog(timedRecord.toCSV()+"\n");
        }
    }

    public void display(String s) {
        this.displayString = s;
        System.out.println(this.displayString);
        repaint();
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
        return this.state;
    }
    public  UiFrame getFrame() {
        return this.frame;
    }
    protected  FileAccess getFile() {
        return this.file;
    }
    public void setCurrentMetricType(MetricType metricType) {
        this.currentMetricType = metricType;
    }
    /*
 //   public DataService getDataService()
    {
        return dataService;
    }
    public void setDataService(DataService dataService)
    {
        this.dataService = dataService;
    }
    */
    public void setCurrentMeter(Meter meter){currentMeter = meter;}
    //
    // Access method for persistent data repository
    //
    public PersistentData getData(){return this.data;}
}
