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

    private int toRAMWrites;
    private int toHDDWrites;

    private int totalWriteInstruction;

    private int processAmountInRAM;
    int[] splittedAddress;
    private GUI gui;


    /**
     * A nonstandard constructor that accepts the list of instructions
     * @param instructionList the list of instructions
     */
    public MemControllerRW(List<Instruction> instructionList) {
        this.instructionList = instructionList;
        this.pageTableList = new LinkedList<>();
        this.ramEntries = new RAMEntry[SystemVariables.RAMFRAMES];
        this.pteInRam = new LinkedList();

        this.clock = 1;
        this.processAmountInRAM = 0;
        this.totalWriteInstruction = 0;
    }

    /**
     * Executes one instruction if there are in the list
     * @return boolean that returns false if there are no instructions left
     */
    public boolean executeOne() {
        boolean canExecute = true;

        if (fetchNextInstruction()) {
            decodeCurrentInstruction();
            gui.updateFields();

        } else canExecute = false;
        clock++;
        return canExecute;
    }

    /**
     * Fetches the next instruction that needs to be executed
     * @return boolean that returns false if there are no more instructions
     */
    private boolean fetchNextInstruction() {
        boolean canFetchNext = true;
        if (instructionList.size() != 0) currentInstruction = instructionList.remove(0);
        else canFetchNext = false;
        return canFetchNext;
    }

    /**
     * Decodes the current instruction according its operation
     */
    private void decodeCurrentInstruction() {
        PageTable pageTable = this.getPageTableList().stream().filter(cpt -> cpt.getPid() == currentInstruction.getPid()).findFirst().orElse(null);

        if(currentInstruction.getOperation().equals("Start")) {
            PageTable pt = new PageTable(currentInstruction.getPid());
            this.pageTableList.add(pt);
            assignFramesInRam(pt);
            processAmountInRAM++;
        }

        if(currentInstruction.getOperation().equals("Write") || currentInstruction.getOperation().equals("Read")) {
            splitAddress(currentInstruction.getAddress());
            if (!pageTable.getpageTableEntry(splittedAddress[0]).isPresent()) {
                PageTableEntry leastUsed = pageTable.pageTableEntryList().stream().filter(pte-> pte.isPresent()).sorted(Comparator.comparingInt(PageTableEntry::getLastAccess)).findFirst().get();
                pageTable.moveEntryToRAM(splittedAddress[0], leastUsed.getFrameNumber(),clock);
                int frameNumber = leastUsed.getFrameNumber();
                moveLeastUsedToHDD(pageTable, leastUsed );
                moveNewToRAM(pageTable,pageTable.getpageTableEntry(splittedAddress[0]), frameNumber);
            }

            if(currentInstruction.getOperation().equals("Write")){
                totalWriteInstruction++;
                pageTable.getpageTableEntry(splittedAddress[0]).setModified(true);
            }
            pageTable.getpageTableEntry(splittedAddress[0]).setLastAccess(clock);
        }

        if(currentInstruction.getOperation().equals("Terminate")) {
            processAmountInRAM--;
            redistributeFrames();
        }

    }

    /**
     * Creates a new page table and assigns frames to the new process depending on the amount of processes in the ram
     * @param pt the new, unchanged, pageTable
     */
    private void assignFramesInRam(PageTable pt) {
        switch (processAmountInRAM) {
            case 0:
                for (int i = 0; i < SystemVariables.RAMFRAMES; i++) {
                    toRAMWrites++;

                    //Set the ram entries
                    RAMEntry entry = new RAMEntry(i, currentInstruction.getPid(), pt.getpageTableEntry(i).getPageNumber());
                    ramEntries[i] = entry;

                    //set in PT the PTE's in RAM
                    pt.moveEntryToRAM(i, i, this.clock);
                    pteInRam.add(pt.getpageTableEntry(i));
                }
                break;
            case 1:
                //Search the 6 frames with the lowest LRU and swap them out
                List<PageTableEntry> leastUsed1 = pteInRam.stream().sorted(Comparator.comparingInt(PageTableEntry::getLastAccess)).collect(Collectors.toList()).subList(0, 6);

                pteInRam.remove(leastUsed1);
                for (int i = 0; i < leastUsed1.size(); i++) {
                    int frameNumberOld = leastUsed1.get(i).getFrameNumber();
                    moveLeastUsedToHDD(pageTableList.get(i/6), leastUsed1.get(i));
                    moveNewToRAM(pt,pt.getpageTableEntry(i), frameNumberOld);
                }
                break;
            case 2:
                List<PageTableEntry> leastUsed2 = new LinkedList<>();
                for (PageTable pageTable : pageTableList) {
                    List temp = new LinkedList();
                    if (pageTable.pageTableEntryList().stream().filter(pte -> pte.isPresent()).collect(Collectors.toList()).size() == 6)
                        temp = pageTable.pageTableEntryList().stream().filter(pte -> pte.isPresent()).sorted(Comparator.comparingInt(PageTableEntry::getLastAccess)).collect(Collectors.toList()).subList(0, 2);
                    leastUsed2.addAll(temp);
                }

                pteInRam.remove(leastUsed2);
                for (int i = 0; i < leastUsed2.size(); i++) {
                    int frameNumberOld = leastUsed2.get(i).getFrameNumber();
                    moveLeastUsedToHDD(pageTableList.get(i/2), leastUsed2.get(i));
                    moveNewToRAM(pt,pt.getpageTableEntry(i), frameNumberOld);
                }

                break;
            case 3:
                List<PageTableEntry> leastUsed3 = new LinkedList<>();
                for (PageTable pageTable : pageTableList) {
                    PageTableEntry temp = null;
                    if (pageTable.pageTableEntryList().stream().filter(pte -> pte.isPresent()).collect(Collectors.toList()).size() == 4)
                        temp = pageTable.pageTableEntryList().stream().filter(pte -> pte.isPresent()).sorted(Comparator.comparingInt(PageTableEntry::getLastAccess)).collect(Collectors.toList()).get(0);
                    if (temp != null) leastUsed3.add(temp);
                }

                pteInRam.remove(leastUsed3);
                for (int i = 0; i < leastUsed3.size(); i++) {
                    int frameNumberOld = leastUsed3.get(i).getFrameNumber();
                    moveLeastUsedToHDD(pageTableList.get(i), leastUsed3.get(i));
                    moveNewToRAM(pt,pt.getpageTableEntry(i), frameNumberOld);
                }
                break;
        }
    }

    /**
     *
     * @param pt Page table of leased used pte
     * @param pte the pte of the leased used
     */
    private void moveLeastUsedToHDD(PageTable pt, PageTableEntry pte) {
        if(pte.isModified()){
            toHDDWrites++;
            pte.setModified(false);
        }

        pt.moveEntryToHDD(pte.getPageNumber());
        pteInRam.remove(pte);
    }

    /**
     *
     * @param pt New pt
     * @param pte new pte from pt
     * @param frameNumber framenumber of old pte
     */
    private void moveNewToRAM(PageTable pt, PageTableEntry pte, int frameNumber) {
        toRAMWrites++;
        pteInRam.add(pte);
        pt.moveEntryToRAM(pte.getPageNumber(),frameNumber,clock);
        ramEntries[frameNumber] = new RAMEntry(frameNumber, pt.getPid(), pte.getPageNumber());
    }

    public void getLEastUSed() {

    }

    /**
     * redistributes the frames among the resting process in RAM when a process is terminated
     */
    private void redistributeFrames() {
        int processIdToRemove = currentInstruction.getPid();
        PageTable pageTableToRemove = pageTableList.stream().filter(pt -> pt.getPid() == processIdToRemove).findFirst().get();
        pageTableList.remove(pageTableToRemove);
        List<PageTableEntry> pteToRemove = pageTableToRemove.pageTableEntryList().stream().filter(pte -> pte.isPresent()).collect(Collectors.toList());


        if (processAmountInRAM == 0) {
            for (int i = 0; i < ramEntries.length; i++) {
                ramEntries[i] = new RAMEntry(i);
            }
        } else {
            for (int i = 0; i < pteToRemove.size(); i++) {
                toRAMWrites++;
                if(pteToRemove.get(i).isModified()){
                    toHDDWrites++;
                    pteToRemove.get(i).setModified(false);
                }

                int pteToRemoveFrameNumber = pteToRemove.get(i).getFrameNumber();

                int modifier = i;
                if (processAmountInRAM == 3) {
                    modifier = i;
                } else if (processAmountInRAM == 2) {
                    modifier = i / 2;
                } else if (processAmountInRAM == 1) {
                    modifier = 0;
                }

                PageTable ptToReplace = pageTableList.get(modifier);
                PageTableEntry pteMRUNotInRam = ptToReplace.pageTableEntryList().stream().filter(pte -> !pte.isPresent()).sorted((pte1, pte2) -> Integer.compare(pte2.getLastAccess(), pte1.getLastAccess())).findFirst().get();

                ptToReplace.moveEntryToRAM(pteMRUNotInRam.getPageNumber(), pteToRemove.get(i).getFrameNumber(), clock);
                ramEntries[pteToRemoveFrameNumber] = new RAMEntry(pteToRemoveFrameNumber, ptToReplace.getPid(), pteMRUNotInRam.getPageNumber());

                pteInRam.remove(pteToRemove.get(i));
                pteInRam.add(pteMRUNotInRam);

            }
        }

        //Move the terminated entries back to HDD
        for (int i = 0; i < pageTableToRemove.pageTableEntryList().size(); i++) {
            pageTableToRemove.moveEntryToHDD(i);
        }
    }

    /**
     * Splits the addres into a page number and an offset and sets the global parameter
     * @param address the address that needs to be splitted
     */
    private void splitAddress(int address) {
        //split the bits: last 12 =  offset within page
        String paddedBinary = String.format("%16s", Integer.toBinaryString(address)).replace(' ', '0');

        String pageNrBin = paddedBinary.substring(0, paddedBinary.length() - 12);
        String offsetBin = paddedBinary.substring(paddedBinary.length() - 12);
        //convert back to ints
        int pageNr = Integer.parseInt(pageNrBin, 2);
        int offset = Integer.parseInt(offsetBin, 2);
        this.splittedAddress = new int[]{pageNr, offset};
    }

    //SETTERS
    public void setGui(GUI gui) {
        this.gui = gui;
    }

    //Getters
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

    public int getToRAMWrites() {
        return toRAMWrites;
    }

    public int getToHDDWrites() {
        return toHDDWrites;
    }

    public int[] getSplittedAddress() {
        return splittedAddress;
    }

    public int getTotalWriteInstructions() { return this.totalWriteInstruction;}
}
