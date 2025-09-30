package entregable2;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Ventana principal para mostrar el juego
 */
public class GameWindow extends JFrame {
    private GameBoardPanel boardPanel;
    private GameInfoPanel infoPanel;

    public GameWindow(GameBoard board, List<Player> players) {
        setTitle("Juego Concurrente - Tablero");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        boardPanel = new GameBoardPanel(board, players);
        infoPanel = new GameInfoPanel();
        add(boardPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.EAST);
        int boardSize = board.getSize();
        int cellSize = 40; // Tamaño cómodo para ver todo
        int infoPanelWidth = 350;
        int width = boardSize * cellSize + infoPanelWidth;
        int height = Math.max(boardSize * cellSize, 500);
        setPreferredSize(new Dimension(width, height));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void updateBoard(GameBoard board, List<Player> players) {
        boardPanel.updateBoard(board, players);
    }

    public void setStats(String stats) {
        infoPanel.setStats(stats);
    }

    public void updateStats(String stats) {
        infoPanel.setStats(stats);
    }

    public void appendLog(String log) {
        // Método obsoleto, usar appendCategorizedLogs
    }

    public void clearLogs() {
        infoPanel.clearLogs();
    }

    public void appendCategorizedLogs(java.util.List<String> logs) {
        infoPanel.appendCategorizedLogs(logs);
    }
}
