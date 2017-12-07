package org.ladbury.userInterfacePkg;
        import javax.swing.*;
        import java.awt.*;
        import java.util.ArrayList;

public class UiListBox extends JFrame {
    private final ArrayList<String> data;
    private final JList<Object> list;

    public UiListBox(String title) throws HeadlessException {
        //super();

        Dimension size = new Dimension(300, 250);
        data = new ArrayList<>();
        list = new JList<>(data.toArray());

        this.setTitle(title);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setSize(size);
        this.setPreferredSize(size);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());

        JScrollPane pane = new JScrollPane(list);
        this.add(pane, BorderLayout.CENTER);

        this.setVisible(true);
    }

    public void add(String data) {
        this.data.add(data);
        list.setListData(this.data.toArray());
        this.repaint();
    }

}
