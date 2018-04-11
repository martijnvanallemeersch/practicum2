package dataentities;

import java.util.ArrayList;
import java.util.List;

public class Process {

	private int pid;
	private List<PageTableEntry> ptEntries;
	private int nToHDD;
	private int nToRam;
	
	public Process(int pid) {
		this.pid = pid;
		ptEntries= new ArrayList<>(12);
		for(int i=0; i<16;i++){
			ptEntries.add(new PageTableEntry(i,false,false));
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + pid;
		return result;
	}

	@Override
	public String toString() {
		return "Process{" +
				"pid=" + pid +
				'}';
	}

	public void incrementToHDD () {
		nToHDD++;
	}
	//getters & setters
	public int getPid() {return pid;}
	public void setPid(int pid) {this.pid = pid;}
	
	public List<PageTableEntry> getPtEntries() {return ptEntries;}
	public void setPtEntries(List<PageTableEntry> ptEntries) {this.ptEntries = ptEntries;}

	public int getnToHDD() {return nToHDD;}
	public void setnToHDD(int nToHDD) {this.nToHDD = nToHDD;}

	public int getnToRam() {return nToRam;}
	public void setnToRam(int nToRam) {this.nToRam = nToRam;}

	public PageTableEntry getPtEntry(int pageNumber) {
		return ptEntries.get(pageNumber);
	}

	public void setPtEntry(int pageNumber, PageTableEntry pte) {
		ptEntries.set(pageNumber, pte);
	}
}
