package dataentities;

public class Operation {
    private int pid;
    private String operation;
    private int address;
    private int pagetable;



    public Operation(int pidArg, String operationArg, int addressArg)
    {
        this.pid = pidArg;
        this.operation = operationArg;
        this.address = addressArg;
    }

    @Override
    public String toString() {
        return "<PID>" + pid + ":\t Operation: " + operation + "\t Address: " + address;
    }

    public int getPid() {
        return pid;
    }

    public String getOperation() {
        return operation;
    }

    public int getAddress() {
        return address;
    }
}
