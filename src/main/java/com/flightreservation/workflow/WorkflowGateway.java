package com.flightreservation.workflow;

import com.flightreservation.storage.JsonStore;
import java.util.List;

public class WorkflowGateway {
    private final WorkflowRepository repository;
    public WorkflowGateway(JsonStore store){repository=new WorkflowRepository(store);}
    public void save(WorkflowProgress w){repository.save(w);}
    public List<WorkflowProgress> all(){return repository.findAll();}
    public List<WorkflowProgress> unfinished(){return all().stream()
        .filter(w->w.getStatus()==WorkflowStatus.PENDING_B || w.getStatus()==WorkflowStatus.PENDING_C
                || w.getStatus()==WorkflowStatus.COMPENSATING).toList();}
}
