package logic;

import gui.GUI;
import io.XMLReader;

public class Logic {
    private MemController memoryController;
    private XMLReader xmlReader;

    public Logic() {
        this.xmlReader = new XMLReader();
    }

    public MemController getMemoryController() {
        return memoryController;
    }

    public void createMemController(String file, GUI gui){
        this.memoryController = new MemController(xmlReader.readXMLFile(file).getOperationsList());
        this.memoryController.addGUIListener(gui);
    }

    public void executeOne() {
        if(!memoryController.executeNext()) System.out.println("No more instructions!");
    }

    public void executeAll() {
        while(true) {
            memoryController.executeNext();
        }
    }
}
