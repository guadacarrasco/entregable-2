package entregable2;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Panel para mostrar estadísticas y logs del juego
 */
public class GameInfoPanel extends JPanel {
    private JTextPane playerLogArea;
    private JTextPane robotLogArea;
    private JTextPane eventLogArea;
    private JTabbedPane tabbedPane;
    private JTextArea statsArea;
    private JCheckBox filterMov;
    private JCheckBox filterPremio;
    private JCheckBox filterTrampa;
    private JCheckBox filterMuerte;
    private JCheckBox filterRobot;
    private JCheckBox filterEvento;

    public GameInfoPanel() {
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
        playerLogArea = createLogPane();
        robotLogArea = createLogPane();
        eventLogArea = createLogPane();
        statsArea = new JTextArea(8, 30);
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.BOLD, 13));
        statsArea.setBackground(new Color(240, 255, 240));
        statsArea.setBorder(BorderFactory.createTitledBorder("Estadísticas"));
        tabbedPane.addTab("Jugadores", new JScrollPane(playerLogArea));
        tabbedPane.addTab("Robots", new JScrollPane(robotLogArea));
        tabbedPane.addTab("Eventos", new JScrollPane(eventLogArea));

        // Filtros
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterMov = new JCheckBox("Movimientos", true);
        filterPremio = new JCheckBox("Premios", true);
        filterTrampa = new JCheckBox("Trampas", true);
        filterMuerte = new JCheckBox("Muertes", true);
        filterRobot = new JCheckBox("Robots", true);
        filterEvento = new JCheckBox("Eventos", true);
        filterPanel.add(new JLabel("Filtrar: "));
        filterPanel.add(filterMov);
        filterPanel.add(filterPremio);
        filterPanel.add(filterTrampa);
        filterPanel.add(filterMuerte);
        filterPanel.add(filterRobot);
        filterPanel.add(filterEvento);
        add(statsArea, BorderLayout.NORTH);
        add(filterPanel, BorderLayout.SOUTH);
        add(tabbedPane, BorderLayout.CENTER);

        ActionListener filterListener = e -> refreshLogs();
        filterMov.addActionListener(filterListener);
        filterPremio.addActionListener(filterListener);
        filterTrampa.addActionListener(filterListener);
        filterMuerte.addActionListener(filterListener);
        filterRobot.addActionListener(filterListener);
        filterEvento.addActionListener(filterListener);
    }

    private JTextPane createLogPane() {
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setFont(new Font("Monospaced", Font.PLAIN, 13));
        return pane;
    }

    public void clearLogs() {
        playerLogArea.setText("");
        robotLogArea.setText("");
        eventLogArea.setText("");
    }

    private List<String> lastLogs = null;
    public void appendCategorizedLogs(java.util.List<String> logs) {
        lastLogs = logs;
        refreshLogs();
    }

    private void refreshLogs() {
        clearLogs();
        if (lastLogs == null) return;
        for (String log : lastLogs) {
            LogType type = getLogType(log);
            if (type == LogType.MOV && !filterMov.isSelected()) continue;
            if (type == LogType.PREMIO && !filterPremio.isSelected()) continue;
            if (type == LogType.TRAMPA && !filterTrampa.isSelected()) continue;
            if (type == LogType.MUERTE && !filterMuerte.isSelected()) continue;
            if (type == LogType.ROBOT && !filterRobot.isSelected()) continue;
            if (type == LogType.EVENTO && !filterEvento.isSelected()) continue;
            JTextPane pane = getPaneForType(type);
            appendStyledLog(pane, log, type);
        }
        playerLogArea.setCaretPosition(playerLogArea.getDocument().getLength());
        robotLogArea.setCaretPosition(robotLogArea.getDocument().getLength());
        eventLogArea.setCaretPosition(eventLogArea.getDocument().getLength());
    }

    private JTextPane getPaneForType(LogType type) {
        switch (type) {
            case MOV: return playerLogArea;
            case PREMIO: return playerLogArea;
            case TRAMPA: return playerLogArea;
            case MUERTE: return playerLogArea;
            case ROBOT: return robotLogArea;
            case EVENTO: return eventLogArea;
            default: return eventLogArea;
        }
    }

    private void appendStyledLog(JTextPane pane, String log, LogType type) {
        try {
            StyledDocument doc = pane.getStyledDocument();
            Style style = pane.addStyle("logStyle", null);
            switch (type) {
                case MOV:
                    StyleConstants.setForeground(style, Color.BLUE);
                    StyleConstants.setBold(style, false);
                    doc.insertString(doc.getLength(), "🟦 " + log + "\n", style);
                    break;
                case PREMIO:
                    StyleConstants.setForeground(style, new Color(218, 165, 32));
                    StyleConstants.setBold(style, true);
                    doc.insertString(doc.getLength(), "🪙 " + log + "\n", style);
                    break;
                case TRAMPA:
                    StyleConstants.setForeground(style, Color.MAGENTA);
                    StyleConstants.setBold(style, true);
                    doc.insertString(doc.getLength(), "⚠️ " + log + "\n", style);
                    break;
                case MUERTE:
                    StyleConstants.setForeground(style, Color.RED);
                    StyleConstants.setBold(style, true);
                    doc.insertString(doc.getLength(), "💀 " + log + "\n", style);
                    break;
                case ROBOT:
                    StyleConstants.setForeground(style, Color.DARK_GRAY);
                    StyleConstants.setBold(style, false);
                    doc.insertString(doc.getLength(), "🤖 " + log + "\n", style);
                    break;
                case EVENTO:
                    StyleConstants.setForeground(style, Color.GREEN.darker());
                    StyleConstants.setBold(style, true);
                    doc.insertString(doc.getLength(), "📢 " + log + "\n", style);
                    break;
                default:
                    doc.insertString(doc.getLength(), log + "\n", null);
            }
        } catch (Exception e) {
            // Ignorar errores de estilo
        }
    }

    private enum LogType { MOV, PREMIO, TRAMPA, MUERTE, ROBOT, EVENTO, OTRO }

    private LogType getLogType(String log) {
        String l = log.toLowerCase();
        // Si es de robot, siempre ROBOT
        if (l.contains("robot") || l.contains("malo")) return LogType.ROBOT;
        if (l.contains("ha muerto")) return LogType.MUERTE;
        if (l.contains("recolectó") || l.contains("colocó") || l.contains("premio")) return LogType.PREMIO;
        if (l.contains("se movió")) return LogType.MOV;
        if (l.contains("trampa")) return LogType.TRAMPA;
        if (l.contains("evento") || l.contains("juego iniciado") || l.contains("fin de partida") || l.contains("ganador")) return LogType.EVENTO;
        return LogType.OTRO;
    }


    public void setStats(String stats) {
        statsArea.setText(stats);
    }
}
