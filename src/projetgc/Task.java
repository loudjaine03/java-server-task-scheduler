package projetgc;

public class Task {

    public enum Priority {
        NORMAL(1), URGENT(3);
        private final int value;
        Priority(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    private final int id;
    private final int size;
    private final Priority priority;
    private long submissionTime;
    private long completionTime;
    private String assignedServer;

    public Task(int id, int size, Priority priority) {
        this.id = id;
        this.size = size;
        this.priority = priority;
        this.submissionTime = System.currentTimeMillis();
    }

    public int      getId()              { return id; }
    public int      getSize()            { return size; }
    public Priority getPriority()        { return priority; }
    public long     getSubmissionTime()  { return submissionTime; }
    public long     getCompletionTime()  { return completionTime; }
    public String   getAssignedServer()  { return assignedServer; }
    public boolean  isUrgent()           { return priority == Priority.URGENT; }

    public void setCompletionTime(long t)  { this.completionTime = t; }
    public void setAssignedServer(String s){ this.assignedServer = s; }
    public void setSubmissionTime(long t)  { this.submissionTime = t; }

    public long getExecutionTime() {
        if (completionTime == 0) return -1;
        return completionTime - submissionTime;
    }

    @Override
    public String toString() {
        return String.format("Task{id=%d, size=%d, priority=%s}", id, size, priority);
    }
}