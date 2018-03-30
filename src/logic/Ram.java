package logic;

import dataentities.Operation;
import dataentities.Operations;

public class Ram {
    int clock;
    int pageWrites;
    private Operations operations;
    private Operations processedOperations;

    public Ram(Operations operations) {
        this.operations = operations;
        this.processedOperations = new Operations();
    }

    public void setOperations(Operations operations){ this.operations = operations;}


    public void runAllRemaining() {
        while(operations.getOperationsList().size() < 0){

        }
    }

    public void executeNextOperation() {
        Operation currentOperation;
        if (this.operations.getOperationsList().size()!= 0) currentOperation = this.operations.getOperationsList().remove(0);
    }

}
