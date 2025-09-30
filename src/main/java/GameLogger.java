
package entregable2;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.BlockingQueue;


/**
 * Clase para manejar el logging del juego de forma thread-safe
 */
public class GameLogger {
    private BlockingQueue<String> displayEventQueue = null;

    public void setDisplayEventQueue(BlockingQueue<String> queue) {
        this.displayEventQueue = queue;
    }
    private static GameLogger instance;
    private final ReentrantLock lock = new ReentrantLock();
    private PrintWriter writer;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String lastLog = "";
    private final List<String> logs = new ArrayList<>();
    
    private GameLogger() {
        try {
            // Crear archivo de log con timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "game_log_" + timestamp + ".txt";
            writer = new PrintWriter(new FileWriter(filename, true));
            log("=== INICIO DE NUEVA PARTIDA ===");
        } catch (IOException e) {
            System.err.println("Error al crear archivo de log: " + e.getMessage());
        }
    }
    
    public static GameLogger getInstance() {
        if (instance == null) {
            synchronized (GameLogger.class) {
                if (instance == null) {
                    instance = new GameLogger();
                }
            }
        }
        return instance;
    }
    
    public void log(String message) {
        lock.lock();
        try {
            String timestamp = LocalDateTime.now().format(formatter);
            String logMessage = "[" + timestamp + "] " + message;
            System.out.println(logMessage);
            if (writer != null) {
                writer.println(logMessage);
                writer.flush();
            }
            lastLog = logMessage;
            logs.add(logMessage); // Agregar el log a la lista
            // Agregar a la cola de eventos si está configurada
            if (displayEventQueue != null) {
                displayEventQueue.offer(logMessage);
            }
        } finally {
            lock.unlock();
        }
    }
    
    public void logPlayerAction(int playerId, String action) {
        log("JUGADOR " + playerId + ": " + action);
    }
    
    public void logRobotAction(String robotName, String action) {
        log(robotName + ": " + action);
    }
    
    public void logGameEvent(String event) {
        log("EVENTO: " + event);
    }
    
    public void logGameEnd(Player[] players) {
        log("=== FIN DE PARTIDA ===");
        log("RESULTADOS FINALES:");
        
        for (Player player : players) {
            if (player != null) {
                log("Jugador " + player.getId() + 
                    " - Vidas: " + player.getLives() + 
                    ", Monedas: " + player.getCoins() + 
                    ", Estado: " + (player.isAlive() ? "VIVO" : "MUERTO"));
            }
        }
        
        // Determinar ganador
        Player winner = findWinner(players);
        if (winner != null) {
            log("GANADOR: Jugador " + winner.getId() + " con " + winner.getCoins() + " monedas");
        } else {
            log("NO HAY GANADOR - Todos los jugadores murieron");
        }
        
        log("=== FIN DEL LOG ===");
    }
    
    private Player findWinner(Player[] players) {
        Player winner = null;
        int maxCoins = -1;
        
        for (Player player : players) {
            if (player != null && player.isAlive() && player.getCoins() > maxCoins) {
                winner = player;
                maxCoins = player.getCoins();
            }
        }
        
        return winner;
    }
    
    public void close() {
        lock.lock();
        try {
            if (writer != null) {
                writer.close();
            }
        } finally {
            lock.unlock();
        }
    }
    
    public String getLastLog() {
        return lastLog;
    }
    
    public List<String> getLogs() {
        return logs;
    }
}
