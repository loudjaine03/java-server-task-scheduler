package projetgc;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProjetGC extends JFrame {

    // MODELS
    private static class Server {
        final String name; final int power; int load;
        Server(String n, int pw, int ld) { name=n; power=pw; load=ld; }
        double normalScore() { return power / (1.0 + load); }
        double urgentScore() { return power * 2.0; }
        double score(Task t) { return t.isUrgent() ? urgentScore() : normalScore(); }
        long execute(Task t) {
            long exec = Math.max(1L, Math.round((double) t.size / power * 1000.0));
            load += Math.max(1, t.size / 100);
            t.server = name; t.execMs = exec;
            return exec;
        }
    }

    private static class Task {
        enum P { NORMAL, URGENT }
        final int id, size; final P priority;
        String server; long execMs;
        Task(int id, int size, P p) { this.id=id; this.size=size; priority=p; }
        boolean isUrgent() { return priority == P.URGENT; }
    }

    // PALETTE
    private static final Color BG       = new Color(247, 248, 251);
    private static final Color NAVY     = new Color(15,  23,  42);
    private static final Color BLUE     = new Color(37,  99, 235);
    private static final Color BLUE_LT  = new Color(219,234,254);
    private static final Color GREEN    = new Color(22, 163,  74);
    private static final Color GREEN_LT = new Color(220,252,231);
    private static final Color RED      = new Color(220,  38,  38);
    private static final Color RED_LT   = new Color(254,226,226);
    private static final Color AMBER    = new Color(217,119,   6);
    private static final Color AMBER_LT = new Color(254,243,199);
    private static final Color GRAY     = new Color(107,114,128);
    private static final Color BORD     = new Color(226,232,240);
    private static final Color WHITE    = Color.WHITE;
    private static final Color SURF     = new Color(241,245,249);

    private static final Font FT  = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FS  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FH  = new Font("Segoe UI", Font.BOLD,  14);
    private static final Font FL  = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font FB  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FSM = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FCN = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FCL = new Font("Segoe UI", Font.PLAIN,  9);
    private static final Font FBT = new Font("Segoe UI", Font.BOLD,  12);

    
    // STATE
    private final List<Server> servers = new ArrayList<>();
    private final List<Task>   tasks   = new ArrayList<>();

    private JTextField        tfName, tfPower, tfLoad, tfSize;
    private JComboBox<String> cbPriority;
    private DefaultTableModel serverModel, taskModel, resultModel;
    private JTable            serverTable, taskTable;
    private JLabel            statusLabel, lblTotal, lblNormal, lblUrgent, lblAvg;
    private JPanel            resultCenter;
    private JPanel            placeholder, livePanel;
    private boolean           ran = false;

    public ProjetGC() {
        super("Scheduler");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1220, 840);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMain(),   BorderLayout.CENTER);
        add(buildStatus(), BorderLayout.SOUTH);

        addServer("ServerA", 1000, 50);
        addServer("ServerB",  700, 10);
        addServer("ServerC",  400,  5);
        addTask(200, Task.P.NORMAL);
        addTask(50,  Task.P.NORMAL);
        addTask(300, Task.P.NORMAL);
        addTask(100, Task.P.URGENT);
        addTask(500, Task.P.URGENT);
    }

    // HEADER
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(NAVY); p.setBorder(new EmptyBorder(14,24,14,24));

        JPanel left = new JPanel(); left.setLayout(new BoxLayout(left,BoxLayout.Y_AXIS));
        left.setBackground(NAVY);
        JLabel t = new JLabel("Scheduler");
        t.setFont(FT); t.setForeground(WHITE);
        JLabel s = new JLabel("Affectation des tâches sur serveurs distribués");
        s.setFont(FS); s.setForeground(new Color(148,163,184));
        left.add(t); left.add(Box.createVerticalStrut(3)); left.add(s);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        right.setBackground(NAVY);
        right.add(pill("NORMAL : power / (1 + load)", new Color(30,58,138), BLUE_LT));
        right.add(pill("URGENT : power × 2",          new Color(127,29,29), RED_LT));
        p.add(left, BorderLayout.WEST); p.add(right, BorderLayout.EAST);
        return p;
    }

    private JLabel pill(String txt, Color bg, Color fg) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Consolas",Font.BOLD,11));
        l.setForeground(fg); l.setBackground(bg); l.setOpaque(true);
        l.setBorder(new EmptyBorder(5,10,5,10)); return l;
    }

    
    // MAIN
    private JPanel buildMain() {
        JPanel p = new JPanel(new BorderLayout(12,12));
        p.setBackground(BG); p.setBorder(new EmptyBorder(12,16,8,16));
        p.add(buildStatRow(), BorderLayout.NORTH);
        JPanel cols = new JPanel(new GridLayout(1,3,10,0));
        cols.setBackground(BG);
        cols.add(buildServerPanel());
        cols.add(buildTaskPanel());
        cols.add(buildResultPanel());
        p.add(cols, BorderLayout.CENTER);
        return p;
    }

    
    // STAT CARDS 
    private JPanel buildStatRow() {
        JPanel p = new JPanel(new GridLayout(1,4,10,0));
        p.setBackground(BG); p.setBorder(new EmptyBorder(0,0,10,0));
        lblTotal  = new JLabel("0"); lblNormal = new JLabel("0");
        lblUrgent = new JLabel("0"); lblAvg    = new JLabel("—");
        p.add(statCard("Total tâches",   lblTotal,  BLUE,  BLUE_LT));
        p.add(statCard("Normales",        lblNormal, GREEN, GREEN_LT));
        p.add(statCard("Urgentes",        lblUrgent, RED,   RED_LT));
        p.add(statCard("Tps moy. exec",   lblAvg,    AMBER, AMBER_LT));
        return p;
    }

    private JPanel statCard(String lbl, JLabel val, Color accent, Color bg) {
        JPanel p = new JPanel(new BorderLayout(3,3)); p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0,3,0,0,accent), new EmptyBorder(10,14,10,14)));
        JLabel l = new JLabel(lbl.toUpperCase()); l.setFont(FCL); l.setForeground(accent);
        val.setFont(FCN); val.setForeground(NAVY);
        p.add(l, BorderLayout.NORTH); p.add(val, BorderLayout.CENTER);
        return p;
    }

    // SERVER PANEL
    private JPanel buildServerPanel() {
        JPanel p = card("Serveurs");
        tfName=field(); tfPower=field(); tfLoad=field();

        JPanel form = new JPanel(new GridLayout(3,2,6,7)); form.setBackground(WHITE);
        form.add(lbl("Nom :"));          form.add(tfName);
        form.add(lbl("Power (MIPS) :")); form.add(tfPower);
        form.add(lbl("Load initial :")); form.add(tfLoad);

        JLabel prev = new JLabel("Sc. normal : —     Sc. urgent : —");
        prev.setFont(FSM); prev.setForeground(GRAY); prev.setBorder(new EmptyBorder(3,2,3,2));

        KeyAdapter ka = new KeyAdapter() { public void keyReleased(KeyEvent e) {
            try {
                int pw=Integer.parseInt(tfPower.getText().trim());
                int ld=Integer.parseInt(tfLoad.getText().trim());
                prev.setText(String.format(Locale.US,"Sc. normal : %.2f     Sc. urgent : %d",pw/(1.0+ld),pw*2));
                prev.setForeground(BLUE);
            } catch(Exception ex) { prev.setText("Sc. normal : —     Sc. urgent : —"); prev.setForeground(GRAY); }
        }};
        tfPower.addKeyListener(ka); tfLoad.addKeyListener(ka);

        JButton add = btn("+ Ajouter", BLUE);
        add.addActionListener(e -> {
            try {
                String nm=tfName.getText().trim();
                int pw=Integer.parseInt(tfPower.getText().trim());
                int ld=Integer.parseInt(tfLoad.getText().trim());
                if(nm.isEmpty()){err("Nom vide !");return;}
                if(pw<=0){err("Power > 0 !");return;}
                if(ld<0){err("Load >= 0 !");return;}
                addServer(nm,pw,ld);
                tfName.setText(""); tfPower.setText(""); tfLoad.setText("");
                prev.setText("Sc. normal : —     Sc. urgent : —"); prev.setForeground(GRAY);
                ok("Serveur '"+nm+"' ajouté.");
            } catch(NumberFormatException ex){ err("Power et Load = entiers !"); }
        });

        JButton del = btn(" Supprimer", RED);
        del.addActionListener(e -> {
            int row=serverTable.getSelectedRow();
            if(row<0){err("Sélectionne un serveur !");return;}
            String nm=(String)serverModel.getValueAt(row,0);
            servers.removeIf(s->s.name.equals(nm));
            serverModel.removeRow(row); refreshStats();
            ok("Serveur '"+nm+"' supprimé.");
        });

        JPanel btns = new JPanel(new GridLayout(1,2,6,0)); btns.setBackground(WHITE);
        btns.add(add); btns.add(del);

        serverModel = new DefaultTableModel(new String[]{"Nom","Power","Load","Sc. Normal","Sc. Urgent"},0){
            public boolean isCellEditable(int r,int c){return false;}};
        serverTable = mkTable(serverModel);
        serverTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                setHorizontalAlignment(CENTER); setOpaque(true);
                int best=bestRow();
                if(row==best){setBackground(GREEN_LT);setForeground(new Color(21,128,61));}
                else{setBackground(row%2==0?WHITE:SURF);setForeground(NAVY);}
                if(sel){setBackground(BLUE_LT);setForeground(new Color(30,58,138));}
                return this;
            }
        });

        JScrollPane sc = new JScrollPane(serverTable); sc.setBorder(new LineBorder(BORD,1));
        JLabel hint = new JLabel("  ● ligne verte = meilleur score normal");
        hint.setFont(FSM); hint.setForeground(new Color(21,128,61));

        JPanel top = new JPanel(new BorderLayout(5,6)); top.setBackground(WHITE);
        top.add(form,BorderLayout.NORTH); top.add(prev,BorderLayout.CENTER); top.add(btns,BorderLayout.SOUTH);
        p.add(top,BorderLayout.NORTH); p.add(sc,BorderLayout.CENTER); p.add(hint,BorderLayout.SOUTH);
        return p;
    }

    private int bestRow() {
        int best=-1; double top=-1;
        for(int i=0;i<serverModel.getRowCount();i++){
            try{
                double sc=Double.parseDouble(serverModel.getValueAt(i,3).toString().replace(",","."));
                if(sc>top){top=sc;best=i;}
            }catch(Exception ignored){}
        }
        return best;
    }

    // TASK PANEL
    private JPanel buildTaskPanel() {
        JPanel p = card("Tâches");
        tfSize=field();
        cbPriority=new JComboBox<>(new String[]{"NORMAL","URGENT"});
        cbPriority.setFont(FB); cbPriority.setBackground(WHITE);

        JPanel form = new JPanel(new GridLayout(2,2,6,7)); form.setBackground(WHITE);
        form.add(lbl("Taille (size) :")); form.add(tfSize);
        form.add(lbl("Priorité :"));      form.add(cbPriority);

        JButton add = btn("+ Ajouter", GREEN);
        add.addActionListener(e -> {
            try{
                int sz=Integer.parseInt(tfSize.getText().trim());
                if(sz<=0){err("Taille > 0 !");return;}
                Task.P pr="URGENT".equals(cbPriority.getSelectedItem())?Task.P.URGENT:Task.P.NORMAL;
                addTask(sz,pr); tfSize.setText(""); ok("Tâche ajoutée.");
            }catch(NumberFormatException ex){err("Taille = entier !");}
        });

        JButton del = btn("Supprimer", RED);
        del.addActionListener(e -> {
            int row=taskTable.getSelectedRow();
            if(row<0){err("Sélectionne une tâche !");return;}
            int id=(int)taskModel.getValueAt(row,0);
            tasks.removeIf(t->t.id==id); taskModel.removeRow(row); refreshStats();
            ok("Tâche #"+id+" supprimée.");
        });

        JPanel btns=new JPanel(new GridLayout(1,2,6,0)); btns.setBackground(WHITE);
        btns.add(add); btns.add(del);

        taskModel=new DefaultTableModel(new String[]{"ID","Taille","Priorité"},0){
            public boolean isCellEditable(int r,int c){return false;}};
        taskTable=mkTable(taskModel);
        taskTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                setHorizontalAlignment(CENTER); setOpaque(true);
                boolean urg=row<taskModel.getRowCount()&&"URGENT".equals(taskModel.getValueAt(row,2));
                if(urg){setBackground(RED_LT);setForeground(RED);}
                else{setBackground(row%2==0?WHITE:SURF);setForeground(NAVY);}
                if(sel){setBackground(BLUE_LT);setForeground(new Color(30,58,138));}
                return this;
            }
        });

        JScrollPane sc=new JScrollPane(taskTable); sc.setBorder(new LineBorder(BORD,1));
        JPanel legend=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0)); legend.setBackground(WHITE);
        JLabel n=new JLabel("● normal"); n.setFont(FSM); n.setForeground(GRAY);
        JLabel u=new JLabel("● URGENT"); u.setFont(FSM); u.setForeground(RED);
        legend.add(n); legend.add(u);

        JPanel top=new JPanel(new BorderLayout(5,6)); top.setBackground(WHITE);
        top.add(form,BorderLayout.NORTH); top.add(btns,BorderLayout.SOUTH);
        p.add(top,BorderLayout.NORTH); p.add(sc,BorderLayout.CENTER); p.add(legend,BorderLayout.SOUTH);
        return p;
    }

    // RESULT PANEL  — only shows: who went where + exec time
    private JPanel buildResultPanel() {
        JPanel p = card("Résultats");

        // Placeholder 
        placeholder = new JPanel(new GridBagLayout());
        placeholder.setBackground(SURF);
        placeholder.setBorder(new LineBorder(BORD,1));
        JPanel ph = new JPanel(); ph.setLayout(new BoxLayout(ph,BoxLayout.Y_AXIS));
        ph.setBackground(SURF);
        JLabel ico = new JLabel("▶"); ico.setFont(new Font("Segoe UI",Font.BOLD,40));
        ico.setForeground(new Color(200,210,230)); ico.setAlignmentX(CENTER_ALIGNMENT);
        JLabel m1 = new JLabel("Lance la simulation"); m1.setFont(FH);
        m1.setForeground(new Color(160,172,188)); m1.setAlignmentX(CENTER_ALIGNMENT);
        JLabel m2 = new JLabel("pour voir les affectations"); m2.setFont(FSM);
        m2.setForeground(new Color(180,190,204)); m2.setAlignmentX(CENTER_ALIGNMENT);
        ph.add(ico); ph.add(Box.createVerticalStrut(10)); ph.add(m1);
        ph.add(Box.createVerticalStrut(4)); ph.add(m2);
        placeholder.add(ph);

        // Live panel 
        livePanel = new JPanel(new BorderLayout(0,10));
        livePanel.setBackground(WHITE); livePanel.setVisible(false);

        // Affectation table
        resultModel = new DefaultTableModel(
            new String[]{"Tâche","Priorité"," Serveur","Exec (ms)"}, 0) {
            public boolean isCellEditable(int r,int c){return false;}
        };

        JTable rt = mkTable(resultModel);
        rt.setRowHeight(36);
        rt.getColumnModel().getColumn(0).setPreferredWidth(55);
        rt.getColumnModel().getColumn(1).setPreferredWidth(80);
        rt.getColumnModel().getColumn(2).setPreferredWidth(130);
        rt.getColumnModel().getColumn(3).setPreferredWidth(90);

        rt.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable t,Object v,
                    boolean sel,boolean foc,int row,int col){
                super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                if(row>=resultModel.getRowCount()) return this;
                setHorizontalAlignment(CENTER); setOpaque(true);
                boolean urg="URGENT".equals(resultModel.getValueAt(row,1));

                // Row background
                setBackground(urg?new Color(255,249,249):(row%2==0?WHITE:SURF));
                setForeground(NAVY); setFont(FB);

                switch(col){
                    case 0 -> { // Task #
                        setForeground(GRAY); setFont(FSM);
                    }
                    case 1 -> { // Priority pill
                        setFont(FBT);
                        if(urg){setBackground(RED_LT);setForeground(RED);}
                        else{setBackground(GREEN_LT);setForeground(GREEN);}
                    }
                    case 2 -> { // Server — the KEY decision
                        setBackground(BLUE_LT);
                        setForeground(new Color(29,78,216));
                        setFont(FBT);
                    }
                    case 3 -> { // Exec time
                        setForeground(AMBER); setFont(FBT);
                    }
                }
                if(sel){setBackground(BLUE_LT);setForeground(NAVY);setFont(FB);}
                return this;
            }
        });

        JScrollPane rScroll=new JScrollPane(rt); rScroll.setBorder(new LineBorder(BORD,1));

        // Bottom bar: avg exec time only (total/normal/urgent already shown top)
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(SURF);
        bottom.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1,0,0,0,BORD), new EmptyBorder(8,12,8,12)));

        JLabel simDoneLabel = new JLabel("—");
        simDoneLabel.setFont(FSM); simDoneLabel.setForeground(GRAY);
        simDoneLabel.setName("SIM_DONE");
        bottom.add(simDoneLabel, BorderLayout.WEST);

        JLabel avgLabel = new JLabel("—");
        avgLabel.setFont(new Font("Segoe UI",Font.BOLD,12)); avgLabel.setForeground(AMBER);
        avgLabel.setName("AVG");
        JLabel avgLblText = new JLabel("tps moyen : ");
        avgLblText.setFont(FSM); avgLblText.setForeground(GRAY);
        JPanel avgRow = new JPanel(new FlowLayout(FlowLayout.RIGHT,4,0));
        avgRow.setBackground(SURF);
        avgRow.add(avgLblText); avgRow.add(avgLabel);
        bottom.add(avgRow, BorderLayout.EAST);

        livePanel.add(rScroll, BorderLayout.CENTER);
        livePanel.add(bottom,  BorderLayout.SOUTH);

        // Container
        resultCenter = new JPanel(new BorderLayout());
        resultCenter.setBackground(WHITE);
        resultCenter.add(placeholder, BorderLayout.CENTER);

        // Run / Reset buttons
        JButton btnRun = new JButton("   Lancer");
        btnRun.setFont(new Font("Segoe UI",Font.BOLD,14));
        btnRun.setBackground(BLUE); btnRun.setForeground(WHITE);
        btnRun.setFocusPainted(false); btnRun.setBorderPainted(false);
        btnRun.setPreferredSize(new Dimension(0,44));
        btnRun.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRun.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){btnRun.setBackground(new Color(29,78,216));}
            public void mouseExited(MouseEvent e) {btnRun.setBackground(BLUE);}
        });
        btnRun.addActionListener(e -> runSim(rt, simDoneLabel, avgLabel));

        JButton btnReset = btn("Réinitialiser", GRAY);
        btnReset.addActionListener(e -> {
            ran=false; resultModel.setRowCount(0);
            simDoneLabel.setText("—"); avgLabel.setText("—");
            livePanel.setVisible(false); placeholder.setVisible(true);
            resultCenter.remove(livePanel); resultCenter.add(placeholder,BorderLayout.CENTER);
            resultCenter.revalidate(); resultCenter.repaint();
            refreshStats(); ok("Réinitialisé.");
        });

        JPanel btnRow=new JPanel(new BorderLayout(6,0)); btnRow.setBackground(WHITE);
        btnRow.setBorder(new EmptyBorder(8,0,0,0));
        btnRow.add(btnRun,BorderLayout.CENTER); btnRow.add(btnReset,BorderLayout.EAST);

        p.add(resultCenter, BorderLayout.CENTER);
        p.add(btnRow,       BorderLayout.SOUTH);
        return p;
    }

    // STATUS BAR
    private JPanel buildStatus() {
        JPanel p=new JPanel(new BorderLayout()); p.setBackground(SURF);
        p.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1,0,0,0,BORD),new EmptyBorder(5,16,5,16)));
        statusLabel=new JLabel("Prêt.");
        statusLabel.setFont(FSM); statusLabel.setForeground(GRAY);

        return p;
    }

    // DATA
    private void addServer(String name, int power, int load) {
        servers.add(new Server(name,power,load));
        serverModel.addRow(new Object[]{
            name, power, load,
            String.format(Locale.US,"%.2f",power/(1.0+load)),
            String.format(Locale.US,"%.0f",power*2.0)
        });
        refreshStats();
    }

    private void addTask(int size, Task.P priority) {
        int id=tasks.size()+1;
        tasks.add(new Task(id,size,priority));
        taskModel.addRow(new Object[]{id,size,priority.toString()});
        refreshStats();
    }

    private void refreshStats() {
        lblTotal.setText(String.valueOf(tasks.size()));
        lblNormal.setText(String.valueOf(tasks.stream().filter(t->!t.isUrgent()).count()));
        lblUrgent.setText(String.valueOf(tasks.stream().filter(Task::isUrgent).count()));
        if(tasks.isEmpty()) lblAvg.setText("—");
        statusLabel.setText(servers.size()+" serveurs  |  "+tasks.size()+" tâches");
        statusLabel.setForeground(GRAY);
    }
    private void refreshServerTable() {
        for (int i = 0; i < servers.size(); i++) {
            Server s = servers.get(i);
            serverModel.setValueAt(s.load, i, 2);
            serverModel.setValueAt(String.format(Locale.US, "%.2f", s.normalScore()), i, 3);
            serverModel.setValueAt(String.format(Locale.US, "%.0f", s.urgentScore()), i, 4);
        }
    }

    // SIMULATION
    private void runSim(JTable rt, JLabel doneLabel, JLabel avgLabel) {
        if(servers.isEmpty()){err("Ajoute au moins un serveur !");return;}
        if(tasks.isEmpty())  {err("Ajoute au moins une tâche !");return;}

        tasks.forEach(t->{t.server=null;t.execMs=0;});
        resultModel.setRowCount(0);

        long start=System.currentTimeMillis(); long totalExec=0;

        for(Task task : tasks){
            // Find best server
            Server best=null; double top=Double.NEGATIVE_INFINITY;
            for(Server s:servers){ double sc=s.score(task); if(sc>top){top=sc;best=s;} }
            if(best==null) continue;
            long exec=best.execute(task); totalExec+=exec;
            refreshServerTable();

            resultModel.addRow(new Object[]{
                "#"+task.id,
                task.priority.toString(),
                best.name,
                exec+" ms"
            });
        }

        long dur=System.currentTimeMillis()-start;
        double avg=tasks.isEmpty()?0:totalExec/(double)tasks.size();

        // Swap to live
        if(!ran){
            placeholder.setVisible(false);
            resultCenter.remove(placeholder);
            resultCenter.add(livePanel,BorderLayout.CENTER);
            livePanel.setVisible(true); ran=true;
        }

        doneLabel.setText(tasks.size()+" tâches  ·  "+dur+" ms");
        avgLabel.setText(String.format(Locale.US,"%.1f ms",avg));

        // Update top stat cards
        lblTotal.setText(String.valueOf(tasks.size()));
        lblNormal.setText(String.valueOf(tasks.stream().filter(t->!t.isUrgent()).count()));
        lblUrgent.setText(String.valueOf(tasks.stream().filter(Task::isUrgent).count()));
        lblAvg.setText(String.format(Locale.US,"%.1f ms",avg));

        resultCenter.revalidate(); resultCenter.repaint();
        ok("Simulation terminée — "+tasks.size()+" tâches en "+dur+" ms.");
    }

    // UI HELPERS
    private JPanel card(String title) {
        JPanel p=new JPanel(new BorderLayout(6,10)); p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORD,1),new EmptyBorder(14,14,14,14)));
        JPanel hdr=new JPanel(new BorderLayout()); hdr.setBackground(WHITE);
        hdr.setBorder(new MatteBorder(0,0,1,0,BORD));
        JLabel l=new JLabel(title); l.setFont(FH); l.setForeground(NAVY);
        l.setBorder(new EmptyBorder(0,0,8,0));
        hdr.add(l,BorderLayout.WEST); p.add(hdr,BorderLayout.NORTH);
        return p;
    }

    private JTextField field() {
        JTextField f=new JTextField(); f.setFont(FB); f.setForeground(NAVY);
        f.setBackground(SURF);
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORD,1),new EmptyBorder(5,8,5,8)));
        return f;
    }

    private JLabel lbl(String txt) {
        JLabel l=new JLabel(txt); l.setFont(FL); l.setForeground(new Color(51,65,85));
        return l;
    }

    private JButton btn(String txt, Color bg) {
        JButton b=new JButton(txt); b.setFont(FBT); b.setBackground(bg);
        b.setForeground(WHITE); b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(7,12,7,12));
        Color hov=bg.darker();
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){b.setBackground(hov);}
            public void mouseExited(MouseEvent e) {b.setBackground(bg);}
        });
        return b;
    }

    private JTable mkTable(DefaultTableModel model) {
        JTable t=new JTable(model); t.setFont(FB); t.setRowHeight(28);
        t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0,1));
        t.setBackground(WHITE); t.setForeground(NAVY);
        t.setSelectionBackground(BLUE_LT); t.setSelectionForeground(new Color(30,58,138));
        t.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable tbl,Object v,
                    boolean sel,boolean foc,int row,int col){
                JLabel l=new JLabel(v==null?"":v.toString()); l.setOpaque(true);
                l.setHorizontalAlignment(CENTER); l.setFont(FL);
                l.setBackground(NAVY); l.setForeground(WHITE);
                l.setBorder(new EmptyBorder(4,6,4,6)); return l;
            }
        });
        t.getTableHeader().setPreferredSize(new Dimension(0,30));
        return t;
    }

    private void ok(String msg){statusLabel.setText("✓  "+msg);statusLabel.setForeground(GREEN);}
    private void err(String msg){
        statusLabel.setText("⚠  "+msg); statusLabel.setForeground(RED);
        JOptionPane.showMessageDialog(this,msg,"Erreur",JOptionPane.ERROR_MESSAGE);
    }

    // main
    public static void main(String[] args) {
        try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
        catch(Exception ignored){}
        SwingUtilities.invokeLater(()->new ProjetGC().setVisible(true));
    }
}