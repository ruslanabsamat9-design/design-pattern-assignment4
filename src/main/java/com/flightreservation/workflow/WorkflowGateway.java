package com.flightreservation.workflow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WorkflowGateway {
    private final Path dataFile;
    private final ObjectMapper objectMapper;

    public WorkflowGateway(Path dataDirectory) {
        this.objectMapper = new ObjectMapper();
        this.dataFile = dataDirectory.resolve("workflow.json");
        try {
            Files.createDirectories(dataDirectory);
            if (Files.notExists(dataFile)) {
                Files.writeString(dataFile, "[]");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize workflow storage", e);
        }
    }

    public WorkflowProgress save(WorkflowProgress progress) {
        List<WorkflowProgress> workflows = loadAll();
        workflows.removeIf(w -> w.getWorkflowId() != null && w.getWorkflowId().equals(progress.getWorkflowId()));
        workflows.add(progress);
        writeAll(workflows);
        return progress;
    }

    public WorkflowProgress loadLatest() {
        List<WorkflowProgress> workflows = loadAll();
        if (workflows.isEmpty()) {
            return null;
        }
        return workflows.get(workflows.size() - 1);
    }

    public List<WorkflowProgress> loadAll() {
        try {
            return objectMapper.readValue(dataFile.toFile(), new TypeReference<List<WorkflowProgress>>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Could not read workflow file", e);
        }
    }

    private void writeAll(List<WorkflowProgress> workflows) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile.toFile(), workflows);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write workflow file", e);
        }
    }
}
