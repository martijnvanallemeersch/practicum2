package logic;

import dataentities.Operations;
import io.XMLReader;

import javax.swing.*;

public class Logic {

    private XMLReader io;
    private String file;


    public Logic() {
        io = new XMLReader();

    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }


    public void execute(String type)
    {
        Operations operations = io.readXMLFile(this.file);

    }
}
