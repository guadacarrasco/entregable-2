package entregable2;

import java.util.concurrent.*;
import java.util.Random;

/**
 * Robot malo que coloca trampas en el tablero
 */
public class EvilRobot implements Runnable {
    private final GameBoard board;
    private final Random random;
    private volatile boolean isRunning;
    private final int wMin;
    private final int wMax;

    public EvilRobot(GameBoard board, int wMin, int wMax) {
        this.board = board;
        this.random = new Random();
        this.isRunning = true;
        this.wMin = wMin;
        this.wMax = wMax;
    }
    
    @Override
    public void run() {
        GameLogger logger = GameLogger.getInstance();
        try {
            while (isRunning) {
                performAction();

                // Espera aleatoria entre colocación de trampas
                int waitTime = wMin + random.nextInt(wMax - wMin + 1);
                Thread.sleep(waitTime);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("Robot Malo interrumpido");
        }
    }
    
    private void performAction() {
        GameLogger logger = GameLogger.getInstance();
        // Verificar si ya hay el máximo de trampas (10% del total de casillas)
        if (board.getTotalTraps() >= board.getMaxTraps()) {
            logger.log("Robot Malo: Ya colocó el máximo de trampas. Terminando labor.");
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
                    logger.log("Robot Malo colocó una trampa en (" + x + "," + y + ")");
                    return;
                }
            }
            attempts++;
        }

        logger.log("Robot Malo: No encontró casillas libres para colocar trampa");
    }
    
    public void stop() {
        this.isRunning = false;
    }
}
