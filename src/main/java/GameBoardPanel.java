package entregable2;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel gráfico para mostrar el tablero del juego
 */
public class GameBoardPanel extends JPanel {
    private GameBoard board;
    private List<Player> players;

    public GameBoardPanel(GameBoard board, List<Player> players) {
        this.board = board;
        this.players = players;
        // No setPreferredSize: el tamaño será dinámico
    }

    public void updateBoard(GameBoard board, List<Player> players) {
        this.board = board;
        this.players = players;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int size = board.getSize();
        int cellSize = Math.max(10, Math.min(getWidth() / size, getHeight() / size));
        if (cellSize < 10 || getWidth() < size || getHeight() < size) {
            // No hay espacio suficiente para dibujar el tablero
            return;
        }
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int x = j * cellSize;
                int y = i * cellSize;
                String cellStr = board.getCellString(i, j);
                // Fondo base
                g.setColor(Color.WHITE);
                g.fillRect(x, y, cellSize, cellSize);
                g.setColor(Color.GRAY);
                g.drawRect(x, y, cellSize, cellSize);

                // Dibujar contenido intuitivo
                if (cellStr.startsWith("P")) {
                    int playerId = 1;
                    try { playerId = Integer.parseInt(cellStr.substring(1)); } catch(Exception e) {}
                    Color[] playerColors = {Color.BLUE, Color.MAGENTA, Color.ORANGE, Color.CYAN, Color.PINK};
                    g.setColor(playerColors[(playerId-1)%playerColors.length]);
                    g.fillOval(x+cellSize/10, y+cellSize/10, cellSize*8/10, cellSize*8/10);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, Math.max(12, cellSize/3)));
                    g.drawString(""+playerId, x+cellSize/2-5, y+cellSize/2+6);
                } else if (cellStr.startsWith("C")) {
                    g.setColor(new Color(212, 175, 55));
                    g.fillOval(x+cellSize/4, y+cellSize/4, cellSize/2, cellSize/2);
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Arial", Font.BOLD, Math.max(10, cellSize/4)));
                    String amount = cellStr.substring(1);
                    g.drawString(amount, x+cellSize/2-6, y+cellSize/2+5);
                } else if (cellStr.equals("L")) {
                    g.setColor(new Color(34, 177, 76));
                    int[] xPoints = {x+cellSize/2, x+cellSize/2-cellSize/5, x+cellSize/2, x+cellSize/2+cellSize/5};
                    int[] yPoints = {y+cellSize/2+cellSize/5, y+cellSize/2-cellSize/10, y+cellSize/2-cellSize/4, y+cellSize/2-cellSize/10};
                    g.fillPolygon(xPoints, yPoints, 4);
                    g.fillOval(x+cellSize/2-cellSize/5, y+cellSize/2-cellSize/4, cellSize/5, cellSize/5);
                    g.fillOval(x+cellSize/2, y+cellSize/2-cellSize/4, cellSize/5, cellSize/5);
                } else if (cellStr.equals("T")) {
                    g.setColor(Color.RED);
                    int[] xPoints = {x+cellSize/2, x+cellSize/5, x+cellSize-cellSize/5};
                    int[] yPoints = {y+cellSize/5, y+cellSize-cellSize/5, y+cellSize-cellSize/5};
                    g.fillPolygon(xPoints, yPoints, 3);
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Arial", Font.BOLD, Math.max(10, cellSize/4)));
                    g.drawString("!", x+cellSize/2-3, y+cellSize-cellSize/4);
                }
            }
        }
    }
}
