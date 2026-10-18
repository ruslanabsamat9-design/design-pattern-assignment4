package com.flightreservation.workflow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.flightreservation.storage.JsonStore;
import java.util.List;
import java.util.Optional;

public class WorkflowRepository {
    private final JsonStore store;
    public WorkflowRepository(JsonStore store){this.store=store;}
    public List<WorkflowProgress> findAll(){return store.read("workflows.json",new TypeReference<List<WorkflowProgress>>(){});}
    public void save(WorkflowProgress w){
        List<WorkflowProgress> all=findAll();all.removeIf(x->x.getId().equals(w.getId()));all.add(w);store.write("workflows.json",all);
    }
    public Optional<WorkflowProgress> find(String id){return findAll().stream().filter(x->x.getId().equals(id)).findFirst();}
}
