import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.Random;

/**
 * Clase que representa un jugador del juego
 */
public class Player implements Runnable {
    private final int id;
    private int x, y;
    private int lives;
    private int coins;
    private final GameBoard board;
    private final Random random;
    private volatile boolean isAlive;
    private volatile boolean gameRunning;
    
    // Tiempos de espera entre jugadas
    private static final int Z_MIN = 1000; // 1 segundo
    private static final int Z_MAX = 3000; // 3 segundos
    
    public Player(int id, GameBoard board) {
        this.id = id;
        this.board = board;
        this.lives = 2; // Vidas iniciales
        this.coins = 0;
        this.isAlive = true;
        this.gameRunning = true;
        this.random = new Random();
        
        // Posición inicial aleatoria
        this.x = random.nextInt(board.getSize());
        this.y = random.nextInt(board.getSize());
    }
    
    @Override
    public void run() {
        try {
            while (gameRunning && isAlive) {
                if (board.isPositionValid(x, y)) {
                    makeMove();
                }
                
                // Espera aleatoria entre jugadas
                int waitTime = Z_MIN + random.nextInt(Z_MAX - Z_MIN + 1);
                Thread.sleep(waitTime);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Jugador " + id + " interrumpido");
        }
    }
    
    private void makeMove() {
        // Tirar dado (1-6)
        int dice = random.nextInt(6) + 1;
        
        // Calcular nueva posición basada en el dado
        int newX = x;
        int newY = y;
        
        // Movimiento basado en el dado
        switch (dice) {
            case 1: newX = Math.max(0, x - 1); break; // Izquierda
            case 2: newX = Math.min(board.getSize() - 1, x + 1); break; // Derecha
            case 3: newY = Math.max(0, y - 1); break; // Arriba
            case 4: newY = Math.min(board.getSize() - 1, y + 1); break; // Abajo
            case 5: // Diagonal arriba-izquierda
                newX = Math.max(0, x - 1);
                newY = Math.max(0, y - 1);
                break;
            case 6: // Diagonal abajo-derecha
                newX = Math.min(board.getSize() - 1, x + 1);
                newY = Math.min(board.getSize() - 1, y + 1);
                break;
        }
        
        // Verificar si la nueva posición es válida y está libre
        if (board.isPositionValid(newX, newY) && board.isCellFree(newX, newY)) {
            // Liberar posición actual
            board.freeCell(x, y);
            
            // Mover a nueva posición
            x = newX;
            y = newY;
            
            // Ocupar nueva casilla
            board.occupyCell(x, y, this);
            
            // Aplicar acciones de la casilla
            applyCellActions();
            
            System.out.println("Jugador " + id + " se movió a (" + x + "," + y + ") con dado " + dice);
        } else {
            System.out.println("Jugador " + id + " no pudo moverse a (" + newX + "," + newY + ") - casilla ocupada");
        }
    }
    
    private void applyCellActions() {
        // Recolectar monedas si las hay
        int collectedCoins = board.collectCoins(x, y);
        if (collectedCoins > 0) {
            coins += collectedCoins;
            System.out.println("Jugador " + id + " recolectó " + collectedCoins + " monedas! Total: " + coins);
        }
        
        // Recolectar vida si la hay
        if (board.collectLife(x, y)) {
            lives++;
            System.out.println("Jugador " + id + " recolectó una vida! Total: " + lives);
        }
        
        // Verificar trampa
        if (board.hasTrap(x, y)) {
            lives--;
            System.out.println("Jugador " + id + " cayó en una trampa! Perdió una vida. Vidas restantes: " + lives);
            
            if (lives <= 0) {
                isAlive = false;
                System.out.println("Jugador " + id + " ha muerto!");
            }
        }
    }
    
    public void stopGame() {
        this.gameRunning = false;
    }
    
    // Getters
    public int getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getLives() { return lives; }
    public int getCoins() { return coins; }
    public boolean isAlive() { return isAlive; }
    
    @Override
    public String toString() {
        return "Jugador " + id + " - Posición: (" + x + "," + y + "), Vidas: " + lives + ", Monedas: " + coins;
    }
}
