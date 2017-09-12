package org.ladbury.userInterfacePkg;

import org.ladbury.dataServicePkg.DataServiceMeter;
import org.ladbury.dataServicePkg.DataServiceMetric;
import org.ladbury.dataServicePkg.DataServiceReadings;
import org.ladbury.meterPkg.*;
import org.ladbury.smartpowerPkg.SmartPower;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Date;

class UiDisplayReadingsDialogue extends JDialog
{
    private static final JLabel lblMeter    =   new JLabel("  Meter:");
    private static final JLabel lblMetric    =   new JLabel("  Metric:");
    private static final JLabel lblEarliest    =   new JLabel("  Start time:");
    private static final JLabel lblLatest    =   new JLabel("  End Time:");
    private final JComboBox<DataServiceMeter> comboMeter;
    private final JComboBox<DataServiceMetric> comboMetric;
    private DataServiceMeter DataServiceMeter;
    private DataServiceMetric dataServiceMetric;
    private Date earliestTime;
    private Date latestTime;

    UiDisplayReadingsDialogue(String title )
    {
        Collection<DataServiceMeter> meters = SmartPower.getMain().getDataService().refreshMetersFromDB();
        Collection<DataServiceMetric> metrics = SmartPower.getMain().getDataService().refreshMetricsFromDB();


                //record the initially selected values in case they are not changed

        if( meters.iterator().hasNext()) DataServiceMeter = meters.iterator().next();
        if( metrics.iterator().hasNext()) dataServiceMetric = metrics.iterator().next();

        //Set up combo boxes and add a listener for changes
        comboMeter  =   new JComboBox<>(meters.toArray(new DataServiceMeter[meters.size()]));
        comboMeter.addActionListener(event -> DataServiceMeter = (DataServiceMeter)comboMeter.getSelectedItem());
        //comboMeter.addItemListener(comboListener);

        comboMetric =   new JComboBox<>(metrics.toArray(new DataServiceMetric[metrics.size()]));
        comboMetric.addActionListener(event -> dataServiceMetric = (DataServiceMetric)comboMetric.getSelectedItem());
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
            //System.out.println("Button Pressed");
            processReadings();
            SmartPower.getMain().displayCurrentReadings();
            this.dispose();
        });

        this.add(btnOK, BorderLayout.SOUTH);
        //setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }
    private void processReadings()
    {
        final DataServiceReadings readings = SmartPower.getMain().getDataService().refreshMetricForPeriodFromDB(
                DataServiceMeter,dataServiceMetric,earliestTime.toInstant(),latestTime.toInstant());
        Meter meter = SmartPower.getMain().getOrCreateMeter(Meter.MeterType.PMON10,DataServiceMeter.getDisplayName());
        MetricType metricType = MetricType.getMetricTypeFromTag(dataServiceMetric.getTag());
        if (metricType == null) return; //problem
        SmartPower.getMain().setCurrentMetricType(metricType);
        SmartPower.getMain().setCurrentMeter(meter);
        for (TimestampedDouble reading : readings.getReadings())
        {
            meter.getMetric(metricType).appendRecord(new TimedRecord(reading));
        }
     }
}