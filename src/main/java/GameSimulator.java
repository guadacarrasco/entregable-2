package entregable2;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

/**
 * Clase principal que ejecuta la simulación del juego
 */
public class GameSimulator {
    // Justificación: Según la consigna, se decide finalizar la simulación al terminar la partida, sin lista de espera.
    private static final int BOARD_SIZE = 10;
    private static final int MIN_PLAYERS = 3;
    private static final int GAME_DURATION = 60; // 60 segundos
    
    public static void main(String[] args) {
        // Lanzar la GUI y la lógica del juego en hilos separados
        Thread gameLogicThread = new Thread(() -> runGameSimulator());
        gameLogicThread.start();
    }

    private static void runGameSimulator() {
        Scanner scanner = new Scanner(System.in);
        // Selección de dificultad
        System.out.println("Seleccione dificultad: 1=Fácil, 2=Normal, 3=Difícil");
        int dificultad = 2;
    try {
            dificultad = Integer.parseInt(scanner.nextLine());
        } catch (Exception e) { dificultad = 2; }
        if (dificultad < 1 || dificultad > 3) dificultad = 2;

        // Parámetros según dificultad
        int vidasIniciales = (dificultad == 1) ? 4 : (dificultad == 3) ? 1 : 2;
        int maxTrampas = (dificultad == 1) ? 5 : (dificultad == 3) ? 15 : 10;
        int maxVidas = (dificultad == 1) ? 15 : (dificultad == 3) ? 5 : 10;
        int maxMonedas = (dificultad == 1) ? 15 : (dificultad == 3) ? 5 : 10;
        int robotPremioFrecuencia = (dificultad == 1) ? 1000 : (dificultad == 3) ? 4000 : 2000;
        int robotTrampaFrecuencia = (dificultad == 1) ? 4000 : (dificultad == 3) ? 1000 : 3000;

        ConcurrentGame game = new ConcurrentGame(BOARD_SIZE, MIN_PLAYERS, GAME_DURATION,
            vidasIniciales, maxTrampas, maxVidas, maxMonedas, robotPremioFrecuencia, robotTrampaFrecuencia);

        System.out.println("=== SIMULADOR DE JUEGO CONCURRENTE ===");
        System.out.println("Tablero: " + BOARD_SIZE + "x" + BOARD_SIZE);
        System.out.println("Jugadores mínimos: " + MIN_PLAYERS);
        System.out.println("Duración: " + GAME_DURATION + " segundos");
        System.out.println();

        try {
            // Crear ventana gráfica y callback de actualización
            final GameWindow[] windowRef = new GameWindow[1];
            Runnable uiUpdateCallback = () -> {
                if (windowRef[0] != null) {
                    windowRef[0].updateBoard(game.getBoard(), game.getPlayers());
                }
            };
            game.setUiUpdateCallback(uiUpdateCallback);

            // Agregar jugadores automáticamente para la simulación
            System.out.println("Agregando jugadores automáticamente...");
            for (int i = 0; i < MIN_PLAYERS; i++) {
                game.addPlayer();
                Thread.sleep(1000); // Esperar 1 segundo entre jugadores
            }

            // Configurar la cola de eventos en el logger
            game.getLogger().setDisplayEventQueue(game.getDisplayEventQueue());

            // Crear ventana gráfica
            SwingUtilities.invokeLater(() -> {
                windowRef[0] = new GameWindow(game.getBoard(), game.getPlayers());
                // Actualización inicial de stats y logs
                windowRef[0].updateStats(game.getStatsText());
                windowRef[0].clearLogs();
                windowRef[0].appendCategorizedLogs(game.getLogger().getLogs());
            });

            // Hilo de display: consume eventos de la cola y actualiza la GUI
            new Thread(() -> {
                try {
                    while (game.isGameRunning()) {
                        String event = game.getDisplayEventQueue().take();
                        if (windowRef[0] != null) {
                            SwingUtilities.invokeLater(() -> {
                                windowRef[0].updateStats(game.getStatsText());
                                windowRef[0].clearLogs();
                                windowRef[0].appendCategorizedLogs(game.getLogger().getLogs());
                            });
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "DisplayEventThread").start();

            // Hilo para refrescar la ventana cada 2 segundos (stats y logs)
            new Thread(() -> {
                while (game.isGameRunning()) {
                    if (windowRef[0] != null) {
                        SwingUtilities.invokeLater(() -> {
                            windowRef[0].updateStats(game.getStatsText());
                            windowRef[0].clearLogs();
                            windowRef[0].appendCategorizedLogs(game.getLogger().getLogs());
                        });
                    }
                    try { Thread.sleep(2000); } catch (InterruptedException e) { break; }
                }
            }).start();

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

            System.out.println("\n¡Juego finalizado! (La simulación se mostrará en la ventana gráfica)");



            // Esperar a que termine el juego
            game.waitForGameEnd();

            // Mostrar resumen final en la GUI y terminar la simulación
            SwingUtilities.invokeLater(() -> {
                StringBuilder resumen = new StringBuilder();
                resumen.append("=== PARTIDA TERMINADA ===\n\n");
                resumen.append("--- Jugadores ---\n");
                for (Player p : game.getPlayers()) {
                    resumen.append("Jugador ").append(p.getId())
                        .append(": Monedas: ").append(p.getCoins())
                        .append(", Vidas: ").append(p.getLives())
                        .append(", Estado: ").append(p.isAlive() ? "VIVO" : "MUERTO").append("\n");
                }
                resumen.append("\n");
                // Buscar ganador
                Player ganador = null;
                int maxCoins = -1;
                for (Player p : game.getPlayers()) {
                    if (p.isAlive() && p.getCoins() > maxCoins) {
                        ganador = p;
                        maxCoins = p.getCoins();
                    }
                }
                if (ganador != null) {
                    resumen.append("GANADOR: Jugador ").append(ganador.getId())
                        .append(" con ").append(ganador.getCoins()).append(" monedas\n");
                } else {
                    resumen.append("NO HAY GANADOR - Todos los jugadores murieron\n");
                }
                // Mostrar el diálogo sobre la ventana principal
                if (windowRef[0] != null && windowRef[0].isDisplayable()) {
                    JOptionPane.showMessageDialog(windowRef[0], resumen.toString(), "Resumen de la Partida", JOptionPane.INFORMATION_MESSAGE);
                    windowRef[0].dispose();
                } else {
                    JOptionPane.showMessageDialog(null, resumen.toString(), "Resumen de la Partida", JOptionPane.INFORMATION_MESSAGE);
                }
                System.exit(0);
            });
        } catch (InterruptedException e) {
            System.out.println("Simulación interrumpida");
            Thread.currentThread().interrupt();
        } finally {
            game.shutdown();
            scanner.close();
        }
    }
}
