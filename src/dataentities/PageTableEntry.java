package dataentities;

public class PageTableEntry {

    private String process;
    private int pageNumber;

    private boolean present;
    private boolean modified;
    private int lastAccess;
    private int frameNumber;

    public PageTableEntry(int pageNumber,int frameNumber, boolean present, boolean modified) {
        this.pageNumber = pageNumber;
        this.frameNumber = frameNumber;
        this.present = present;
        this.modified = modified;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public int getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(int lastAccess) {
        this.lastAccess = lastAccess;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }

    @Override
    public String toString() {
        return "PageTableEntry [pageNr=" + pageNumber + ", present=" + present + ", modified=" + modified +
                ", lastAccess=" + lastAccess + ", frameNr=" + frameNumber + "]";
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }
}
