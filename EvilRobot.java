import java.util.concurrent.*;
import java.util.Random;

/**
 * Robot malo que coloca trampas en el tablero
 */
public class EvilRobot implements Runnable {
    private final GameBoard board;
    private final Random random;
    private volatile boolean isRunning;
    private static final int W_MIN = 3000; // 3 segundos
    private static final int W_MAX = 7000; // 7 segundos
    
    public EvilRobot(GameBoard board) {
        this.board = board;
        this.random = new Random();
        this.isRunning = true;
    }
    
    @Override
    public void run() {
        try {
            while (isRunning) {
                performAction();
                
                // Espera aleatoria entre colocación de trampas
                int waitTime = W_MIN + random.nextInt(W_MAX - W_MIN + 1);
                Thread.sleep(waitTime);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Robot Malo interrumpido");
        }
    }
    
    private void performAction() {
        // Verificar si ya hay el máximo de trampas (10% del total de casillas)
        if (board.getTotalTraps() >= board.getMaxTraps()) {
            System.out.println("Robot Malo: Ya colocó el máximo de trampas. Terminando labor.");
            isRunning = false;
            return;
        }
        
        // Buscar posición libre para colocar trampa
        int attempts = 0;
        int maxAttempts = board.getSize() * board.getSize() * 2;
        
        while (attempts < maxAttempts) {
            int x = random.nextInt(board.getSize());
            int y = random.nextInt(board.getSize());
            
            // La casilla debe estar libre (sin jugadores ni premios)
            if (board.isCellFree(x, y)) {
                if (board.addTrap(x, y)) {
                    System.out.println("Robot Malo colocó una trampa en (" + x + "," + y + ")");
                    return;
                }
            }
            attempts++;
        }
        
        System.out.println("Robot Malo: No encontró casillas libres para colocar trampa");
    }
    
    public void stop() {
        this.isRunning = false;
    }
}
