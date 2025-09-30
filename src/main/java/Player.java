
package entregable2;
import java.util.Random;

public class Player implements Runnable {
    private final int id;
    private int x, y;
    private int lives;
    private int coins;
    private final GameBoard board;
    private final Random random;
    private volatile boolean isAlive;
    private volatile boolean gameRunning;
    private static final int Z_MIN = 1000;
    private static final int Z_MAX = 3000;
    private final Runnable uiUpdateCallback;

    public Player(int id, GameBoard board, int vidasIniciales) {
        this(id, board, vidasIniciales, null);
    }

    public Player(int id, GameBoard board, int vidasIniciales, Runnable uiUpdateCallback) {
        this.id = id;
        this.board = board;
        this.lives = vidasIniciales;
        this.coins = 0;
        this.isAlive = true;
        this.gameRunning = true;
        this.random = new Random();
        this.uiUpdateCallback = uiUpdateCallback;
        this.x = random.nextInt(board.getSize());
        this.y = random.nextInt(board.getSize());
    }

    @Override
    public void run() {
        GameLogger logger = GameLogger.getInstance();
        try {
            while (gameRunning && isAlive) {
                if (board.isPositionValid(x, y)) {
                    makeMove();
                }
                int waitTime = Z_MIN + random.nextInt(Z_MAX - Z_MIN + 1);
                Thread.sleep(waitTime);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("Jugador " + id + " interrumpido");
        }
    }

    private void makeMove() {
        GameLogger logger = GameLogger.getInstance();
        int dice = random.nextInt(6) + 1;
        // Elegir una dirección aleatoria: 0=izq, 1=der, 2=arriba, 3=abajo, 4=diag izq-arriba, 5=diag der-abajo, 6=diag izq-abajo, 7=diag der-arriba
        int dir = random.nextInt(8);
        int dx = 0, dy = 0;
        switch (dir) {
            case 0: dx = -1; dy = 0; break; // izquierda
            case 1: dx = 1; dy = 0; break; // derecha
            case 2: dx = 0; dy = -1; break; // arriba
            case 3: dx = 0; dy = 1; break; // abajo
            case 4: dx = -1; dy = -1; break; // diag izq-arriba
            case 5: dx = 1; dy = 1; break; // diag der-abajo
            case 6: dx = -1; dy = 1; break; // diag izq-abajo
            case 7: dx = 1; dy = -1; break; // diag der-arriba
        }
        int currX = x;
        int currY = y;
        boolean muerto = false;
        StringBuilder recorrido = new StringBuilder();
        recorrido.append("(" + currX + "," + currY + ")");
        for (int paso = 0; paso < dice; paso++) {
            int nextX = currX + dx;
            int nextY = currY + dy;
            // Limitar a los bordes
            if (nextX < 0 || nextX >= board.getSize() || nextY < 0 || nextY >= board.getSize()) break;
            // No puede pasar por casillas ocupadas
            if (!board.isCellFree(nextX, nextY)) break;
            board.getWriteLock().lock();
            try {
                board.freeCell(currX, currY);
                board.occupyCell(nextX, nextY, this);
                this.x = currX = nextX;
                this.y = currY = nextY;
                int collectedCoins = board.collectCoins(currX, currY);
                if (collectedCoins > 0) {
                    coins += collectedCoins;
                    logger.log("Jugador " + id + " recolectó " + collectedCoins + " monedas! Total: " + coins);
                }
                if (board.collectLife(currX, currY)) {
                    lives++;
                    logger.log("Jugador " + id + " recolectó una vida! Total: " + lives);
                }
                if (board.hasTrap(currX, currY)) {
                    lives--;
                    logger.log("Jugador " + id + " cayó en una trampa! Perdió una vida. Vidas restantes: " + lives);
                    if (lives <= 0) {
                        isAlive = false;
                        logger.log("Jugador " + id + " ha muerto!");
                        muerto = true;
                    }
                }
            } finally {
                board.getWriteLock().unlock();
            }
            recorrido.append(" -> (" + currX + "," + currY + ")");
            if (uiUpdateCallback != null) {
                javax.swing.SwingUtilities.invokeLater(uiUpdateCallback);
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            if (muerto) return;
        }
        logger.log("Jugador " + id + " se movió con dado " + dice + ": " + recorrido.toString());
    }

    private void applyCellActions() {
        GameLogger logger = GameLogger.getInstance();
        int collectedCoins = board.collectCoins(x, y);
        if (collectedCoins > 0) {
            coins += collectedCoins;
            logger.log("Jugador " + id + " recolectó " + collectedCoins + " monedas! Total: " + coins);
        }
        if (board.collectLife(x, y)) {
            lives++;
            logger.log("Jugador " + id + " recolectó una vida! Total: " + lives);
        }
        if (board.hasTrap(x, y)) {
            lives--;
            logger.log("Jugador " + id + " cayó en una trampa! Perdió una vida. Vidas restantes: " + lives);
            if (lives <= 0) {
                isAlive = false;
                logger.log("Jugador " + id + " ha muerto!");
            }
        }
    }

    public void stopGame() {
        this.gameRunning = false;
    }

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
