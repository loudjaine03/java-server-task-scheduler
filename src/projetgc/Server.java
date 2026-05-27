package projetgc;

import java.util.*;

public class Server {

    private final String name;
    private final int    power;
    private int          load;
    private int          totalTasksProcessed;
    private long         totalProcessingTime;
    private final List<Task> taskHistory;
    
    // Constructeur
    public Server(String name, int power, int initialLoad) {
        this.name = name;
        this.power = power;
        this.load = initialLoad;
        this.totalTasksProcessed = 0;
        this.totalProcessingTime = 0;
        this.taskHistory = new ArrayList<>();
    }

    // Formules de score
    public double computeNormalScore() {
        return (double) power / (1.0 + load);
    }

    public double computeUrgentScore() {
        return power * 2.0;
    }

    public double computeScore(Task task) {
        return task.isUrgent() ? computeUrgentScore() : computeNormalScore();
    }

    public long executeTask(Task task) {
        // Augmenter temporairement la charge
        load++;
        
        // Enregistrer l'affectation
        task.setAssignedServer(this.name);
        task.setSubmissionTime(System.currentTimeMillis());

        // Calcul du temps d'exécutio
        long execTime = (long)((task.getSize() * 100.0) / power);
        task.setCompletionTime(task.getSubmissionTime() + execTime);
        // Mise a jour
        totalTasksProcessed++;
        totalProcessingTime += execTime;
        taskHistory.add(task);

        if (load > 0) load--;
        return execTime;
    }

    public String    getName()               { return name; }
    public int       getPower()              { return power; }
    public int       getLoad()               { return load; }
    public int       getTotalTasksProcessed(){ return totalTasksProcessed; }
    public long      getTotalProcessingTime(){ return totalProcessingTime; }
    public List<Task>getTaskHistory()        { return taskHistory; }
    public void      setLoad(int load)       { this.load = Math.max(0, load); }

    public double getAverageProcessingTime() {
        if (totalTasksProcessed == 0) return 0;
        return (double) totalProcessingTime / totalTasksProcessed;
    }

    @Override
    public String toString() {
        return String.format("Server{name='%s', power=%d, load=%d}", name, power, load);
    }
}