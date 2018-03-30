package logic;

import dataentities.Operations;
import io.XMLReader;

import javax.swing.*;

public class Logic {

    private XMLReader io;
    private Operations operations;


    public Logic() {
        io = new XMLReader();
    }


    public void readFile(String file) {
        operations = io.readXMLFile(file);
    }


    public void executeOne(){

    }

    public void executeAll(){

    }
}
