package org.ladbury.userInterfacePkg;

import me.mawood.data_api_client.accessors.DataTypeAccessor;
import me.mawood.data_api_client.accessors.DeviceAccessor;
import me.mawood.data_api_client.objects.DataType;
import me.mawood.data_api_client.objects.Device;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Date;

import static org.ladbury.userInterfacePkg.UiFrame.API_URL;

class UiReadingsSelectionDialogue extends JDialog
{
    private static final JLabel lblMeter    = new JLabel("  Meter:");
    private static final JLabel lblMetric   = new JLabel("  Metric:");
    private static final JLabel lblEarliest = new JLabel("  Start time:");
    private static final JLabel lblLatest   = new JLabel("  End Time:");
    private final JComboBox<Device> comboMeter;
    private final JComboBox<DataType> comboMetric;
    private final ReadingsRange readingsRange;
   enum RequestType{LOAD_API_DATA,ARCHIVE_API_DATA}

    UiReadingsSelectionDialogue(String title, RequestType requestType, UiFrame owner)
    {
        readingsRange = new ReadingsRange();
        DeviceAccessor deviceAccessor = new DeviceAccessor(API_URL);
        Collection<Device> devices = deviceAccessor.getDevices();
        DataTypeAccessor dataTypeAccessor = new DataTypeAccessor(API_URL);
        Collection<DataType> dataTypes = dataTypeAccessor.getDataTypes();


                //record the initially selected values in case they are not changed

        if( devices.iterator().hasNext()) readingsRange.setDevice(devices.iterator().next());
        if( dataTypes.iterator().hasNext()) readingsRange.setDataType(dataTypes.iterator().next());

        //Set up combo boxes and add a listener for changes
        comboMeter  =   new JComboBox<>(devices.toArray(new Device[devices.size()]));
        comboMeter.addActionListener(event -> readingsRange.setDevice((Device)comboMeter.getSelectedItem()));
        //comboMeter.addItemListener(comboListener);

        comboMetric =   new JComboBox<>(dataTypes.toArray(new DataType[dataTypes.size()]));
        comboMetric.addActionListener(event -> readingsRange.setDataType((DataType)comboMetric.getSelectedItem()));
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
        earliestTimeSpinner.addChangeListener(e -> readingsRange.setEarliestTime((Date)earliestTimeSpinner.getValue()));

        JSpinner latestTimeSpinner = new JSpinner( new SpinnerDateModel() );
        JSpinner.DateEditor latestTimeEditor = new JSpinner.DateEditor(latestTimeSpinner, "d MMM yyyy HH:mm:ss");
        latestTimeSpinner.setEditor(latestTimeEditor);
        latestTimeSpinner.setValue(new Date()); // will only show the current time
        latestTimeSpinner.addChangeListener(e -> readingsRange.setLatestTime((Date)latestTimeSpinner.getValue()));

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
            switch( requestType)
            {
                case LOAD_API_DATA:
                    owner.handleReadingsDialogueResultsForDisplay(readingsRange);
                    break;
                case ARCHIVE_API_DATA:
                    owner.handleReadingsDialogueResultsForArchive(readingsRange);
                    break;
            }
            this.dispose();
        });

        this.add(btnOK, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }
}