package entregable2;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

/**
 * Clase que representa el tablero del juego con sincronización thread-safe
 */
public class GameBoard {
    public String getCellString(int i, int j) {
        readLock.lock();
        try {
            return board[i][j].toString();
        } finally {
            readLock.unlock();
        }
    }
    private final int size;
    private final Cell[][] board;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    // Estadísticas del tablero
    private int totalCoins = 0;
    private int totalLives = 0;
    private int totalTraps = 0;
    private final int maxCoins;
    private final int maxTraps;
    private final int maxLives;

    public GameBoard(int size, int maxTraps, int maxLives, int maxCoins) {
        this.size = size;
        this.maxTraps = maxTraps;
        this.maxLives = maxLives;
        this.maxCoins = maxCoins;
        this.board = new Cell[size][size];
        initializeBoard();
    }
    
    private void initializeBoard() {
        writeLock.lock();
        try {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    board[i][j] = new Cell();
                }
            }
        } finally {
            writeLock.unlock();
        }
    }
    
    public boolean isPositionValid(int x, int y) {
        return x >= 0 && x < size && y >= 0 && y < size;
    }
    
    public boolean isCellFree(int x, int y) {
        readLock.lock();
        try {
            return board[x][y].isFree();
        } finally {
            readLock.unlock();
        }
    }
    
    public boolean occupyCell(int x, int y, Player player) {
        writeLock.lock();
        try {
            // Eliminar al jugador de cualquier otra casilla antes de ocupar la nueva
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    Cell cell = board[i][j];
                    if (cell.occupant == player) {
                        cell.setOccupant(null);
                    }
                }
            }
            if (board[x][y].isFree()) {
                board[x][y].setOccupant(player);
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }
    
    public void freeCell(int x, int y) {
        writeLock.lock();
        try {
            board[x][y].setOccupant(null);
        } finally {
            writeLock.unlock();
        }
    }
    
    public boolean addCoins(int x, int y, int amount) {
        writeLock.lock();
        try {
            if (totalCoins < maxCoins && board[x][y].isFree()) {
                board[x][y].addCoins(amount);
                totalCoins++;
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }
    
    public boolean addLife(int x, int y) {
        writeLock.lock();
        try {
            if (totalLives < maxLives && board[x][y].isFree()) {
                board[x][y].addLife();
                totalLives++;
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }
    
    public boolean addTrap(int x, int y) {
        writeLock.lock();
        try {
            if (totalTraps < maxTraps && board[x][y].isFree()) {
                board[x][y].addTrap();
                totalTraps++;
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }
    
    public int collectCoins(int x, int y) {
        writeLock.lock();
        try {
            int coins = board[x][y].getCoins();
            if (coins > 0) {
                board[x][y].removeCoins();
                totalCoins--;
            }
            return coins;
        } finally {
            writeLock.unlock();
        }
    }
    
    public boolean collectLife(int x, int y) {
        writeLock.lock();
        try {
            if (board[x][y].hasLife()) {
                board[x][y].removeLife();
                totalLives--;
                return true;
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }
    
    public boolean hasTrap(int x, int y) {
        readLock.lock();
        try {
            return board[x][y].hasTrap();
        } finally {
            readLock.unlock();
        }
    }
    
    public void display(List<Player> players) {
        readLock.lock();
        try {
            AnsiConsole.systemInstall();
            String horizontalBorder = Ansi.ansi().fgBright(Ansi.Color.WHITE).a("╔").reset().toString();
            for (int k = 0; k < size; k++) horizontalBorder += Ansi.ansi().fgBright(Ansi.Color.WHITE).a("═══").reset();
            horizontalBorder += Ansi.ansi().fgBright(Ansi.Color.WHITE).a("╗").reset();
            System.out.println("\n" + horizontalBorder);
            for (int i = 0; i < size; i++) {
                StringBuilder row = new StringBuilder();
                row.append(Ansi.ansi().fgBright(Ansi.Color.WHITE).a("║").reset());
                for (int j = 0; j < size; j++) {
                    String cellStr = board[i][j].toString();
                    Ansi ansiCell = Ansi.ansi();
                    if (cellStr.startsWith("P")) {
                        ansiCell.bg(Ansi.Color.BLUE).fgBright(Ansi.Color.WHITE).a(String.format("%3s", cellStr)).reset();
                    } else if (cellStr.startsWith("C")) {
                        ansiCell.bg(Ansi.Color.YELLOW).fgBright(Ansi.Color.BLACK).a(String.format("%3s", cellStr)).reset();
                    } else if (cellStr.equals("L")) {
                        ansiCell.bg(Ansi.Color.GREEN).fgBright(Ansi.Color.BLACK).a(String.format("%3s", cellStr)).reset();
                    } else if (cellStr.equals("T")) {
                        ansiCell.bg(Ansi.Color.RED).fgBright(Ansi.Color.WHITE).a(String.format("%3s", cellStr)).reset();
                    } else {
                        ansiCell.bg(Ansi.Color.BLACK).fgBright(Ansi.Color.WHITE).a(String.format("%3s", cellStr)).reset();
                    }
                    row.append(ansiCell);
                }
                row.append(Ansi.ansi().fgBright(Ansi.Color.WHITE).a("║").reset());
                System.out.println(row);
            }
            String bottomBorder = Ansi.ansi().fgBright(Ansi.Color.WHITE).a("╚").reset().toString();
            for (int k = 0; k < size; k++) bottomBorder += Ansi.ansi().fgBright(Ansi.Color.WHITE).a("═══").reset();
            bottomBorder += Ansi.ansi().fgBright(Ansi.Color.WHITE).a("╝").reset();
            System.out.println(bottomBorder);

            System.out.println("Leyenda: " + Ansi.ansi().fg(Ansi.Color.BLUE).a("P=Jugador").reset() + ", "
                + Ansi.ansi().fg(Ansi.Color.YELLOW).a("C=Monedas").reset() + ", "
                + Ansi.ansi().fg(Ansi.Color.GREEN).a("L=Vida").reset() + ", "
                + Ansi.ansi().fg(Ansi.Color.RED).a("T=Trampa").reset() + ", "
                + Ansi.ansi().fg(Ansi.Color.WHITE).a(".=Libre").reset());
            System.out.printf("%sEstadísticas:%s Monedas: %s%d%s/%s%d%s, Vidas: %s%d%s, Trampas: %s%d%s/%s%d%s\n",
                Ansi.ansi().fg(Ansi.Color.CYAN), Ansi.ansi().reset(),
                Ansi.ansi().fg(Ansi.Color.YELLOW), totalCoins, Ansi.ansi().reset(),
                Ansi.ansi().fg(Ansi.Color.YELLOW), maxCoins, Ansi.ansi().reset(),
                Ansi.ansi().fg(Ansi.Color.GREEN), totalLives, Ansi.ansi().reset(),
                Ansi.ansi().fg(Ansi.Color.RED), totalTraps, Ansi.ansi().reset(),
                Ansi.ansi().fg(Ansi.Color.RED), maxTraps, Ansi.ansi().reset());
            // Estadísticas individuales de jugadores
            System.out.println(Ansi.ansi().fgBright(Ansi.Color.MAGENTA).a("\n--- Jugadores ---").reset());
            for (Player player : players) {
                String estado = player.isAlive() ? "VIVO" : "MUERTO";
                Ansi color = player.isAlive() ? Ansi.ansi().fg(Ansi.Color.BLUE) : Ansi.ansi().fg(Ansi.Color.RED);
                System.out.println(color.a("Jugador " + player.getId() + ": ")
                    .reset().a("Monedas: ")
                    .fg(Ansi.Color.YELLOW).a(player.getCoins()).reset()
                    .a(", Vidas: ")
                    .fg(Ansi.Color.GREEN).a(player.getLives()).reset()
                    .a(", Estado: ")
                    .fg(player.isAlive() ? Ansi.Color.GREEN : Ansi.Color.RED).a(estado).reset());
            }
            AnsiConsole.systemUninstall();
        } finally {
            readLock.unlock();
        }
    }
    
    public int getSize() { return size; }
    public int getMaxCoins() { return maxCoins; }
    public int getMaxTraps() { return maxTraps; }
    public int getMaxLives() { return maxLives; }
    public int getTotalCoins() { return totalCoins; }
    public int getTotalLives() { return totalLives; }
    public int getTotalTraps() { return totalTraps; }
    public Lock getWriteLock() {
        return writeLock;
    }
    
    /**
     * Clase interna que representa una casilla del tablero
     */
    private static class Cell {
        private Player occupant;
        private int coins;
        private boolean hasLife;
        private boolean hasTrap;
        
        public Cell() {
            this.occupant = null;
            this.coins = 0;
            this.hasLife = false;
            this.hasTrap = false;
        }
        
        // Una casilla está completamente libre si no tiene jugador, ni vida, ni monedas, ni trampa
        public boolean isCompletelyFree() {
            return occupant == null && coins == 0 && !hasLife && !hasTrap;
        }
        // Para compatibilidad, si hay código que usa isFree() para movimiento, dejamos:
        public boolean isFree() {
            return occupant == null;
        }
        
        public void setOccupant(Player player) {
            this.occupant = player;
        }
        
        public void addCoins(int amount) {
            this.coins += amount;
        }
        
        public int getCoins() {
            return coins;
        }
        
        public void removeCoins() {
            this.coins = 0;
        }
        
        public void addLife() {
            this.hasLife = true;
        }
        
        public boolean hasLife() {
            return hasLife;
        }
        
        public void removeLife() {
            this.hasLife = false;
        }
        
        public void addTrap() {
            this.hasTrap = true;
        }
        
        public boolean hasTrap() {
            return hasTrap;
        }
        
        @Override
        public String toString() {
            if (occupant != null) {
                return "P" + occupant.getId();
            } else if (coins > 0) {
                return "C" + coins;
            } else if (hasLife) {
                return "L";
            } else if (hasTrap) {
                return "T";
            } else {
                return ".";
            }
        }
    }
}
