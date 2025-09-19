import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.*;
import java.util.List;

/**
 * Clase principal que maneja el juego concurrente
 */
public class ConcurrentGame {
    private final GameBoard board;
    private final GameLogger logger;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;
    private final CountDownLatch gameStartLatch;
    private final CountDownLatch gameEndLatch;
    private final ReentrantLock gameStateLock;
    
    // Configuración del juego
    private final int boardSize;
    private final int minPlayers;
    private final int gameDuration; // en segundos
    private final int displayUpdateInterval; // en milisegundos
    
    // Estado del juego
    private volatile boolean gameRunning;
    private volatile boolean gameStarted;
    private final List<Player> players;
    private final List<Thread> playerThreads;
    private Thread displayThread;
    private Thread lifeRobotThread;
    private Thread coinRobotThread;
    private Thread evilRobotThread;
    
    // Robots
    private LifeRobot lifeRobot;
    private CoinRobot coinRobot;
    private EvilRobot evilRobot;
    
    public ConcurrentGame(int boardSize, int minPlayers, int gameDuration) {
        this.boardSize = boardSize;
        this.minPlayers = minPlayers;
        this.gameDuration = gameDuration;
        this.displayUpdateInterval = 2000; // Actualizar cada 2 segundos
        
        this.board = new GameBoard(boardSize);
        this.logger = GameLogger.getInstance();
        this.executor = Executors.newCachedThreadPool();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.gameStartLatch = new CountDownLatch(1);
        this.gameEndLatch = new CountDownLatch(1);
        this.gameStateLock = new ReentrantLock();
        
        this.gameRunning = false;
        this.gameStarted = false;
        this.players = new ArrayList<>();
        this.playerThreads = new ArrayList<>();
        
        // Inicializar robots
        this.lifeRobot = new LifeRobot(board);
        this.coinRobot = new CoinRobot(board);
        this.evilRobot = new EvilRobot(board);
    }
    
    /**
     * Agrega un jugador al juego
     */
    public boolean addPlayer() {
        gameStateLock.lock();
        try {
            if (gameStarted) {
                logger.log("No se pueden agregar jugadores durante una partida en curso");
                return false;
            }
            
            Player player = new Player(players.size() + 1, board);
            players.add(player);
            
            logger.log("Jugador " + player.getId() + " se unió al juego. Total: " + players.size() + "/" + minPlayers);
            
            // Verificar si se puede comenzar el juego
            if (players.size() >= minPlayers && !gameStarted) {
                startGame();
            }
            
            return true;
        } finally {
            gameStateLock.unlock();
        }
    }
    
    /**
     * Inicia el juego
     */
    private void startGame() {
        gameStateLock.lock();
        try {
            if (gameStarted) return;
            
            gameStarted = true;
            gameRunning = true;
            
            logger.logGameEvent("Iniciando partida con " + players.size() + " jugadores");
            logger.logGameEvent("Duración: " + gameDuration + " segundos");
            
            // Posicionar jugadores en el tablero
            for (Player player : players) {
                // Buscar posición libre
                boolean positioned = false;
                int attempts = 0;
                while (!positioned && attempts < 100) {
                    int x = (int) (Math.random() * boardSize);
                    int y = (int) (Math.random() * boardSize);
                    if (board.occupyCell(x, y, player)) {
                        positioned = true;
                        logger.logPlayerAction(player.getId(), "Posicionado en (" + x + "," + y + ")");
                    }
                    attempts++;
                }
            }
            
            // Iniciar hilos de jugadores
            for (Player player : players) {
                Thread playerThread = new Thread(player);
                playerThreads.add(playerThread);
                playerThread.start();
            }
            
            // Iniciar robots
            lifeRobotThread = new Thread(lifeRobot);
            coinRobotThread = new Thread(coinRobot);
            evilRobotThread = new Thread(evilRobot);
            
            lifeRobotThread.start();
            coinRobotThread.start();
            evilRobotThread.start();
            
            // Iniciar hilo de visualización
            startDisplayThread();
            
            // Programar fin del juego por tiempo
            scheduler.schedule(() -> {
                endGameByTime();
            }, gameDuration, TimeUnit.SECONDS);
            
            // Señalar que el juego ha comenzado
            gameStartLatch.countDown();
            
            logger.logGameEvent("¡Juego iniciado!");
            
        } finally {
            gameStateLock.unlock();
        }
    }
    
    /**
     * Hilo de visualización del tablero
     */
    private void startDisplayThread() {
        displayThread = new Thread(() -> {
            try {
                while (gameRunning) {
                    board.display();
                    Thread.sleep(displayUpdateInterval);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        displayThread.start();
    }
    
    /**
     * Termina el juego por tiempo
     */
    private void endGameByTime() {
        gameStateLock.lock();
        try {
            if (!gameRunning) return;
            
            gameRunning = false;
            logger.logGameEvent("Tiempo agotado - Finalizando partida");
            endGame();
        } finally {
            gameStateLock.unlock();
        }
    }
    
    /**
     * Termina el juego por victoria (solo un jugador vivo)
     */
    public void endGameByVictory() {
        gameStateLock.lock();
        try {
            if (!gameRunning) return;
            
            gameRunning = false;
            logger.logGameEvent("Solo queda un jugador vivo - Finalizando partida");
            endGame();
        } finally {
            gameStateLock.unlock();
        }
    }
    
    /**
     * Termina el juego
     */
    private void endGame() {
        // Detener jugadores
        for (Player player : players) {
            player.stopGame();
        }
        
        // Detener robots
        lifeRobot.stop();
        coinRobot.stop();
        evilRobot.stop();
        
        // Esperar a que terminen todos los hilos
        try {
            for (Thread thread : playerThreads) {
                thread.join(1000); // Esperar máximo 1 segundo
            }
            
            if (lifeRobotThread != null) lifeRobotThread.join(1000);
            if (coinRobotThread != null) coinRobotThread.join(1000);
            if (evilRobotThread != null) evilRobotThread.join(1000);
            if (displayThread != null) displayThread.join(1000);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Mostrar resultado final
        board.display();
        logger.logGameEnd(players.toArray(new Player[0]));
        
        // Señalar que el juego ha terminado
        gameEndLatch.countDown();
    }
    
    /**
     * Espera a que el juego termine
     */
    public void waitForGameEnd() throws InterruptedException {
        gameEndLatch.await();
    }
    
    /**
     * Verifica si el juego está en curso
     */
    public boolean isGameRunning() {
        return gameRunning;
    }
    
    /**
     * Verifica si el juego ha comenzado
     */
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    /**
     * Obtiene el número de jugadores vivos
     */
    public int getAlivePlayersCount() {
        int count = 0;
        for (Player player : players) {
            if (player.isAlive()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Cierra los recursos del juego
     */
    public void shutdown() {
        executor.shutdown();
        scheduler.shutdown();
        logger.close();
        
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Reinicia el juego para una nueva partida
     */
    public void resetGame() {
        gameStateLock.lock();
        try {
            // Limpiar estado anterior
            players.clear();
            playerThreads.clear();
            gameStarted = false;
            gameRunning = false;
            
            // Crear nuevo tablero
            // (En una implementación real, se recrearía el GameBoard)
            
            logger.logGameEvent("Juego reiniciado - Listo para nueva partida");
        } finally {
            gameStateLock.unlock();
        }
    }
}
