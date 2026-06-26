package org.example.repository.memory;

import org.example.domain.Task;
import org.example.repository.InMemoryRepository;
import java.util.UUID;

public class TaskRepository extends InMemoryRepository<Task,UUID> {

    public TaskRepository() {
        super(task -> task.getId());
    }
    


}
