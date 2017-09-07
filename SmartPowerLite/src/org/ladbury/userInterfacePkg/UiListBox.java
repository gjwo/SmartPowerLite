package org.ladbury.userInterfacePkg;
import javax.swing.*;
import java.awt.*;

public class UiListBox extends JDialog
{

    private DefaultListModel<String> listModel;

    UiListBox(String title)
    {
        listModel = new DefaultListModel<>();
        JList<String> elementList = new JList<>(listModel);
        JPanel panel1 = new JPanel();

        // Set up the panel
        panel1.setPreferredSize(new Dimension( 200,1000));
        panel1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel1.add(elementList);

        //Add the panel to the scroll frame
        JScrollPane scrollFrame = new JScrollPane(panel1);
        panel1.setAutoscrolls(true);
        scrollFrame.setPreferredSize(new Dimension(250,300));

        //Set up the dialog
        setTitle(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.add(scrollFrame);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }
    public void add(String s){listModel.addElement(s);}
}