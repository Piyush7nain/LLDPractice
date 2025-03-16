package ThreadPoolExecutorExample;

public class RejectQueueException extends RuntimeException {
    public RejectQueueException() {}
    public RejectQueueException(String message) {
        super(message);
    }
}
