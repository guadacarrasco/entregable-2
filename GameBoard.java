import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;

/**
 * Clase que representa el tablero del juego con sincronización thread-safe
 */
public class GameBoard {
    private final int size;
    private final Cell[][] board;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    
    // Estadísticas del tablero
    private int totalCoins = 0;
    private int totalLives = 0;
    private int totalTraps = 0;
    private final int maxCoins = 10; // 10% del total de casillas
    private final int maxTraps = 10; // 10% del total de casillas
    
    public GameBoard(int size) {
        this.size = size;
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
            if (board[x][y].isFree()) {
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
    
    public void display() {
        readLock.lock();
        try {
            System.out.println("\n" + "=".repeat(size * 4 + 1));
            for (int i = 0; i < size; i++) {
                System.out.print("|");
                for (int j = 0; j < size; j++) {
                    System.out.printf("%3s|", board[i][j].toString());
                }
                System.out.println();
                System.out.println("=".repeat(size * 4 + 1));
            }
            System.out.println("Leyenda: P=Jugador, C=Monedas, L=Vida, T=Trampa, .=Libre");
            System.out.printf("Estadísticas: Monedas: %d/%d, Vidas: %d, Trampas: %d/%d\n", 
                            totalCoins, maxCoins, totalLives, totalTraps, maxTraps);
        } finally {
            readLock.unlock();
        }
    }
    
    public int getSize() { return size; }
    public int getMaxCoins() { return maxCoins; }
    public int getMaxTraps() { return maxTraps; }
    public int getTotalCoins() { return totalCoins; }
    public int getTotalLives() { return totalLives; }
    public int getTotalTraps() { return totalTraps; }
    
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
