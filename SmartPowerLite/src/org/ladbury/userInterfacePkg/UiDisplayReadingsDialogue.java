package org.ladbury.userInterfacePkg;

import org.ladbury.dataServicePkg.DataServiceMeter;
import org.ladbury.dataServicePkg.DataServiceMetric;
import org.ladbury.smartpowerPkg.SmartPower;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

public class UiDisplayReadingsDialogue extends JDialog
{
    private static final JLabel lblMeter    =   new JLabel("  Meter:");
    private static final JLabel lblMetric    =   new JLabel("  Metric:");
    private static final JLabel lblEarliest    =   new JLabel("  Start time:");
    private static final JLabel lblLatest    =   new JLabel("  End Time:");
    private final JComboBox<DataServiceMeter> comboMeter;
    private final JComboBox<DataServiceMetric> comboMetric;
    private final JComboBox<String> comboEarliest;
    private final JComboBox<String> comboLatest;
    //private final Collection<String> earliestTimes;
    //private final Collection<String> latestTimes;
    private Collection<String> readings;
    private final JButton btnOK = new JButton("OK");
    private DataServiceMeter meter;
    private DataServiceMetric metric;
    private String earliestTime;
    private String latestTime;

    UiDisplayReadingsDialogue(String title )
    {
        Collection<DataServiceMeter> meters = SmartPower.getMain().getDataService().getAvailableMeters();
        Collection<DataServiceMetric> metrics = SmartPower.getMain().getDataService().getAvailableMetrics();


                //record the initially selected values in case they are not changed

        if( meters.iterator().hasNext()) meter = meters.iterator().next();
        if( metrics.iterator().hasNext()) metric = metrics.iterator().next();
        Collection<String> readings = SmartPower.getMain().getDataService().getDBResourceForPeriodAsStrings(
                meter.getTag()+"/"+metric.getTag(), "2017-09-03 11:02:00","2017-09-06 11:03:01");

        if( readings.iterator().hasNext()) earliestTime = readings.iterator().next();
        if( readings.iterator().hasNext()) latestTime = readings.iterator().next();

        //Set up combo boxes and add a listener for changes
        comboMeter  =   new JComboBox<>(meters.toArray(new DataServiceMeter[meters.size()]));
        comboMeter.addActionListener(event -> {
            meter = (DataServiceMeter)comboMeter.getSelectedItem();
        });
        //comboMeter.addItemListener(comboListener);

        comboMetric =   new JComboBox<>(metrics.toArray(new DataServiceMetric[metrics.size()]));
        comboMetric.addActionListener(event -> {
            metric = (DataServiceMetric)comboMetric.getSelectedItem();
        });
        //comboMetric.addItemListener(comboListener);

        comboEarliest = new JComboBox<>(readings.toArray(new String[readings.size()]));
        comboEarliest.addActionListener(event -> {
            earliestTime = (String)comboEarliest.getSelectedItem();
        });
        //comboEarliest.addItemListener(comboListener);

        comboLatest =   new JComboBox<>(readings.toArray(new String[readings.size()]));
        comboLatest.addActionListener(event -> {
            latestTime = (String)comboLatest.getSelectedItem();
        });
        //comboLatest .addItemListener(comboListener);


        JPanel panel1 = new JPanel(new GridLayout(2,4));
        panel1.add(lblMeter);
        panel1.add(comboMeter);
        panel1.add(lblMetric);
        panel1.add(comboMetric);

        JSpinner earliestTimeSpinner = new JSpinner( new SpinnerDateModel() );
        JSpinner.DateEditor earliestTimeEditor = new JSpinner.DateEditor(earliestTimeSpinner, "d MMM yyyy HH:mm:ss");
        earliestTimeSpinner.setEditor(earliestTimeEditor);
        earliestTimeSpinner.setValue(new Date()); // will only show the current time
        earliestTimeSpinner.addChangeListener(e -> System.out.println(earliestTimeSpinner.getValue()));
        JSpinner latestTimeSpinner = new JSpinner( new SpinnerDateModel() );
        JSpinner.DateEditor latestTimeEditor = new JSpinner.DateEditor(latestTimeSpinner, "d MMM yyyy HH:mm:ss");
        latestTimeSpinner.setEditor(latestTimeEditor);
        latestTimeSpinner.setValue(new Date()); // will only show the current time
        latestTimeSpinner.addChangeListener(e -> System.out.println(latestTimeSpinner.getValue()));

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

        btnOK.addActionListener(event ->
        {
            System.out.println("Button Pressed");
            SmartPower.getMain().getDataService().printDBResourceForPeriod(meter.getTag()+"/"+metric.getTag(),earliestTime,latestTime);
        });

        this.add(btnOK, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }
}