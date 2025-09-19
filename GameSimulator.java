import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Clase principal que ejecuta la simulación del juego
 */
public class GameSimulator {
    private static final int BOARD_SIZE = 10;
    private static final int MIN_PLAYERS = 3;
    private static final int GAME_DURATION = 60; // 60 segundos
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ConcurrentGame game = new ConcurrentGame(BOARD_SIZE, MIN_PLAYERS, GAME_DURATION);
        
        System.out.println("=== SIMULADOR DE JUEGO CONCURRENTE ===");
        System.out.println("Tablero: " + BOARD_SIZE + "x" + BOARD_SIZE);
        System.out.println("Jugadores mínimos: " + MIN_PLAYERS);
        System.out.println("Duración: " + GAME_DURATION + " segundos");
        System.out.println();
        
        try {
            // Agregar jugadores automáticamente para la simulación
            System.out.println("Agregando jugadores automáticamente...");
            for (int i = 0; i < MIN_PLAYERS; i++) {
                game.addPlayer();
                Thread.sleep(1000); // Esperar 1 segundo entre jugadores
            }
            
            // Agregar jugadores adicionales opcionales
            System.out.println("¿Desea agregar más jugadores? (s/n): ");
            String response = scanner.nextLine().toLowerCase();
            if (response.equals("s") || response.equals("si")) {
                System.out.println("¿Cuántos jugadores adicionales? (1-3): ");
                try {
                    int additionalPlayers = Integer.parseInt(scanner.nextLine());
                    for (int i = 0; i < Math.min(additionalPlayers, 3); i++) {
                        game.addPlayer();
                        Thread.sleep(1000);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Número inválido, continuando con " + MIN_PLAYERS + " jugadores");
                }
            }
            
            System.out.println("\n¡Juego iniciado! Presione Enter para ver el estado del juego...");
            scanner.nextLine();
            
            // Monitorear el juego
            monitorGame(game);
            
            // Esperar a que termine el juego
            game.waitForGameEnd();
            
            System.out.println("\n=== PARTIDA TERMINADA ===");
            System.out.println("¿Desea jugar otra partida? (s/n): ");
            String playAgain = scanner.nextLine().toLowerCase();
            
            if (playAgain.equals("s") || playAgain.equals("si")) {
                game.resetGame();
                // Reiniciar con nuevos jugadores
                for (int i = 0; i < MIN_PLAYERS; i++) {
                    game.addPlayer();
                    Thread.sleep(1000);
                }
                monitorGame(game);
                game.waitForGameEnd();
            }
            
        } catch (InterruptedException e) {
            System.out.println("Simulación interrumpida");
            Thread.currentThread().interrupt();
        } finally {
            game.shutdown();
            scanner.close();
        }
    }
    
    /**
     * Monitorea el estado del juego durante la ejecución
     */
    private static void monitorGame(ConcurrentGame game) throws InterruptedException {
        while (game.isGameRunning()) {
            Thread.sleep(5000); // Verificar cada 5 segundos
            
            int alivePlayers = game.getAlivePlayersCount();
            System.out.println("\n--- Estado del juego ---");
            System.out.println("Jugadores vivos: " + alivePlayers);
            
            // Verificar si solo queda un jugador vivo
            if (alivePlayers <= 1) {
                game.endGameByVictory();
                break;
            }
        }
    }
}
