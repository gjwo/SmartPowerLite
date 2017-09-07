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
        JPanel panel1 = new JPanel(new BorderLayout());

        // Set up the panel
        panel1.setMaximumSize(new Dimension( 200,1000));
        panel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel1.add(elementList);

        //Add the panel to the scroll frame
        JScrollPane scrollFrame = new JScrollPane(panel1);
        panel1.setAutoscrolls(true);
        scrollFrame.setMaximumSize(new Dimension(400,300));
        scrollFrame.setLayout(new ScrollPaneLayout());

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