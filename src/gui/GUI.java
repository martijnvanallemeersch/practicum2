package gui;

import logic.Logic;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
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
    private JTextField textFieldTotalWriteInstructions;
    private JList listRAM;
    private JTree treePageTableList;
    private JTextField textFieldTotalHDDAndRAMWrites;


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

        //set the tree to empty on startup
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("PageTables");
        this.treePageTableList.setModel(new DefaultTreeModel(root));
        DefaultTreeModel model = (DefaultTreeModel) treePageTableList.getModel();
        model.reload(root);
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
        //Set memory-controller data
        this.textFieldClock.setText(logic.getMemoryController().getClock() + "");
        this.textFieldRAMWrites.setText(logic.getMemoryController().getToRAMWrites() + "");
        this.textFieldHDDWRites.setText(logic.getMemoryController().getToHDDWrites() + "");
        this.textFieldTotalHDDAndRAMWrites.setText(logic.getMemoryController().getToHDDWrites() + logic.getMemoryController().getToRAMWrites() + "");
        this.textFieldTotalWriteInstructions.setText(logic.getMemoryController().getTotalWriteInstructions() + "");

        //Set current instruction data
        this.textFieldInstruction.setText(logic.getMemoryController().getCurrentInstruction().getOperation() + " PID:" + logic.getMemoryController().getCurrentInstruction().getPid());
        this.textFieldAdress.setText(logic.getMemoryController().getCurrentInstruction().getAddress() + "");

        if (logic.getMemoryController().getSplittedAddress() != null &&
                (logic.getMemoryController().getCurrentInstruction().getOperation().equals("Write")||
                        logic.getMemoryController().getCurrentInstruction().getOperation().equals("Read"))) {
            this.textFieldFrame.setText(logic.getMemoryController().getSplittedAddress()[0] + "");
            this.textFieldOffset.setText(logic.getMemoryController().getSplittedAddress()[1] + "");
        }
        else {
            this.textFieldFrame.setText("N/A");
            this.textFieldOffset.setText("N/A");

        }


        //Create a tree from the PTEList
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("PageTables");
        logic.getMemoryController().getPageTableList().forEach(pt -> {
            DefaultMutableTreeNode process = new DefaultMutableTreeNode("Procecss: " + pt.getPid());
            pt.pageTableEntryList().forEach(pte -> {
                DefaultMutableTreeNode pteNode = new DefaultMutableTreeNode(pte);
                process.add(pteNode);
            });
            root.add(process);
        });

        //Set the tree
        this.treePageTableList.setModel(new DefaultTreeModel(root));
        DefaultTreeModel model = (DefaultTreeModel) treePageTableList.getModel();
        model.reload(root);

        this.listRAM.setListData(logic.getMemoryController().getRamEntries());
        expandAllNodes(this.treePageTableList,0,treePageTableList.getRowCount());

    }


    private void expandAllNodes(JTree tree, int startingIndex, int rowCount){
        for(int i=startingIndex;i<rowCount;++i){
            tree.expandRow(i);
        }

        if(tree.getRowCount()!=rowCount){
            expandAllNodes(tree, rowCount, tree.getRowCount());
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
    }
}
