package gui;

import logic.Logic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class GUI extends JDialog {
    private Logic logic;
    private JPanel contentPane;
    private JButton buttonRun;
    private JButton buttonOpenFile;
    private JButton buttonCancel;
    private JButton buttonFileOpen;
    private JLabel textFieldFile;
    private JPanel graph1JPanel;
    private JPanel graph2JPanel;


    public GUI() {
        logic = new Logic();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        contentPane.setPreferredSize(new Dimension(screenSize.width/2, screenSize.height/2));
        setContentPane(contentPane);

        setModal(true);
        getRootPane().setDefaultButton(buttonRun);

        buttonRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onRun();
            }
        });
        buttonOpenFile.addActionListener(new ActionListener() {
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
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onRun() {
        if(logic.getFile().equals(""))
            System.out.println("No file");
        else{
            System.out.println("run");
            logic.execute("PLACEHOLDER");
        }
    }

    private void onFileOpen() {
        JFileChooser fc = new JFileChooser();
        fc.showOpenDialog(contentPane);
        File file = fc.getSelectedFile();
        if(file != null){
            logic.setFile(file.getAbsolutePath());
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
