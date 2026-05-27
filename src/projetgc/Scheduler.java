package projetgc;

import java.util.List;

public class Scheduler {

    // Compteurs de décisions
    private int totalDecisions;
    private int urgentDecisions;
    private int normalDecisions;

    // Constructeur : initialise les compteurs de décisions
    public Scheduler() {
        this.totalDecisions  = 0;
        this.urgentDecisions = 0;
        this.normalDecisions = 0;
    }

    
        /**
     * Sélectionne le serveur le plus adapté pour une tâche donnée.
     * Algorithme :
     * - Calcul du score pour chaque serveur
     * - Choix du serveur avec le score maximal
     * - Affichage des scores pour analyse
     *
     * @param task tâche à affecter
     * @param servers liste des serveurs disponibles
     * @return serveur choisi
     */
    public Server selectServer(Task task, List<Server> servers) {
        // Vérification
        if (servers == null || servers.isEmpty()) {
            throw new IllegalArgumentException("Liste de serveurs vide !");
        }

        Server bestServer = null;
        double bestScore  = Double.NEGATIVE_INFINITY;

        System.out.println("\n  [Scheduler] " + task);
        System.out.printf("  %-12s %-8s %-6s %-10s%n",
            "Serveur","Power","Load","Score");
        System.out.println("  " + "-".repeat(40));

        for (Server server : servers) {
            double score = server.computeScore(task);
            System.out.printf("  %-12s %-8d %-6d %.2f%n",
                server.getName(), server.getPower(),
                server.getLoad(), score);
                
            // Mise à jour du meilleur choix
            if (score > bestScore) {
                bestScore  = score;
                bestServer = server;
            }
        }
        // Résultat final de décision
        System.out.printf("   %s Choix : %s (score=%.2f)%n",
            task.isUrgent() ? "URGENT" : "NORMAL",
            bestServer.getName(), bestScore);

        // Mise à jour des statistiques
        totalDecisions++;
        if (task.isUrgent()) urgentDecisions++;
        else normalDecisions++;

        return bestServer;
    }
        /**
     * Exécute la tâche sur le serveur sélectionné.
     * Mesure :
     * - Temps de décision du scheduler
     * - Temps d'exécution de la tâche
     *
     * @param task tâche à exécuter
     * @param servers liste des serveurs
     * @return résultat complet de l'affectation
     */
    public SchedulingResult dispatch(Task task, List<Server> servers) {
        long start    = System.currentTimeMillis();
        Server chosen = selectServer(task, servers);
        long decTime  = System.currentTimeMillis() - start;

        if (chosen == null)
            return new SchedulingResult(task, null, -1, decTime, false);

        long execTime = chosen.executeTask(task);
        return new SchedulingResult(task, chosen, execTime, decTime, true);
    }

    public int getTotalDecisions()  { return totalDecisions; }
    public int getUrgentDecisions() { return urgentDecisions; }
    public int getNormalDecisions() { return normalDecisions; }

    // Classe interne résultat
    public static class SchedulingResult {
        private final Task    task;
        private final Server  server;
        private final long    executionTime;
        private final long    decisionTime;
        private final boolean success;

        public SchedulingResult(Task task, Server server,
                long executionTime, long decisionTime, boolean success) {
            this.task          = task;
            this.server        = server;
            this.executionTime = executionTime;
            this.decisionTime  = decisionTime;
            this.success       = success;
        }

        public Task    getTask()          { return task; }
        public Server  getServer()        { return server; }
        public long    getExecutionTime() { return executionTime; }
        public long    getDecisionTime()  { return decisionTime; }
        public boolean isSuccess()        { return success; }
    }
}