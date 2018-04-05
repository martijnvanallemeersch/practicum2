package logic;

import dataentities.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MemController {
    private List<PageTableEntry> pageTableList;

    private RAMEntry[] physicalRam;
    private List<Operation> remainingOperationList;

    private int clock = -1;
    private Map<Integer, Job> jobMapInRAM;
    private Map<Integer, Job> jobMapFinished;

    private Operation currentInstruction;
    private int jobsInRam;
    private int toHDDWrites;
    private int toRAMWrites;
    private int totalWriteInstructions = toHDDWrites + toRAMWrites;

    public MemController(List<Operation> remainingOperationList) {
        initPhysicalRAM(SystemVariables.RAMFRAMES);
        this.remainingOperationList = remainingOperationList;

        jobMapInRAM = new HashMap<>();
        jobMapFinished = new HashMap<>();
    }

    /**
     * Initialize physical RAM
     *
     * @param frameAmount Amount of frames in RAM
     */
    private void initPhysicalRAM(int frameAmount) {
        this.physicalRam = new RAMEntry[frameAmount];
        for (int i = 0; i < frameAmount; i++) {
            this.physicalRam[i] = new RAMEntry(i);
        }
    }


    public void executeAll() {
        while (remainingOperationList.size() != 0) {
            this.executeNext();
        }
    }

    public void executeNext() {
        //get next instruction
        fetchNextInstruction();
        Job job;
        switch (currentInstruction.getOperation()) {
            case "Start":
                job = new Job(currentInstruction.getPid());
                pageTableList = job.getPtEntries();

                jobMapInRAM.put(currentInstruction.getPid(), job); //add new job to jobmap
                LoadJobsInRam(job); //re-distribute allocated frames
                jobsInRam++;
                break;
            case "Read":
                // Nothing needs to be done here?
                break;
            case "Write":
                break;
            case "Terminate":
                job = jobMapInRAM.get(currentInstruction.getPid());
                jobsInRam--;
                //migrate Job from MapInRAM to Finished
                jobMapInRAM.remove(currentInstruction.getPid());
                jobMapFinished.put(currentInstruction.getPid(), job);
                //redistribute frames, replace frames with the MRU of each job
                redistributeFrames(job);
                break;

        }
    }

    private void fetchNextInstruction() {
        this.currentInstruction = this.remainingOperationList.remove(0);
        Job job;
        if (jobMapInRAM.containsKey(currentInstruction.getPid())) {
            job = jobMapInRAM.get(currentInstruction.getPid());
            pageTableList = job.getPtEntries();
        } else pageTableList = null;

        clock++;
    }

    private void LoadJobsInRam(Job job) {
        switch (jobsInRam) {
            case 0:
                noJobsInRAM(job);
                break;
            case 1:
                oneJobsInRAM(job);
                break;
            case 2:
                twoJobsInRAM(job);
                break;
            case 3:
                threeJobsInRAM(job);
                break;
        }
    }


    private void noJobsInRAM(Job job) {

        for (int pageNr = 0; pageNr < 12; pageNr++) {
            RAMEntry re = physicalRam[pageNr];
            re.setPid(currentInstruction.getPid());
            re.setPageNr(pageNr);
            physicalRam[pageNr] = re;

            PageTableEntry pte = pageTableList.get(pageNr); //pagetable of current instruction
            pte.setFrameNumber(pageNr);
            pte.setPresent(true);
            pte.setLastAccess(clock);
            pageTableList.set(pageNr, pte);
        }

        job.setnToRam(12);
    }

    private void oneJobsInRAM(Job job) {

        jobMapInRAM.forEach((pid, jobInRAM) -> {
            if (pid != currentInstruction.getPid()) {
                for (int pageNr = 0; pageNr < 6; pageNr++) {
                    swapLRUFrameFromJob(jobInRAM, currentInstruction.getPid(), pageNr);
                }
            }
        });
        job.setnToRam(6);
    }

    private void twoJobsInRAM(Job job) {
        int pageNr = 0;
        //replace 2 frames of each of the 2 jobs (so all 3 jobs will have 4 frames in the end)
        for (Map.Entry<Integer, Job> entry : jobMapInRAM.entrySet()) {
            if (entry.getKey() != currentInstruction.getPid()) {
                for (int framesToReplace = 2; framesToReplace > 0; framesToReplace--) {
                    swapLRUFrameFromJob(entry.getValue(), currentInstruction.getPid(), pageNr);
                    pageNr++;
                    if (pageNr > 4) break;
                }
            }
        }
        job.setnToRam(4);
    }

    private void threeJobsInRAM(Job job) {
        int pageNr = 0;

        //replace 1 frame of each of the 3 jobs (so all 4 jobs will have 3 frames in the end)
        for (Map.Entry<Integer, Job> entry : jobMapInRAM.entrySet()) {
            if (entry.getKey() != currentInstruction.getPid()) {
                for (int framesToReplace = 1; framesToReplace > 0; framesToReplace--) {
                    swapLRUFrameFromJob(entry.getValue(), currentInstruction.getPid(), pageNr);
                    pageNr++;
                    if (pageNr > 3) break;
                }
            }
        }
        job.setnToRam(3);


        job.setnToRam(3);
    }

    private void swapLRUFrameFromJob(Job job, int pid, int pageNumber) {
        List<PageTableEntry> pageTable = job.getPtEntries();

        PageTableEntry pteToModify = pageTable.stream().sorted(Comparator.comparingInt(pte -> pte.getLastAccess())).collect(Collectors.toList()).get(pageTable.size() - 1);

        pteToModify.setPresent(false);
        //if modified (write instruction executed on this page), write back to disk
        if (pteToModify.isModified()) {
            job.incrementToHDD(); //when swapping
            pteToModify.setModified(false);
        }
        //3) update RAM-entry with the newly available frame
        int frameNumber = pteToModify.getFrameNumber();
        physicalRam[frameNumber].setPid(pid);
        physicalRam[frameNumber].setPageNr(pageNumber);

        //4) update pageTable of process "pid"
        PageTableEntry pteToAdd = jobMapInRAM.get(pid).getPtEntry(pageNumber);
        pteToAdd.setFrameNumber(frameNumber);
        pteToAdd.setLastAccess(clock);
        pteToAdd.setPresent(true);
        pageTableList.set(pageNumber, pteToAdd);
    }



    private void redistributeFrames(Job terminatingJob){
        //only 4 different processes can be in ram
        //update pagetable list
        for(PageTableEntry pte : pageTableList){
            if(pte.isModified() && pte.isPresent()) terminatingJob.setnToHDD(terminatingJob.getnToHDD()+1);
            pte.setPresent(false);


        }
        toHDDWrites=toHDDWrites+terminatingJob.getnToHDD();
        toRAMWrites=toRAMWrites+terminatingJob.getnToRam();//to get a global image of the writes from and to ram (total)
        switch(jobsInRam){
            case 0:
                //was alone in RAM  -> free all frames
                for(RAMEntry re: physicalRam) {re.setPageNr(-1);  re.setPid(-1);}
                break;
            case 1: //still 1 process  in RAM => give 6 extra frames, preference to most recently used
                jobMapInRAM.forEach((pid,jobInRAM)->{
                    for(int i=0; i<6; i++){
                        swapMRU(jobInRAM);
                    }
                    jobInRAM.setnToRam(jobInRAM.getnToRam()+6);  //6 nieuwe frames in ram geladen per overige job
                });
                break;
            case 2://still 2 processes in RAM => 	give each 2 extra frames
                jobMapInRAM.forEach((pid,jobInRAM)->{
                    for(int i=0; i<2; i++){
                        swapMRU(jobInRAM);
                    }
                    jobInRAM.setnToRam(jobInRAM.getnToRam()+2); //2 nieuwe frames in ram geladen per overige job
                });
                break;
            case 3://still 3 processes in RAM => 	give each 1 extra frame
                jobMapInRAM.forEach((pid,jobInRAM)->{
                    swapMRU(jobInRAM);
                    jobInRAM.setnToRam(jobInRAM.getnToRam()+1); //1 extra frame in ram geladen per overige job
                });
                break;
            //there can't be 4 jobs left in ram
            default : System.out.println("ERROR: more than 4 jobs in ram");
        }
    }

    private void swapMRU(Job remainingJob){
        //remainingJob is a job that still exists in RAM, the current job is the terminating one
        //1) search for frame from terminating job
        RAMEntry oldRe=null;
        for(RAMEntry re : physicalRam){
            if(re.getPid()==currentInstruction.getPid()){
                oldRe = re;
                break;
            }
        }
        System.out.println("oldRe: "+oldRe);
        //2) search for most recently used frame of remainingJob that is not present in RAM
        int lastUsed = -1;
        PageTableEntry mruPte=null;
        for(PageTableEntry pte : remainingJob.getPtEntries()){
            if(!pte.isPresent() && pte.getLastAccess()>lastUsed) {
                lastUsed = pte.getLastAccess();
                mruPte = pte;
            }
        }
        System.out.println("mruPte: "+mruPte);
        //3) replace old frame with this MRU frame
        mruPte.setLastAccess(clock);
        mruPte.setPresent(true);
        mruPte.setFrameNumber(oldRe.getFrameNr());
        physicalRam[oldRe.getFrameNr()].setPid(remainingJob.getPid());
        physicalRam[oldRe.getFrameNr()].setPageNr(mruPte.getPageNumber());

    }


}
