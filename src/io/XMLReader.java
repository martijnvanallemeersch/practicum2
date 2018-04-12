package io;

import dataentities.Instruction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class XMLReader {

    public List<Instruction> readXMLFile(String path) {
        List<Instruction> instructionList= new LinkedList();
        System.out.println(path);
        try {

            File fXmlFile = new File(path);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("instruction");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    int pid = Integer.parseInt(getTagValue("processID", eElement));
                    String operation = getTagValue("operation", eElement);
                    int address = Integer.parseInt(getTagValue("address", eElement));
                    Instruction tempInstruction = new Instruction(pid, operation, address);
                    instructionList.add(tempInstruction);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return instructionList;
    }


    private static String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodeList.item(0);
        return node.getNodeValue();
    }
}
