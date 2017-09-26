package org.ladbury.userInterfacePkg;

import me.mawood.data_api_client.accessors.DataTypeAccessor;
import me.mawood.data_api_client.accessors.DeviceAccessor;
import me.mawood.data_api_client.accessors.ReadingAccessor;
import me.mawood.data_api_client.objects.DataType;
import me.mawood.data_api_client.objects.Device;
import me.mawood.data_api_client.objects.Reading;
import org.ladbury.meterPkg.*;
import org.ladbury.smartpowerPkg.SmartPower;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Date;

import static org.ladbury.userInterfacePkg.UiFrame.API_URL;

class UiLoadReadingsDialogue extends JDialog
{
   private static final JLabel lblMeter    = new JLabel("  Meter:");
    private static final JLabel lblMetric   = new JLabel("  Metric:");
    private static final JLabel lblEarliest = new JLabel("  Start time:");
    private static final JLabel lblLatest   = new JLabel("  End Time:");
    private final JComboBox<Device> comboMeter;
    private final JComboBox<DataType> comboMetric;
    private Device device;
    private DataType dataType;
    private Date earliestTime;
    private Date latestTime;

    @SuppressWarnings("SameParameterValue")
    UiLoadReadingsDialogue(String title )
    {
        DeviceAccessor deviceAccessor = new DeviceAccessor(API_URL);
        Collection<Device> devices = deviceAccessor.getDevices();
        DataTypeAccessor dataTypeAccessor = new DataTypeAccessor(API_URL);
        Collection<DataType> dataTypes = dataTypeAccessor.getDataTypes();


                //record the initially selected values in case they are not changed

        if( devices.iterator().hasNext()) device = devices.iterator().next();
        if( dataTypes.iterator().hasNext()) dataType = dataTypes.iterator().next();

        //Set up combo boxes and add a listener for changes
        comboMeter  =   new JComboBox<>(devices.toArray(new Device[devices.size()]));
        comboMeter.addActionListener(event -> device = (Device)comboMeter.getSelectedItem());
        //comboMeter.addItemListener(comboListener);

        comboMetric =   new JComboBox<>(dataTypes.toArray(new DataType[dataTypes.size()]));
        comboMetric.addActionListener(event -> dataType = (DataType)comboMetric.getSelectedItem());
        //comboMetric.addItemListener(comboListener);

        JPanel panel1 = new JPanel(new GridLayout(2,4));
        panel1.add(lblMeter);
        panel1.add(comboMeter);
        panel1.add(lblMetric);
        panel1.add(comboMetric);

        JSpinner earliestTimeSpinner = new JSpinner( new SpinnerDateModel() );
        JSpinner.DateEditor earliestTimeEditor = new JSpinner.DateEditor(earliestTimeSpinner, "d MMM yyyy HH:mm:ss");
        earliestTimeSpinner.setEditor(earliestTimeEditor);
        earliestTimeSpinner.setValue(new Date()); // will only show the current time
        earliestTimeSpinner.addChangeListener(e -> earliestTime = (Date)earliestTimeSpinner.getValue());

        JSpinner latestTimeSpinner = new JSpinner( new SpinnerDateModel() );
        JSpinner.DateEditor latestTimeEditor = new JSpinner.DateEditor(latestTimeSpinner, "d MMM yyyy HH:mm:ss");
        latestTimeSpinner.setEditor(latestTimeEditor);
        latestTimeSpinner.setValue(new Date()); // will only show the current time
        latestTimeSpinner.addChangeListener(e -> latestTime = (Date)latestTimeSpinner.getValue());

        earliestTime = new Date();
        latestTime = new Date();

        panel1.add(lblEarliest);
        panel1.add(earliestTimeSpinner);
        panel1.add(lblLatest);
        panel1.add(latestTimeSpinner);

        // Set up the panel
        panel1.setPreferredSize(new Dimension( 600,50));
        panel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        //Set up the dialog
        setTitle(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.add(panel1,BorderLayout.CENTER);

        JButton btnOK = new JButton("OK");
        btnOK.addActionListener(event ->
        {
            loadAPIData();
            SmartPower.getMain().change_state(SmartPower.RunState.PROCESS_API_DATA); //trigger processing in main loop
            this.dispose();
        });

        this.add(btnOK, BorderLayout.SOUTH);
        //setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }
    private void loadAPIData()
    {
        final ReadingAccessor readingAccessor = new ReadingAccessor(API_URL);
        final Collection <Reading> readings = readingAccessor.getReadingsFor(device.getName(),
                                                                dataType.getName(),
                                                                earliestTime.toInstant().toEpochMilli(),
                                                                latestTime.toInstant().toEpochMilli());
        Meter meter = SmartPower.getMain().getOrCreateMeter(Meter.MeterType.PMON10, device.getName());
        MetricType metricType = MetricType.getMetricTypeFromTag(dataType.getTag());
        if (metricType == null) return; //problem
        SmartPower.getMain().setCurrentMetricType(metricType);
        SmartPower.getMain().setCurrentMeter(meter);
        for (Reading reading : readings)
        {
            meter.getMetric(metricType).appendRecord(new TimedRecord(new TimestampedDouble(reading.getReading(), reading.getTimestamp())));
        }
     }
}