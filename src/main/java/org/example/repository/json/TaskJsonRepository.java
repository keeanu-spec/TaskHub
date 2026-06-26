package org.example.repository.json;

import java.nio.file.Path;
import java.util.UUID;

import org.example.domain.Task;

public class TaskJsonRepository  extends JsonRepository<Task,UUID>{

    public TaskJsonRepository(){
        super(Task::getId,Path.of("data/tasks.json"),Task.class);
    }
    
}
