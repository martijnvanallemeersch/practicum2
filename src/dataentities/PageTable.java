package dataentities;

import java.util.LinkedList;
import java.util.List;

public class PageTable {
    private int pid;
    private List<PageTableEntry> pageTableEntryList;
    private int nToHDD;
    private int nToRam;

    public PageTable(int pid) {
        this.pageTableEntryList = new LinkedList<>();
        for (int i = 0; i < 16; i++) {
            pageTableEntryList.add(new PageTableEntry(i, -1,false, false));
        }
    }

    public void moveEntryToRAM(int pageTableEntry, int frameNumber, int clock) {
        nToRam++;
        pageTableEntryList.get(pageTableEntry).setLastAccess(clock);
        pageTableEntryList.get(pageTableEntry).setPresent(true);
        pageTableEntryList.get(pageTableEntry).setFrameNumber(frameNumber);
    }

    public void moveEntryToHDD(int pageTableEntry) {
        nToHDD++;
        pageTableEntryList.get(pageTableEntry).setPresent(false);
        pageTableEntryList.get(pageTableEntry).setFrameNumber(-1);

    }
    public void incrementToHDD() {
        nToHDD++;
    }

    public int getPid() {
        return pid;
    }

    public List<PageTableEntry> pageTableEntryList() {
        return pageTableEntryList;
    }

    public int getnToHDD() {
        return nToHDD;
    }

    public int getnToRam() {
        return nToRam;
    }

    public void setnToHDD(int nToHDD) {
        this.nToHDD = nToHDD;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public void setnToRam(int nToRam) {
        this.nToRam = nToRam;
    }

    public PageTableEntry getpageTableEntry(int pageNumber) {
        return pageTableEntryList.get(pageNumber);
    }

    public void setpageTableEntry(int pageNumber, PageTableEntry pte) {
        pageTableEntryList.set(pageNumber, pte);
    }
}
