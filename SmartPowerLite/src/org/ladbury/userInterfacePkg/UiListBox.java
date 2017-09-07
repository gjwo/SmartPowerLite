package org.ladbury.userInterfacePkg;
import javax.swing.*;

public class UiListBox extends JDialog
{

    private DefaultListModel<String> listModel;

    UiListBox(String title)
    {
        listModel = new DefaultListModel<>();
        JList<String> elementList = new JList<>(listModel);
        JPanel panel1 = new JPanel();

        setTitle(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(200, 200);
        panel1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel1.add(elementList);
        add(panel1);
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
    }
    public void add(String s){listModel.addElement(s);}
}