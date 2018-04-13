package logic;


import dataentities.*;
import gui.GUI;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MemControllerRW {

    private List<Instruction> instructionList;
    private RAMEntry[] ramEntries; // complete RAM overview
    private List<PageTable> pageTableList; // PageTable for each process
    private Instruction currentInstruction;
    private int clock;
    private List<PageTableEntry> pteInRam;

    private int processAmountInRAM;
    int[] splittedAddress;
    private GUI gui;


    // GETTERS and SETTERS
    public void setGui(GUI gui) {
        this.gui = gui;
    }

    //CONSTRUCTOR
    public MemControllerRW(List<Instruction> instructionList) {
        this.instructionList = instructionList;
        this.pageTableList = new LinkedList<>();
        this.ramEntries = new RAMEntry[SystemVariables.RAMFRAMES];
        this.pteInRam = new LinkedList();

        this.clock = 0;
        this.processAmountInRAM= 0;
    }

    public boolean executeOne() {
        boolean canExecute = true;

        if (fetchNextInstruction()) {
            decodeCurrentInstruction();
             gui.updateFields();

        } else canExecute = false;
        clock++;
        return canExecute;
    }

    private boolean fetchNextInstruction() {
        boolean canFetchNext = true;
        if (instructionList.size() != 0) currentInstruction = instructionList.remove(0);
        else canFetchNext = false;
        return canFetchNext;
    }


    private void decodeCurrentInstruction() {
        switch (currentInstruction.getOperation()) {
            case "Start":
                PageTable pt = new PageTable(currentInstruction.getPid());
                this.pageTableList.add(pt);
                assignFramesInRam(pt);
                break;
            case "Read":
                break;
            case "Write":
                //TODO: the suff that a write does
                break;
            case "Terminate":
                //TODO redistribute all the frames
                break;
        }
    }

    private void assignFramesInRam(PageTable pt) {
        switch (processAmountInRAM) {
            case 0:
                for (int i = 0; i < SystemVariables.RAMFRAMES; i++) {
                    //Set the ram entries
                    RAMEntry entry = new RAMEntry(i, currentInstruction.getPid(),pt.getpageTableEntry(i).getPageNumber());
                    ramEntries[i] = entry;

                    //set in PT the PTE's in RAM
                    pt.moveEntryToRAM(i, i,this.clock);
                    pteInRam.add(pt.getpageTableEntry(i));
                }
                processAmountInRAM++;
                break;
            case 1:
                //Search the 6 frames with the lowest LRU and swap them out
                List<PageTableEntry> leastused = pteInRam.stream().sorted(Comparator.comparingInt(PageTableEntry::getLastAccess)).collect(Collectors.toList()).subList(0,6);

                pteInRam.remove(leastused);
                for (int i = 0; i < leastused.size(); i++) {
                    int frameNumber = leastused.get(i).getFrameNumber();

                    ramEntries[frameNumber] = new RAMEntry(frameNumber,pt.getPid(),pt.pageTableEntryList().get(i).getPageNumber());
                    pteInRam.add(pt.pageTableEntryList().get(i));

                    //set the pte to the ram
                    pt.moveEntryToRAM(i,frameNumber,clock);
                    pageTableList.get(0).moveEntryToHDD(leastused.get(i).getPageNumber());
                }
                processAmountInRAM++;
                break;
            case 2:
                List<PageTableEntry> leastUsed = new LinkedList<>();
                for(PageTable pageTable : pageTableList){
                    List temp = new LinkedList();
                    if(pageTable.pageTableEntryList().stream().filter(pte-> pte.isPresent()).collect(Collectors.toList()).size() == 6)
                        temp = pageTable.pageTableEntryList().stream().filter(pte-> pte.isPresent()).sorted(Comparator.comparingInt(PageTableEntry::getLastAccess)).collect(Collectors.toList()).subList(0,2);
                    leastUsed.addAll(temp);
                }

                pteInRam.remove(leastUsed);
                for (int i = 0; i < leastUsed.size(); i++) {
                    int pageNumber =  leastUsed.get(i).getPageNumber();
                    int frameNumber = leastUsed.get(i).getFrameNumber();
                    PageTable currentPT = pageTableList.get(i/2);
                    currentPT.moveEntryToHDD(pageNumber);
                    pt.moveEntryToRAM(i,pageNumber, clock);
                    pteInRam.add(pt.getpageTableEntry(i));
                    ramEntries[frameNumber] = new RAMEntry(frameNumber,pt.getPid(),pt.pageTableEntryList().get(i).getPageNumber());
                }

                break;
            case 3:
                List<PageTableEntry> leastUsed1 = new LinkedList<>();
                for(PageTable pageTable : pageTableList){
                    PageTableEntry temp = null;
                    if(pageTable.pageTableEntryList().stream().filter(pte-> pte.isPresent()).collect(Collectors.toList()).size() == 4)
                        temp = pageTable.pageTableEntryList().stream().filter(pte-> pte.isPresent()).sorted(Comparator.comparingInt(PageTableEntry::getLastAccess)).collect(Collectors.toList()).get(0);
                    leastUsed1.add(temp);
                }

                pteInRam.remove(leastUsed1);
                for (int i = 0; i < leastUsed1.size(); i++) {
                    int pageNumber =  leastUsed1.get(i).getPageNumber();
                    int frameNumber = leastUsed1.get(i).getFrameNumber();
                    PageTable currentPT = pageTableList.get(i);
                    currentPT.moveEntryToHDD(pageNumber);
                    pt.moveEntryToRAM(i,pageNumber, clock);
                    pteInRam.add(pt.getpageTableEntry(i));
                    ramEntries[frameNumber] = new RAMEntry(frameNumber,pt.getPid(),pt.pageTableEntryList().get(i).getPageNumber());
                }
                //TODO: Same as above but for more processes.
                break;
        }
        processAmountInRAM++;
    }

    private void test() {

    }


    public int getClock() {
        return clock;
    }

    public Instruction getCurrentInstruction() {
        return currentInstruction;
    }

    public List<PageTable> getPageTableList() {
        return pageTableList;
    }

    public RAMEntry[] getRamEntries() {
        return ramEntries;
    }

}
