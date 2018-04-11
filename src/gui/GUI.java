package gui;

import logic.Logic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class GUI extends JDialog {
    private Logic logic;
    private JPanel contentPanel;
    private JButton executeAllButton;
    private JButton executeOneButton;
    private JButton readXMLButton;
    private JPanel buttons;
    private JTextField textFieldFileName;
    private JTextField textFieldClock;
    private JTextField textFieldInstruction;
    private JTextField textFieldAdress;
    private JTextField textFieldFrame;
    private JTextField textFieldOffset;
    private JPanel Data;
    private JTextField textFieldRAMWrites;
    private JTextField textFieldHDDWRites;
    private JTextField textFieldTotalWrites;
    private JList listPTE;
    private JList listRAM;
    private JTable tableRam;


    public GUI() {
        logic = new Logic();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        contentPanel.setPreferredSize(new Dimension(screenSize.width / 2, screenSize.height / 2));
        setContentPane(contentPanel);

        setModal(true);
        getRootPane().setDefaultButton(executeOneButton);

        executeOneButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onReadOne();
            }
        });
        executeAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onReadAll();
            }
        });

        readXMLButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onFileOpen();
            }
        });


        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }


    private void onReadOne() {
        if (logic.getMemoryController() != null) logic.executeOne();
    }

    public void onReadAll() {
        if (logic.getMemoryController() != null) logic.executeAll();
    }

    private void onFileOpen() {
        JFileChooser fc = new JFileChooser();
        fc.showOpenDialog(contentPanel);
        File file = fc.getSelectedFile();
        if (file != null) {
            logic.createMemController(file.getAbsolutePath(), this);
            this.textFieldFileName.setText(file.getAbsolutePath());
        }
    }


    public void updateFields() {
        this.textFieldClock.setText(logic.getMemoryController().getClock() +"");
        this.textFieldRAMWrites.setText(logic.getMemoryController().getToRAMWrites() + "");
        this.textFieldHDDWRites.setText(logic.getMemoryController().getToHDDWrites() + "");
        this.textFieldTotalWrites.setText(logic.getMemoryController().getTotalWrites() + "");

        this.textFieldInstruction.setText(logic.getMemoryController().getCurrentInstruction().getOperation() + " PID:" + logic.getMemoryController().getCurrentInstruction().getPid());
        this.textFieldAdress.setText(logic.getMemoryController().getCurrentInstruction().getAddress() + "");

        if (logic.getMemoryController().getSplittedAddress() != null)
            this.textFieldFrame.setText(logic.getMemoryController().getSplittedAddress()[0] + "");
        else this.textFieldFrame.setText("");

        if (logic.getMemoryController().getSplittedAddress() != null)
            this.textFieldOffset.setText(logic.getMemoryController().getSplittedAddress()[1] + "");
        else this.textFieldFrame.setText("");


        this.listPTE.setListData(logic.getMemoryController().getPageTableList().toArray() );
        this.listRAM.setListData(logic.getMemoryController().getRamEntryList().toArray() );
    }

    /*public void updateTable() {

        RAMEntry[] entries = logic.getMemoryController().getRamEntryList();
        Object[][] data = new Object[entries.length][3];
        String columnNames[] = {"PID","FrameNR","PageNR"};
        int i=0;

        for(RAMEntry entry : entries) {
            data[i][0] = entry.getPid();
            data[i][1] = entry.getFrameNumber();
            data[i][2] = entry.getPageNr();
            i++;
        }
        System.out.println("update");

        this.tableRam = new JTable(data, columnNames);
        pack();

    }*/

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); //Windows Look and feel
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        GUI dialog = new GUI();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }


    private void createUIComponents() {
    }
}
