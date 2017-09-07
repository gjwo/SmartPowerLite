package org.ladbury.userInterfacePkg;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import java.awt.*;

public class UiListBox extends JFrame
{

    private JList<String> ElementList;
    private DefaultListModel<String> listModel;
    public UiListBox(String Title)
    {
        //create the model and add elements
        listModel = new DefaultListModel<>();
        //create the list
        ElementList = new JList<>(listModel);
        add(ElementList);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setTitle(Title);
        this.setSize(200, 200);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
    public void add(String s){listModel.addElement(s);}
    public void refreshContent(){ElementList = new JList<>(listModel);}
    public void clearContent(){listModel = new DefaultListModel<>();}
}