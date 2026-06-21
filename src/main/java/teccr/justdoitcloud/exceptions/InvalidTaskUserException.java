package teccr.justdoitcloud.exceptions;

public class InvalidTaskUserException extends RuntimeException {
    public InvalidTaskUserException(Long taskId, Long userId) {
        super("User " + taskId + " doesn't own task " + userId);
    }
}
