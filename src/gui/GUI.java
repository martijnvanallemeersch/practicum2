package gui;

import logic.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class GUI extends JDialog {
    private logic.MemController logic;
    private JPanel contentPanel;
    private JButton executeAllButton;
    private JButton executeOneButton;
    private JButton readXMLButton;
    private JPanel buttons;
    private JTextField textFieldFileName;
    private JTextField textFieldTimer;
    private JTextField textFieldInstruction;
    private JTextField textFieldAdress;
    private JTextField textFieldFrame;
    private JTextField textFieldOffset;
    private JPanel Data;
    private JTextField textFieldRAMWrites;
    private JTextField textFieldHDDWRites;


    public GUI() {
        logic = new logic.MemController();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        contentPanel.setPreferredSize(new Dimension(screenSize.width/2, screenSize.height/2));
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
        logic.fetchNext();
        logic.executeNext();
        this.textFieldInstruction.setText(logic.getCurrentInstruction().toString());
        this.textFieldFrame.setText(logic.getCurrentInstruction().toString());
        this.textFieldRAMWrites.setText(logic.getToRAMWrites()+"");
        this.textFieldHDDWRites.setText(logic.getToHDDWrites()+"");

    }

    public void onReadAll() {logic.executeNext();
    }

    private void onFileOpen() {
        JFileChooser fc = new JFileChooser();
        fc.showOpenDialog(contentPanel);
        File file = fc.getSelectedFile();
        if(file != null){
            logic.readFile(file.getAbsolutePath());
            this.textFieldFileName.setText( file.getAbsolutePath());
        }


    }

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
        // TODO: place custom component creation code here
    }
}
