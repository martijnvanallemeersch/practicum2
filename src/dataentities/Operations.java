package dataentities;

import java.util.LinkedList;
import java.util.List;

public class Operations {
    List<Operation> operationsList;

    public Operations() {
        this.operationsList = new LinkedList<>();
    }
    public Operations(List operationsListArg) {
        this.operationsList = operationsListArg;
    }

    public void addOperation(Operation operationArg) {
        if (operationsList != null) {
            operationsList.add(operationArg);
        }
    }

    public List<Operation> getOperationsList() {return this.operationsList;}
}
