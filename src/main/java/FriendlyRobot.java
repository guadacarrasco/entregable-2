package entregable2;

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
            // Solo considerar casillas completamente libres para colocar premios
            if (board.getCellString(x, y).equals(".")) {
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
    public LifeRobot(GameBoard board, int minWaitTime, int maxWaitTime) {
        super(board, minWaitTime, maxWaitTime);
    }
    
    @Override
    protected void performAction() {
        GameLogger logger = GameLogger.getInstance();
        // Verificar si ya hay el máximo de vidas en el tablero
        if (board.getTotalLives() >= board.getMaxLives()) {
            logger.log("Robot de Vidas: Esperando a que se tomen vidas del tablero...");
            return;
        }

        // Buscar posición libre para colocar vida
        int attempts = 0;
        int maxAttempts = board.getSize() * board.getSize();

        while (attempts < maxAttempts) {
            int x = random.nextInt(board.getSize());
            int y = random.nextInt(board.getSize());
            // Solo colocar vida si la casilla está completamente libre
            if (board.getCellString(x, y).equals(".")) {
                if (board.addLife(x, y)) {
                    logger.log("Robot de Vidas colocó una vida en (" + x + "," + y + ")");
                    return;
                }
            }
            attempts++;
        }

        logger.log("Robot de Vidas: No encontró casillas libres para colocar vida");
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
    private final int[] coinValues = {1, 2, 5, 10};

    public CoinRobot(GameBoard board, int minWaitTime, int maxWaitTime) {
        super(board, minWaitTime, maxWaitTime);
    }
    
    @Override
    protected void performAction() {
        GameLogger logger = GameLogger.getInstance();
        // Verificar si ya hay el máximo de casillas con monedas (10% del total)
        if (board.getTotalCoins() >= board.getMaxCoins()) {
            logger.log("Robot de Monedas: Esperando a que se liberen casillas...");
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
                logger.log("Robot de Monedas colocó " + coinAmount + " monedas en (" + x + "," + y + ")");
                return;
            }
            attempts++;
        }

        logger.log("Robot de Monedas: No encontró casillas libres para colocar monedas");
    }
    
    @Override
    protected String getRobotName() {
        return "Robot de Monedas";
    }
}
