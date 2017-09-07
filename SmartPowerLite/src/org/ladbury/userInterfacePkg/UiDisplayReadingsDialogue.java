package org.ladbury.userInterfacePkg;

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

public class UiDisplayReadingsDialogue extends JDialog implements ActionListener
{
    private static final JLabel lblMeter    =   new JLabel("Meter:");
    private static final JLabel lblMetric    =   new JLabel("Metric:");
    private static final JLabel lblEarliest    =   new JLabel("Start time:");
    private static final JLabel lblLatest    =   new JLabel("End Time:");
    private final JComboBox<String> comboMeter;
    private final JComboBox<String> comboMetric;
    private final JComboBox<String> comboEarliest;
    private final JComboBox<String> comboLatest;
    private final Collection<String> earliestTimes = new ArrayList<String>(Arrays.asList("time1", "time2"));
    private final Collection<String> latestTimes = new ArrayList<String>(Arrays.asList("time3", "time4"));
    private Collection<String> readings;
    private final JButton btnOK = new JButton("OK");
    private String meter;
    private String metric;
    private String earliestTime;
    private String latestTime;

    class ItemChangeListener implements ItemListener
    {
        @Override
        public void itemStateChanged(ItemEvent event) {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                Object item = event.getItem();
                System.out.println(item.toString());
                if (item.equals(comboMeter))
                {
                    meter = (String) comboMeter.getSelectedItem();
                    return;
                }
                if (item.equals(comboMetric))
                {
                    metric = (String) comboMetric.getSelectedItem();
                    return;
                }
                if (item.equals(comboEarliest)) earliestTime = (String)comboEarliest.getSelectedItem();
                if (item.equals(comboLatest)) latestTime = (String)comboLatest.getSelectedItem();
            }
        }
    }

    private final ItemChangeListener comboListener = new ItemChangeListener();

    UiDisplayReadingsDialogue(String title, Collection<String > meterNames,Collection<String > metricNames )
    {
        //Set up combo boxes and add a listener for changes
        comboMeter  =   new JComboBox<>(meterNames.toArray(new String[meterNames.size()]));
        comboMeter.addItemListener(comboListener);
        comboMetric =   new JComboBox<>(metricNames.toArray(new String[metricNames.size()]));
        comboMetric.addItemListener(comboListener);
        comboEarliest = new JComboBox<>(earliestTimes.toArray(new String[earliestTimes.size()]));
        comboEarliest.addItemListener(comboListener);
        comboLatest =   new JComboBox<>(latestTimes.toArray(new String[earliestTimes.size()]));
        comboLatest .addItemListener(comboListener);

        Collection<String> readings = SmartPower.getMain().getDataService().getDBResourceForPeriodAsStrings(
                "whole_house/voltage", "2017-09-03 11:02:00","2017-09-06 11:03:01");

                //record the initially selected values in case they are not changed
        if( meterNames.iterator().hasNext()) meter = meterNames.iterator().next();
        if( metricNames.iterator().hasNext()) metric = metricNames.iterator().next();
        readings = SmartPower.getMain().getDataService().getDBResourceAsStrings(meter.replace(" ", "_").toLowerCase()
                +"/"+metric.replace(" ", "").toLowerCase());
        if( readings.iterator().hasNext()) earliestTime = readings.iterator().next();
        if( readings.iterator().hasNext()) latestTime = readings.iterator().next();


        JPanel panel1 = new JPanel(new GridLayout(2,4));
        panel1.add(lblMeter);
        panel1.add(comboMeter);
        panel1.add(lblMetric);
        panel1.add(comboMetric);
        panel1.add(lblEarliest);
        panel1.add(comboEarliest);
        panel1.add(lblLatest);
        panel1.add(comboLatest);

        // Set up the panel
        panel1.setPreferredSize(new Dimension( 400,50));
        panel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        //Set up the dialog
        setTitle(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.add(panel1,BorderLayout.CENTER);
        btnOK.addActionListener(this::actionPerformed);
        this.add(btnOK, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }
    @Override
    public void actionPerformed(ActionEvent e)
    {
        System.out.println("Button Pressed");
        SmartPower.getMain().getDataService().printDBResourceForPeriod(meter+"/"+metric,earliestTime,latestTime);
    }
}