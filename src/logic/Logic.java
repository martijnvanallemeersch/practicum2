package logic;

import gui.GUI;
import io.XMLReader;

public class Logic {
    private MemControllerRW memoryController;
    private XMLReader xmlReader;

    public Logic() {
        this.xmlReader = new XMLReader();
    }

    public MemControllerRW getMemoryController() {
        return memoryController;
    }

    public void createMemController(String file, GUI gui){
        this.memoryController = new MemControllerRW(xmlReader.readXMLFile(file));
        this.memoryController.setGui(gui);
    }

    public void executeOne() {
        memoryController.executeOne();
    }

    public void executeAll() {
        while(memoryController.executeOne()) {
            }
    }
}
