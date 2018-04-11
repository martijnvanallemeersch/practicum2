package dataentities;

public class Instruction {
    private int pid;
    private String operation;
    private int address;



    public Instruction(int pidArg, String operationArg, int addressArg)
    {
        this.pid = pidArg;
        this.operation = operationArg;
        this.address = addressArg;
    }

    @Override
    public String toString() {
        return "<PID>" + pid + ":\t Instruction: " + operation + "\t Address: " + address;
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
