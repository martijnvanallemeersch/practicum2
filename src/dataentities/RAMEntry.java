package dataentities;

public class RAMEntry {

	private int frameNr;
	private int pid;
	private int pageNr;
	
	public RAMEntry(int frameNr) {
		this.frameNr = frameNr;
		this.pageNr = -1;
		this.pid = -1;
	}
	
	public RAMEntry(int frameNr, int pid, int pageNr) {
		super();
		this.frameNr = frameNr;
		this.pid = pid;
		this.pageNr = pageNr;
	}

	//getters & setters
	public int getFrameNumber() {return frameNr;}
	public void setFrameNr(int frameNr) {this.frameNr = frameNr;}
	
	public int getPid() {return pid;}
	public void setPid(int pid) {this.pid = pid;}
	
	public int getPageNr() {return pageNr;}
	public void setPageNr(int pageNr) {this.pageNr = pageNr;	}

	@Override
	public String toString() {
		return "RAMEntry [frameNr=" + frameNr + ", pid=" + pid + ", pageNr=" + pageNr + "]";
	}
	
	
}
