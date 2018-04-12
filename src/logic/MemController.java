package logic;

import dataentities.*;
import dataentities.Process;
import gui.GUI;
import javafx.collections.FXCollections;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MemController {

    private List<RAMEntry> ramEntryList;
    //    private Map<Process, List<PageTableEntry>> pageTableList;
    private List<PageTableEntry> pageTableList;
    private List<Instruction> remainingInstructionList;

    private int clock = -1;
    private Map<Integer, Process> jobMapInRAM;
    private Map<Integer, Process> jobMapFinished;

    int[] splittedAddress;

    private Instruction currentInstruction;
    private int jobsInRam;
    private int toHDDWrites;
    private int toRAMWrites;
    private int totalWriteInstructions;

    private GUI gui = null;

    public MemController(List<Instruction> remainingInstructionList) {
        initRAM(SystemVariables.RAMFRAMES);
        this.remainingInstructionList = remainingInstructionList;

        jobMapInRAM = new HashMap<>();
        jobMapFinished = new HashMap<>();
    }

    /**
     * Initialize physical RAM
     *
     * @param frameAmount Amount of frames in RAM
     */
    private void initRAM(int frameAmount) {
        this.ramEntryList = new LinkedList<>();
        for (int i = 0; i < 12; i++) ramEntryList.add(new RAMEntry(i));
        System.out.println(ramEntryList.size());

    }


    public boolean executeNext() {
        //get next Instruction
        if (!fetchNextInstruction()) return false;
        Process process;
        PageTableEntry pte;

        switch (currentInstruction.getOperation()) {
            case "Start":
                process = new Process(currentInstruction.getPid());
                pageTableList = process.getPtEntries();

                jobMapInRAM.put(currentInstruction.getPid(), process); //add new process to jobmap
                LoadJobsInRam(process); //re-distribute allocated frames
                jobsInRam++;
                break;
            case "Read":
                // Nothing needs to be done here?
                break;
            case "Write":
                process = jobMapInRAM.get(currentInstruction.getPid()); //getting process process
                splittedAddress = calculateAddress(currentInstruction.getAddress()); //calculating address for pagetable entry
                pte = process.getPtEntry(splittedAddress[0]); //getting pagetable entry of that process
                if (!pte.isPresent()) {
                    //use LRU to remove frame from this process and replace with this frame
                    swapLRUFrameOfProcess(process, process.getPid(), pte.getPageNumber());
                    process.setnToRam(process.getnToRam() + 1);
                }
                pte.setLastAccess(clock);
                if (currentInstruction.getOperation().equals("Write")) {
                    pte.setModified(true);
                    totalWriteInstructions++;
                }
                pageTableList = FXCollections.observableArrayList(process.getPtEntries());
                break;
            case "Terminate":
                process = jobMapInRAM.get(currentInstruction.getPid());
                jobsInRam--;
                //migrate Process from MapInRAM to Finished
                jobMapInRAM.remove(currentInstruction.getPid());
                jobMapFinished.put(currentInstruction.getPid(), process);
                //redistribute frames, replace frames with the MRU of each process
                redistributeFrames(process);
                break;

        }
        System.out.println("\n\n");
        jobMapInRAM.forEach((id, proc) -> {
            System.out.println("\nid: " + id);
            proc.getPtEntries().forEach(p -> System.out.println(p.toString()));
        });

        if (gui != null) gui.updateFields();
        return true;
    }

    private boolean fetchNextInstruction() {
        if (remainingInstructionList.size() == 0) return false;
        this.currentInstruction = this.remainingInstructionList.remove(0);
        Process process;
        if (jobMapInRAM.containsKey(currentInstruction.getPid())) {
            process = jobMapInRAM.get(currentInstruction.getPid());
            pageTableList = process.getPtEntries();
        } else pageTableList = null;

        clock++;
        return true;
    }

    private void LoadJobsInRam(Process process) {
        switch (jobsInRam) {
            case 0:
                noProcessInRAM(process);
                break;
            case 1:
                oneProcessInRAM(process);
                break;
            case 2:
                twoProcessesInRAM(process);
                break;
            case 3:
                threeProcessesInRAM(process);
                break;
        }
    }


    private void noProcessInRAM(Process process) {
        System.out.println(ramEntryList.toString());
        for (int i = 0; i < 12; i++) {
            RAMEntry re = ramEntryList.get(i);
            re.setPid(currentInstruction.getPid());
            re.setPageNr(i);
            ramEntryList.set(i, re);
            PageTableEntry pte = pageTableList.get(i); //pagetable of current Instruction
            pte.setFrameNumber(i);
            pte.setPresent(true);
            pte.setLastAccess(clock);
            pageTableList.set(i, pte);
        }

        process.setnToRam(12);
    }

    private void oneProcessInRAM(Process process) {
        jobMapInRAM.forEach((pid, processInRAM) -> {
            if (pid != currentInstruction.getPid()) {
                for (int i = 0; i < 6; i++) {
                    swapLRUFrameOfProcess(processInRAM, currentInstruction.getPid(), i);
                }
            }
        });
        process.setnToRam(6);
    }

    private void twoProcessesInRAM(Process process) {
        int pageNr = 0;
        //replace 2 frames of each of the 2 jobs (so all 3 jobs will have 4 frames in the end)
        for (Map.Entry<Integer, Process> entry : jobMapInRAM.entrySet()) {
            if (entry.getKey() != currentInstruction.getPid()) {
                for (int framesToReplace = 2; framesToReplace > 0; framesToReplace--) {
                    swapLRUFrameOfProcess(entry.getValue(), currentInstruction.getPid(), pageNr);
                    pageNr++;
                    if (pageNr > 4) break;
                }
            }
        }
        process.setnToRam(4);
    }

    private void threeProcessesInRAM(Process process) {
        int pageNr = 0;
        //replace 1 frame of each of the 3 jobs (so all 4 jobs will have 3 frames in the end)
        for (Map.Entry<Integer, Process> entry : jobMapInRAM.entrySet()) {
            if (entry.getKey() != currentInstruction.getPid()) {
                for (int framesToReplace = 1; framesToReplace > 0; framesToReplace--) {
                    swapLRUFrameOfProcess(entry.getValue(), currentInstruction.getPid(), pageNr);
                    pageNr++;
                    if (pageNr > 3) break;
                }
            }
        }
        process.setnToRam(3);
    }

    private void swapLRUFrameOfProcess(Process process, int pid, int pageNumber) {
        List<PageTableEntry> pageTable = process.getPtEntries();

        PageTableEntry pteToModify = pageTable.stream().sorted(Comparator.comparingInt(pte -> pte.getLastAccess())).collect(Collectors.toList()).get(pageTable.size() - 1);

        pteToModify.setPresent(false);
        //if modified (write Instruction executed on this page), write back to disk
        if (pteToModify.isModified()) {
            process.incrementToHDD(); //when swapping
            pteToModify.setModified(false);
        }
        //3) update RAM-entry with the newly available frame
        int frameNumber = pteToModify.getFrameNumber();
        ramEntryList.get(frameNumber).setPid(pid);
        ramEntryList.get(frameNumber).setPageNr(pageNumber);

        //4) update pageTable of process "pid"
        PageTableEntry pteToAdd = jobMapInRAM.get(pid).getPtEntry(pageNumber);
        pteToAdd.setFrameNumber(frameNumber);
        pteToAdd.setLastAccess(clock);
        pteToAdd.setPresent(true);
        pageTableList.set(pageNumber, pteToAdd);
    }


    private void redistributeFrames(Process terminatingProcess) {
        //only 4 different processes can be in ram
        //update pagetable list
        for (PageTableEntry pte : pageTableList) {
            if (pte.isModified() && pte.isPresent()) terminatingProcess.setnToHDD(terminatingProcess.getnToHDD() + 1);
            pte.setPresent(false);
        }

        toHDDWrites = toHDDWrites + terminatingProcess.getnToHDD();
        toRAMWrites = toRAMWrites + terminatingProcess.getnToRam();//to get a global image of the writes from and to ram (total)
        switch (jobsInRam) {
            case 0:
                //was alone in RAM  -> free all frames
                for (RAMEntry re : ramEntryList) {
                    re.setPageNr(-1);
                    re.setPid(-1);
                }
                break;
            case 1: //still 1 process  in RAM => give 6 extra frames, preference to most recently used
                jobMapInRAM.forEach((pid, processInRAM) -> {
                    for (int i = 0; i < 6; i++) {
                        swapMRU(processInRAM);
                    }
                    processInRAM.setnToRam(processInRAM.getnToRam() + 6);  //6 nieuwe frames in ram geladen per overige job
                });
                break;
            case 2://still 2 processes in RAM => 	give each 2 extra frames
                jobMapInRAM.forEach((pid, processInRAM) -> {
                    for (int i = 0; i < 2; i++) {
                        swapMRU(processInRAM);
                    }
                    processInRAM.setnToRam(processInRAM.getnToRam() + 2); //2 nieuwe frames in ram geladen per overige job
                });
                break;
            case 3://still 3 processes in RAM => 	give each 1 extra frame
                jobMapInRAM.forEach((pid, processInRAM) -> {
                    swapMRU(processInRAM);
                    processInRAM.setnToRam(processInRAM.getnToRam() + 1); //1 extra frame in ram geladen per overige job
                });
                break;
        }
    }

    private void swapMRU(Process remainingProcess) {
        //remainingProcess is a job that still exists in RAM, the current job is the terminating one
        //1) search for frame from terminating job
        RAMEntry oldRe = null;
        for (RAMEntry re : ramEntryList) {
            if (re.getPid() == currentInstruction.getPid()) {
                oldRe = re;
                break;
            }
        }
        //2) search for most recently used frame of remainingProcess that is not present in RAM
        int lastUsed = -1;
        PageTableEntry mruPte = null;
        for (PageTableEntry pte : remainingProcess.getPtEntries()) {
            if (!pte.isPresent() && pte.getLastAccess() > lastUsed) {
                lastUsed = pte.getLastAccess();
                mruPte = pte;
            }
        }

        //3) replace old frame with this MRU frame
        mruPte.setLastAccess(clock);
        mruPte.setPresent(true);
        mruPte.setFrameNumber(oldRe.getFrameNumber());
        ramEntryList.get(oldRe.getFrameNumber()).setPid(remainingProcess.getPid());
        ramEntryList.get(oldRe.getFrameNumber()).setPageNr(mruPte.getPageNumber());

    }

    public int[] calculateAddress(int address) {
        //split the bits: last 12 =  offset within page
        String paddedBinary = String.format("%16s", Integer.toBinaryString(address)).replace(' ', '0');

        String pageNrBin = paddedBinary.substring(0, paddedBinary.length() - 12);
        String offsetBin = paddedBinary.substring(paddedBinary.length() - 12);
        //convert back to ints
        int pageNr = Integer.parseInt(pageNrBin, 2);
        int offset = Integer.parseInt(offsetBin, 2);
        int[] splitted = new int[]{pageNr, offset};
        return splitted;
    }


    public void addGUIListener(GUI gui) {
        this.gui = gui;
    }

    public Instruction getCurrentInstruction() {
        return currentInstruction;
    }

    public int getToRAMWrites() {
        return toRAMWrites;
    }

    public int getToHDDWrites() {
        return toHDDWrites;
    }

    public int getTotalWrites() {
        return this.totalWriteInstructions;
    }

    public int[] getSplittedAddress() {
        return splittedAddress;
    }

    public int getClock() {
        return this.clock;
    }

    public List<RAMEntry> getRamEntryList() {
        return this.ramEntryList;
    }

    public Map<Integer, Process> getJobMapInRAM() {
        return jobMapInRAM;
    }

    public List<PageTableEntry> getPageTableList() {
        return pageTableList;
    }
}
