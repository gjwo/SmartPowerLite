package org.ladbury.smartpowerPkg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.ladbury.deviceActivityPkg.DeviceActivity;
import org.ladbury.meterPkg.Meter;
import org.ladbury.meterPkg.Meter.MeterType;
import org.ladbury.meterPkg.Metric;
import org.ladbury.meterPkg.Metric.MetricType;
import org.ladbury.meterPkg.TimedRecord;
import org.ladbury.persistentData.PersistentList;


//
//
// FileAccess
//
//
class FileAccess
     {
    /**
	 * 
	 */
	
	private String inputPathName;
	private String outputPathName;
    private String inputFileName;
    private String outputFileName;
    private File inputFile, outputFile;
    private FileInputStream m_in;
    private BufferedReader m_br;
    private PrintWriter m_pw;
 
    protected void SPFile() {
    	/*
    	 *  Constructor
    	 */
    	inputPathName = null;
        inputFileName = null;
        inputFile = null;
        m_in = null;
        m_br = null;
        m_pw = null;
        
    }

    void openInput()
    //throws IOException
    // opens a file as a buffered stream
    {
        try {
        	//SmartPower.m_main.frame.displayLog("FileAccess Opening file " + inputPathName + inputFileName + "\n\r");
            inputFile = new File(inputPathName, inputFileName);
            m_in = new FileInputStream(inputFile);
            m_br = new BufferedReader(new InputStreamReader(m_in));
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            SmartPower.getMain().getFrame().displayLog(ioe.toString());
            //throw ioe;
        }
    }

    void closeInput()
    //throws IOException
    {
        try {
            m_br = null;
            m_in.close();
            m_in = null;
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println(ioe.toString());
            //throw ioe;
        }
    }

    private void closeOutput()
    //throws IOException
    {
        try {
        	SmartPower.getMain().getFrame().displayLog("Closing output file " + inputPathName +
                    outputFileName + "\n\r");
            m_pw.close();
        	outputFile = null;
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
            //throw ioe;
        }
    }
    private PrintWriter openOutput(String dirname, String filename)
    {
    	try{
        	SmartPower.getMain().getFrame().displayLog("Opening output file " + dirname +
                    filename + "\n\r");
        	outputFile = null;
            outputFile = new File(dirname, filename);
            // Open file to print output to
            m_pw = new PrintWriter( new FileOutputStream(outputFile), true);
    	}catch(Exception e){
            e.printStackTrace();
            System.out.println(e.toString());
    	}
        return m_pw;
    }

    protected PrintWriter outputWriter()
    {
        return m_pw;
    }
 
    
    String state() {
        if (inputFile == null) {
            return ("Closed");
        }
        else {
            return ("Open");
        }
    }

    //
    // process_file
    //
    // to turn each line of input tokens into readings tokens
    // then to group these into readings records for parsing
    //
    void processFile(MetricType metricT) {
 		String 	dataRow;
        Meter mtr = SmartPower.getMain().getData().getMeters().get(0);
        Metric mtc = mtr.getMetric(metricT);
        try {
        	switch (mtr.getType()){
        	case OWLCM160:      	
            	break; // finished as EOF reached
                
            case ONZO:
            	//SmartPower.m_main.frame.displayLog("Loading readings from file\n\r");
             	dataRow = m_br.readLine(); // Read the first line of data.
            	// The while checks to see if the data is null. If it is, we've hit
            	// the end of the file. If not, process the data.
             	while (dataRow != null){
            		mtc.appendRecord(new TimedRecord(dataRow.split(",")));
            		dataRow = m_br.readLine(); // Read next line of data.
            	}  
            	
            	break; // finished as EOF reached
 			default:
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println(ioe.toString());
            //throw ioe;
        }
        catch ( ParseException pe){
        	System.out.println(pe.toString());
        }
    }

    
    /**
     * Output metric as a CSV file
     * @param mt Meter type 
     * @param mtc Metric
     */
    void OutputMetricAsCSVFile(MeterType mt, Metric mtc){
    	switch(mt){
    	case OWLCM160:
    		break;
    	case ONZO:
        	// construct output filename and open file
        	outputFileName = mtc.readingsName()+"_R.csv";
            m_pw = openOutput(outputPathName, outputFileName);
            //save Reading events
            mtc.outputReadingsCSV(m_pw);
            closeOutput();
            //save device Activity
            
        	outputFileName = mtc.readingsName()+"_DA.csv";
            m_pw = openOutput(outputPathName, outputFileName);
        	for(int i=0; i<SmartPower.getMain().getData().getActivity().size();i++){
         		m_pw.println(SmartPower.getMain().getData().getActivity().get(i).toCSV());
        	}
            closeOutput();
    		break;
    	default:
    		break;
    	}
        //m_main.frame.displayLog("Run: back from events save\n");
    }
    
    /**
     * Output Activity as CSV file
     * @param activityName
     * @param activity
     */
    void OutputActivityAsCSVFile(String activityName, PersistentList<DeviceActivity> activity){
          
       outputFileName = activityName+"_DA.csv";
       m_pw = openOutput(outputPathName, outputFileName);
       for(int i=0; i<activity.size();i++){
       		m_pw.println(SmartPower.getMain().getData().getActivity().get(i).toCSV());
       	}
       //m_main.frame.displayLog("Run: back from events save\n");
    }
   
    /**
     * Find CSV files for date
     * @param dirName
     * @param t
     * @return
     */
    protected List<String> findCSVFilesForDate(String dirName, Timestamp t){
		DateFormat df = new SimpleDateFormat(Timestamped.FILE_DATE_FORMAT); 

    	List<String> selectedFiles = new ArrayList<>();
    	File dir = new File(dirName);
    	String filename;
    	
    	for (File file : dir.listFiles()) {
    		filename = file.getName();
    	    if (filename.toLowerCase().endsWith((".csv")) && filename.contains(df.format(t))) {
    	      selectedFiles.add(filename);
    	    }
    	}
    	return selectedFiles;
   	}
    
    MetricType identifyTypeFromFilename(String filename){
    	MetricType mtcT = MetricType.UNDEFINED;
    	MetricType[] mtcTs = MetricType.values();
    	for (int i = 1; i < mtcTs.length; i++){
    		if (filename.toLowerCase().contains(mtcTs[i].toString().toLowerCase())){
    			mtcT = mtcTs[i];
    			break;
    		}
    	}
    	return mtcT;
    }

    //
    // Accessor methods
    //

    void setInputFilename(String s){
    	inputFileName = s;
    }

    String inputFilename(){
    	return inputFileName;
    }
    
    void setInputPathname(String s){
    	inputPathName = s;
    }

    String inputPathname(){
    	return inputPathName;
    }
    

    void setOutputFilename(String s){
    	outputFileName = s;
    }

    String outputFilename(){
    	return outputFileName;
    }

    void setOutputPathname(String s){
    	outputPathName = s;
    }

    String outputPathname(){
    	return outputPathName;
    }
    
    protected PrintWriter pw(){
    	return m_pw;
    }
}