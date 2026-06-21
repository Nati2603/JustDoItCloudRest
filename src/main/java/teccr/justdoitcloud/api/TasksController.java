package teccr.justdoitcloud.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import teccr.justdoitcloud.data.Task;
import teccr.justdoitcloud.data.User;
import teccr.justdoitcloud.service.TaskService;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users/{userId}/tasks")
public class TasksController {

    private final TaskService taskService;

    public TasksController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public Iterable<Task> getTasksForUser(@PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        return taskService.getTasksForUser(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task addTaskToUser(@PathVariable Long userId,
                              @RequestBody(required = false) Task task,
                              @RequestParam(name = "autogenerate", required = false) String autogenerate) {
        User user = new User();
        user.setId(userId);

        boolean auto = autogenerate != null && (autogenerate.isEmpty() || autogenerate.equalsIgnoreCase("true"));

        if (auto) {
            // Ignorar el cuerpo y usar el generador para crear la tarea
            return taskService.autogenerateTaskForUser(user);
        }

        // Flujo normal: crear usando el Task provisto en el body
        if (task == null) {
            throw new IllegalArgumentException("Task body is required when autogenerate is not used");
        }
        return taskService.addTaskToUser(user, task);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id, @PathVariable Long userId) {
        Optional<Task> taskOpt = taskService.getTaskById(id);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            if (task.getUserId().equals(userId)) {
                return new ResponseEntity<>(task, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @PathVariable Long userId, @RequestBody Task task) {
        Optional<Task> taskOpt = taskService.getTaskById(id);
        if (taskOpt.isPresent()) {
            Task result = taskOpt.get();
            if (result.getUserId().equals(userId)) {
                return new ResponseEntity<>(taskService.updateTaskFields(id, task),  HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, @PathVariable Long userId) {
        if (taskService.deleteTaskById(id, userId)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}
