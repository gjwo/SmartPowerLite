package org.ladbury.userInterfacePkg;
        import javax.swing.*;
        import java.awt.*;

public class UiListBox extends JDialog
{

    private final DefaultListModel<String> model;
    private final JList<String> list;
    private final JPanel panel;
    private final JScrollPane pane;

    public UiListBox(String title)
    {
        model = new DefaultListModel<>();
        list = new JList<>(model);
        pane = new JScrollPane();
        panel = new JPanel();

        // Add the list to the scroll frame
        pane.setMaximumSize(new Dimension( 200,1000));
        pane.setAutoscrolls(true);
        pane.setLayout(new ScrollPaneLayout());
        pane.add(list);

        //Add the scroll frame to the panel
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setMaximumSize(new Dimension(250,300));
        panel.add(pane);

        //Set up this dialog
        setTitle(title);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.add(panel, BorderLayout.NORTH);
    }
    public void add(String s){
        model.addElement(s);}
}