package projetgc;

import java.util.*;

public class SimulationStats {

    private final List<Scheduler.SchedulingResult> results;
    private long startTime;
    private long endTime;

    public SimulationStats() {
        this.results = new ArrayList<>();
    }

    public void startSimulation() { startTime = System.currentTimeMillis(); }
    public void endSimulation()   { endTime   = System.currentTimeMillis(); }

    public void addResult(Scheduler.SchedulingResult r) { results.add(r); }

    public int    getTotalTasks()     { return results.size(); }
    public int    getUrgentTasks()    { return (int) results.stream().filter(r -> r.getTask().isUrgent()).count(); }
    public int    getNormalTasks()    { return getTotalTasks() - getUrgentTasks(); }
    public int    getSuccessfulTasks(){ return (int) results.stream().filter(Scheduler.SchedulingResult::isSuccess).count(); }
    public long   getSimulationDuration(){ return endTime - startTime; }

    public double getAverageExecutionTime() {
        return results.stream()
            .filter(Scheduler.SchedulingResult::isSuccess)
            .mapToLong(Scheduler.SchedulingResult::getExecutionTime)
            .average().orElse(0);
    }

    public void printReport(List<Server> servers) {
        System.out.println("\n" + "=".repeat(55));
        System.out.println("       RAPPORT DE SIMULATION GRIDSIM");
        System.out.println("=".repeat(55));

        System.out.printf("  Tâches totales    : %d%n", getTotalTasks());
        System.out.printf("  → Normales        : %d%n", getNormalTasks());
        System.out.printf("  → Urgentes        : %d%n", getUrgentTasks());
        System.out.printf("  Réussies          : %d%n", getSuccessfulTasks());
        System.out.printf("  Tps moyen exec    : %.2f ms%n", getAverageExecutionTime());
        System.out.printf("  Durée simulation  : %d ms%n", getSimulationDuration());

        System.out.println("\n" + "-".repeat(55));
        System.out.printf("  %-12s %-10s %-15s %-12s%n",
            "Serveur","Tâches","Tps Moy (ms)","Charge finale");
        System.out.println("  " + "-".repeat(52));

        for (Server s : servers) {
            long count = results.stream()
                .filter(r -> r.isSuccess() && r.getServer().getName().equals(s.getName()))
                .count();
            double avg = results.stream()
                .filter(r -> r.isSuccess() && r.getServer().getName().equals(s.getName()))
                .mapToLong(Scheduler.SchedulingResult::getExecutionTime)
                .average().orElse(0);
            System.out.printf("  %-12s %-10d %-15.2f %-12d%n",
                s.getName(), count, avg, s.getLoad());
        }

        System.out.println("\n" + "-".repeat(55));
        System.out.printf("  %-6s %-8s %-10s %-12s %-12s%n",
            "ID","Taille","Priorité","Serveur","Tps (ms)");
        System.out.println("  " + "-".repeat(52));
        for (Scheduler.SchedulingResult r : results) {
            if (r.isSuccess()) {
                System.out.printf("  %-6d %-8d %-10s %-12s %-12d%n",
                    r.getTask().getId(),
                    r.getTask().getSize(),
                    r.getTask().getPriority(),
                    r.getServer().getName(),
                    r.getExecutionTime());
            }
        }

        System.out.println("\n" + "=".repeat(55));
        System.out.println("  Simulation terminée avec succès !");
        System.out.println("=".repeat(55));
    }
}