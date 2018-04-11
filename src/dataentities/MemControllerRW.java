package dataentities;



import gui.GUI;
import javafx.collections.FXCollections;

import java.util.List;
import java.util.Map;

public class MemControllerRW {

    private List<Instruction> instructionList;
    private RAMEntry[] ramEntrys;
    private List<Process> processList; // Process contains the process and the PTE's for each process
    private Instruction currentInstruction;

    private int jobsInRam;

    private GUI gui;


    private int clock;

    public MemControllerRW(List<Instruction> instructionList) {
        this.instructionList = instructionList;
        this.ramEntrys = new RAMEntry[12];
    }


    public void executeAll() {
        while(executeOne()){}
    }

    public boolean executeOne() {

        boolean canExecute = true;
        if(instructionList.size() != 0)
        {
            this.currentInstruction = instructionList.remove(0);

            Process process;
            PageTableEntry pte;

            switch (currentInstruction.getOperation()) {
                case "Start":
                    process = new Process(currentInstruction.getPid());
                    this.processList.add(process);
                    LoadJobsInRam(process); //re-distribute allocated frames
                    jobsInRam++;
                    break;
                case "Read":
                    // Nothing needs to be done here
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

        } else canExecute = false;

        return canExecute;
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

        System.out.println(process.getPtEntries().toString());
        for (int i = 0; i < 12; i++) {
            //set all the ramEntries to this process
            RAMEntry re = ramEntrys[i];
            re.setPid(currentInstruction.getPid());
            re.setPageNr(i);
            ramEntrys[i] = re;

            PageTableEntry pte = process.getPtEntry(i); //pagetable of current Instruction
            pte.setFrameNumber(i);
            pte.setPresent(true);
            pte.setLastAccess(clock);
            process.setPtEntry(i,pte);
        }

        process.setnToRam(12);
    }

    private void oneProcessInRAM(Process process) {
        processList.forEach((proc) -> {
            if (proc.getPid() != currentInstruction.getPid()) {
                for (int i = 0; i < 6; i++) {
                    swapLRUFrameOfProcess(proc, currentInstruction.getPid(), i);
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


}
