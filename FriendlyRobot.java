import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.Random;

/**
 * Clase base para los robots amigables
 */
public abstract class FriendlyRobot implements Runnable {
    protected final GameBoard board;
    protected final Random random;
    protected volatile boolean isRunning;
    protected final int minWaitTime;
    protected final int maxWaitTime;
    
    public FriendlyRobot(GameBoard board, int minWaitTime, int maxWaitTime) {
        this.board = board;
        this.random = new Random();
        this.isRunning = true;
        this.minWaitTime = minWaitTime;
        this.maxWaitTime = maxWaitTime;
    }
    
    @Override
    public void run() {
        try {
            while (isRunning) {
                performAction();
                
                // Espera aleatoria entre acciones
                int waitTime = minWaitTime + random.nextInt(maxWaitTime - minWaitTime + 1);
                Thread.sleep(waitTime);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(getRobotName() + " interrumpido");
        }
    }
    
    protected abstract void performAction();
    protected abstract String getRobotName();
    
    public void stop() {
        this.isRunning = false;
    }
    
    protected boolean findRandomFreePosition() {
        int attempts = 0;
        int maxAttempts = board.getSize() * board.getSize() * 2;
        
        while (attempts < maxAttempts) {
            int x = random.nextInt(board.getSize());
            int y = random.nextInt(board.getSize());
            
            if (board.isCellFree(x, y)) {
                return true;
            }
            attempts++;
        }
        return false;
    }
}

/**
 * Robot que coloca vidas en el tablero
 */
class LifeRobot extends FriendlyRobot {
    private static final int X_MIN = 2000; // 2 segundos
    private static final int X_MAX = 5000; // 5 segundos
    private static final int MAX_LIVES = 10; // Máximo de vidas en el tablero
    
    public LifeRobot(GameBoard board) {
        super(board, X_MIN, X_MAX);
    }
    
    @Override
    protected void performAction() {
        // Verificar si ya hay el máximo de vidas en el tablero
        if (board.getTotalLives() >= MAX_LIVES) {
            System.out.println("Robot de Vidas: Esperando a que se tomen vidas del tablero...");
            return;
        }
        
        // Buscar posición libre para colocar vida
        int attempts = 0;
        int maxAttempts = board.getSize() * board.getSize();
        
        while (attempts < maxAttempts) {
            int x = random.nextInt(board.getSize());
            int y = random.nextInt(board.getSize());
            
            if (board.addLife(x, y)) {
                System.out.println("Robot de Vidas colocó una vida en (" + x + "," + y + ")");
                return;
            }
            attempts++;
        }
        
        System.out.println("Robot de Vidas: No encontró casillas libres para colocar vida");
    }
    
    @Override
    protected String getRobotName() {
        return "Robot de Vidas";
    }
}

/**
 * Robot que coloca monedas en el tablero
 */
class CoinRobot extends FriendlyRobot {
    private static final int Y_MIN = 1500; // 1.5 segundos
    private static final int Y_MAX = 4000; // 4 segundos
    private final int[] coinValues = {1, 2, 5, 10};
    
    public CoinRobot(GameBoard board) {
        super(board, Y_MIN, Y_MAX);
    }
    
    @Override
    protected void performAction() {
        // Verificar si ya hay el máximo de casillas con monedas (10% del total)
        if (board.getTotalCoins() >= board.getMaxCoins()) {
            System.out.println("Robot de Monedas: Esperando a que se liberen casillas...");
            return;
        }
        
        // Generar cantidad aleatoria de monedas
        int coinAmount = coinValues[random.nextInt(coinValues.length)];
        
        // Buscar posición libre para colocar monedas
        int attempts = 0;
        int maxAttempts = board.getSize() * board.getSize();
        
        while (attempts < maxAttempts) {
            int x = random.nextInt(board.getSize());
            int y = random.nextInt(board.getSize());
            
            if (board.addCoins(x, y, coinAmount)) {
                System.out.println("Robot de Monedas colocó " + coinAmount + " monedas en (" + x + "," + y + ")");
                return;
            }
            attempts++;
        }
        
        System.out.println("Robot de Monedas: No encontró casillas libres para colocar monedas");
    }
    
    @Override
    protected String getRobotName() {
        return "Robot de Monedas";
    }
}
