package org.ladbury.userInterfacePkg;

import me.mawood.data_api_client.accessors.DataTypeAccessor;
import me.mawood.data_api_client.accessors.DeviceAccessor;
import me.mawood.data_api_client.accessors.ReadingAccessor;
import me.mawood.data_api_client.objects.DataType;
import me.mawood.data_api_client.objects.Device;
import me.mawood.data_api_client.objects.Reading;
import org.ladbury.chartingPkg.PieChart;
import org.ladbury.chartingPkg.ScatterChart;
import org.ladbury.chartingPkg.TimeHistogram;
import org.ladbury.meterPkg.Meter;
import org.ladbury.meterPkg.MetricType;
import org.ladbury.meterPkg.TimedRecord;
import org.ladbury.meterPkg.TimestampedDouble;
import org.ladbury.smartpowerPkg.SmartPower;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("unused")
public class UiFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	static final String API_URL = "http://192.168.1.164/api/";

    private final JMenuBar jMenuBar1 = new JMenuBar();

    private final JMenu jMenuFile = new JMenu("File");
    private final JMenuItem jMenuFileOpen = new JMenuItem("Open");
    private final JMenuItem jMenuFileSave = new JMenuItem("Save");
    private final JMenuItem jMenuFileExit = new JMenuItem("Exit");

    private final JMenu jMenuData = new JMenu("Data");
    private final JMenuItem jMenuDataMeters = new JMenuItem("Meter options");
    private final JMenuItem jMenuDataMetrics = new JMenuItem("Metric options");
    private final JMenuItem jMenuDataDisplayMetrics = new JMenuItem("Display metrics");
    private final JMenuItem jMenuDataArchiveMetrics = new JMenuItem("Archive Metrics");

    private final JMenu jMenuProcess = new JMenu("Process");
    private final JMenuItem jMenuProcessRecords = new JMenuItem("Process Edges");
    private final JMenuItem jMenuProcessDevices = new JMenuItem("Process Devices");
    
    private final JMenu jMenuChart = new JMenu("Chart");
    //private JMenuItem jMenuChartPie = new JMenuItem("Pie 3D");
    private final JMenuItem jMenuChartHistogram = new JMenuItem("Histogram");
    private final JMenuItem jMenuChartScatter = new JMenuItem("Scatter");

    private final JMenu jMenuHelp = new JMenu("Help");
    private final JMenuItem jMenuHelpAbout = new JMenuItem("About..");
    private final TextArea textArea1 = new TextArea();

    private FileDialog fileDialogue = null;
	private String 	windowTitle = null;
	private final SmartPower owner;

    //
    // Construct the frame
    //
    public UiFrame(String windowTitle, SmartPower owner) {
        super(windowTitle);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        this.owner = owner;
        try {
            // Create a content pane
            JPanel contentPane = (JPanel) this.getContentPane();
            contentPane.setLayout(new BorderLayout());
            this.setSize(new Dimension(800, 600));
            this.setTitle(windowTitle);

            // add a menu bar
            this.setJMenuBar(jMenuBar1);

            // initialise the menus
            createFileMenu();
            createDataMenu();
            createProcessMenu();
            createChartMenu();
            createHelpMenu();

            // add log text area
            textArea1.setBackground(Color.pink);
            textArea1.setColumns(80);
            textArea1.setCursor(null);
            textArea1.setEditable(false);
            textArea1.setFont(UiStyle.NORMAL_FONT);
            textArea1.setRows(20);
            textArea1.setText("Smart Power System Messages\n");

            contentPane.add(textArea1, BorderLayout.CENTER);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createFileMenu(){

        // add sub items and their actions 
        jMenuFile.add(jMenuFileOpen);
        jMenuFileOpen.addActionListener(this::jMenuFileOpen_actionPerformed);
        jMenuFile.add(jMenuFileSave);
        jMenuFileSave.addActionListener(this::jMenuFileSave_actionPerformed);
        jMenuFile.addSeparator();
        jMenuFile.add(jMenuFileExit);
        jMenuFileExit.addActionListener(e -> owner.stop());
        // add the menu to the menu bar
        jMenuBar1.add(jMenuFile);
    }

    private void createDataMenu()
    {
        jMenuData.add(jMenuDataMeters);
        jMenuDataMeters.addActionListener(this::jMenuDataMeters_actionPerformed);
        jMenuData.add(jMenuDataMetrics);
        jMenuDataMetrics.addActionListener(this::jMenuDataMetrics_actionPerformed);
        jMenuData.add(jMenuDataDisplayMetrics);
        jMenuDataDisplayMetrics.addActionListener(this::jMenuDataDisplay_actionPerformed);
        jMenuData.add(jMenuDataArchiveMetrics);
        jMenuDataArchiveMetrics.addActionListener(this::jMenuDataArchive_actionPerformed);
        // add the menu to the menu bar
        jMenuBar1.add(jMenuData);
    }

    private void createProcessMenu(){
        // add sub items and their actions      
        jMenuProcessRecords.addActionListener(e -> owner.change_state(SmartPower.RunState.PROCESS_EDGES));
        jMenuProcess.add(jMenuProcessDevices);
        jMenuProcessDevices.addActionListener(e -> owner.change_state(SmartPower.RunState.PROCESS_EVENTS));
        jMenuProcess.add(jMenuProcessRecords);
        jMenuProcess.add(jMenuProcessDevices);
        // add the menu to the menu bar
        jMenuBar1.add(jMenuProcess);
    }

    private void createChartMenu(){
        // add sub items and their actions    
    	/*
        jMenuChartPie.addActionListener(e-> jMenuChartPie_actionPerformed(e));
        jMenuChart.add(jMenuChartPie);*/
        jMenuChartHistogram.addActionListener(this::jMenuChartHistogram_actionPerformed);
        jMenuChart.add(jMenuChartHistogram);
        jMenuChartScatter.addActionListener(this::jMenuChartScatter_actionPerformed);
        jMenuChart.add(jMenuChartScatter);
       // add the menu to the menu bar
        jMenuBar1.add(jMenuChart);
    }
    
    private void createHelpMenu(){
        // add sub items and their actions      
        jMenuHelpAbout.addActionListener(this::jMenuHelpAbout_actionPerformed);
        jMenuHelp.add(jMenuHelpAbout);
        // add the menu to the menu bar
        jMenuBar1.add(jMenuHelp);
    }

    //Overridden so we can exit when window is closed
    public void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            owner.stop();
        	//System.exit(0);
        }
    }

    //
    //File | Open action performed
    //
    private void jMenuFileOpen_actionPerformed(ActionEvent e) {
        //FilenameFilter m_filter = "*.csv";
        // create a file dialogue
        fileDialogue = new FileDialog(this, "Open measurement readings (.csv) file");
        //fileDialogue.setFilenameFilter((FilenameFilter)"*.csv");
        fileDialogue.setDirectory("c:/");
        fileDialogue.setVisible(true);
        // File Dialogue is modal so won't return unless file or cancel
        if(fileDialogue.getFile() != null ){
        	owner.change_state(SmartPower.RunState.OPEN_FILE); //trigger processing in main loop
        }
        // else the user cancelled the dialog, do nothing
    }

    //
    //File | Save action performed
    //
    private void jMenuFileSave_actionPerformed(ActionEvent e) {
        //FilenameFilter m_filter = "*.csv";
        // create a file dialogue
        fileDialogue = new FileDialog(this, "Open measurement readings (.csv) file");
        //fileDialogue.setFilenameFilter((FilenameFilter)"*.csv");
        fileDialogue.setDirectory("c:/");
        fileDialogue.setVisible(true);
        // File Dialogue is modal so won't return unless file or cancel
        if(fileDialogue.getFile() != null ){
        	owner.change_state(SmartPower.RunState.SAVE_FILE);  //trigger processing in main loop
        }
    }

    //
    //Data | Meters action performed
    //
    private void jMenuDataMeters_actionPerformed(ActionEvent actionEvent)
    {
        DeviceAccessor deviceAccessor = new DeviceAccessor(API_URL);
        Collection<Device> devices = deviceAccessor.getDevices();
        UiListBox meterBox = new UiListBox("Meters");
        for (Device d:devices) meterBox.add(d.getName());
        meterBox.pack();
        meterBox.setVisible(true);
    }

    //
    //Data | Metrics action performed
    //
    private void jMenuDataMetrics_actionPerformed(ActionEvent actionEvent)
    {
        DataTypeAccessor dataTypeAccessor = new DataTypeAccessor(API_URL);
        Collection<DataType> dataTypes = dataTypeAccessor.getDataTypes();
        UiListBox metricBox = new UiListBox("Metrics");
        for (DataType d:dataTypes) metricBox.add(d.getName());
        metricBox.pack();
        metricBox.setVisible(true);
    }

    //
    //Data | Metrics Display action performed
    //
    private void jMenuDataDisplay_actionPerformed(ActionEvent actionEvent)
    {
        UiReadingsSelectionDialogue readingsDialogue = new UiReadingsSelectionDialogue("Readings", UiReadingsSelectionDialogue.RequestType.LOAD_API_DATA,this);
        readingsDialogue.pack();
        readingsDialogue.setVisible(true);
    }

    void handleReadingsDialogueResultsForDisplay(ReadingsRange readingsRange)
    {
        final ReadingAccessor readingAccessor = new ReadingAccessor(API_URL);
        System.out.println(readingsRange);
        final Collection <Reading> readings = readingAccessor.getReadingsFor(
                readingsRange.getDevice().getTag(),
                readingsRange.getDataType().getTag(),
                readingsRange.getEarliestTime().toInstant().toEpochMilli(),
                readingsRange.getLatestTime().toInstant().toEpochMilli());
        System.out.println(readings);
        Meter meter = owner.getOrCreateMeter(Meter.MeterType.PMON10, readingsRange.getDevice().getName());
        MetricType metricType = MetricType.getMetricTypeFromTag(readingsRange.getDataType().getTag());
        if (metricType == null) return; //problem
        owner.setCurrentMetricType(metricType);
        owner.setCurrentMeter(meter);
        for (Reading reading : readings)
        {
            meter.getMetric(metricType).appendRecord(new TimedRecord(new TimestampedDouble(reading.getReading(), reading.getTimestamp())));
        }
        owner.change_state(SmartPower.RunState.DISPLAY_API_DATA); //trigger processing in main loop
    }

    //
    //Data | Metrics Archive action performed
    //
    private void jMenuDataArchive_actionPerformed(ActionEvent actionEvent)
    {
        UiReadingsSelectionDialogue readingsDialogue = new UiReadingsSelectionDialogue("Readings", UiReadingsSelectionDialogue.RequestType.ARCHIVE_API_DATA,this);
        readingsDialogue.pack();
        readingsDialogue.setVisible(true);

    }
    void handleReadingsDialogueResultsForArchive(ReadingsRange readingsRange)
    {
        final ReadingAccessor readingAccessor = new ReadingAccessor(API_URL);
        final Collection <Reading> readings = readingAccessor.getReadingsFor(
                readingsRange.getDevice().getName(),
                readingsRange.getDataType().getName(),
                readingsRange.getEarliestTime().toInstant().toEpochMilli(),
                readingsRange.getLatestTime().toInstant().toEpochMilli());
        Meter meter = owner.getOrCreateMeter(Meter.MeterType.PMON10, readingsRange.getDevice().getName());
        MetricType metricType = MetricType.getMetricTypeFromTag(readingsRange.getDataType().getTag());
        if (metricType == null) return; //problem
        owner.setCurrentMetricType(metricType);
        owner.setCurrentMeter(meter);
        for (Reading reading : readings)
        {
            meter.getMetric(metricType).appendRecord(new TimedRecord(new TimestampedDouble(reading.getReading(), reading.getTimestamp())));
        }
        owner.change_state(SmartPower.RunState.ARCHIVE_API_DATA); //trigger processing in main loop
    }
    //
    //Chart Pie action performed
    //
    public void jMenuChartPie_actionPerformed(ActionEvent e) {
    	PieChart demo = new PieChart("Comparison", "Which operating system are you using?");
        demo.pack();
        demo.setVisible(true);
    }

    //
    //Histogram action performed
    //
    private void jMenuChartHistogram_actionPerformed(ActionEvent e) {
     	for (Meter m: owner.getData().getMeters())
        {
            ArrayList<TimeHistogram> histograms = new ArrayList<>(Collections.emptyList());
            for (int i = 0; i < m.getMetricCount(); i++)
            {
                if (m.getMetric(i).getReadingsCount() > 0)
                {
                    System.out.println("meter type: "+ m.getType()+" meter name: "+ m.name()+ " metric name: "+ m.getMetric(i).getName());
                    histograms.add(new TimeHistogram("Power Histogram", m.getMetric(i), "Power (W)"));
                }
            }
            for (int i = 0; i < histograms.size(); i++)
            {
                histograms.get(i).setLocation(i * 20, i * 20);  //cascade windows
                histograms.get(i).pack();
                //RefineryUtilities.centerFrameOnScreen(histograms.get(i));
                histograms.get(i).setVisible(true);
            }
        }
    }
    //
    //Chart Scatter action performed
    //
    private void jMenuChartScatter_actionPerformed(ActionEvent e) {

        final ScatterChart plot = new ScatterChart("Device Scatter Chart");
        plot.pack();
        //RefineryUtilities.centerFrameOnScreen(plot);
        plot.setVisible(true);
    }
    

      //
      //Help About action performed
      //
      private void jMenuHelpAbout_actionPerformed(ActionEvent e) {
          UiAboutBox dlg = new UiAboutBox(this);
          Dimension dlgSize = dlg.getPreferredSize();
          Dimension frmSize = getSize();
          Point loc = getLocation();
          dlg.setLocation( (frmSize.width - dlgSize.width) / 2 + loc.x,
                          (frmSize.height -
                           dlgSize.height) / 2 + loc.y);
          dlg.setModal(true);
          dlg.setVisible(true);
      }

    //
    // write a String to the log area
    //
    public void displayLog(String str) {
        textArea1.append(str);
        repaint();
    }
    //
    // write an int to the log area
    //
    public void displayLog(int i) {
        Integer intWrapper = i;
        textArea1.append(intWrapper.toString());
        repaint();
    }
    public FileDialog getFileDialog() {
		return fileDialogue;
	}

	public void setFileDialog(FileDialog m_filediag1) {
		this.fileDialogue = m_filediag1;
		}
}