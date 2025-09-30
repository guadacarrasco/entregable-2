<<<<<<< HEAD
# Juego Concurrente en Java

## Descripción
Este proyecto implementa un juego multijugador concurrente en Java que utiliza hilos para simular jugadores, robots amigables y un robot malo en un tablero de NxN.

## Características del Juego

### Personajes
- **Jugadores**: Se mueven por el tablero, recolectan monedas y vidas, evitan trampas
- **Robot de Vidas**: Coloca vidas en casillas libres del tablero
- **Robot de Monedas**: Coloca monedas (1, 2, 5 o 10) en casillas libres
- **Robot Malo**: Coloca trampas que quitan vidas a los jugadores

### Mecánicas
- Tablero de 10x10 casillas
- Mínimo 3 jugadores para iniciar
- Duración de 60 segundos por partida
- Movimiento basado en dados (1-6)
- Recolección de premios al pasar por casillas
- Sistema de vidas (inicio con 2 vidas)
- Objetivo: Obtener la mayor cantidad de monedas y sobrevivir

## Compilación y Ejecución

### Requisitos
- Java 8 o superior
- Sistema operativo compatible con Java

### Compilar
```bash
javac *.java
```

### Ejecutar
```bash
java GameSimulator
```

## Estructura del Proyecto

### Clases Principales

1. **GameBoard.java**: Tablero del juego con sincronización thread-safe
2. **Player.java**: Representa un jugador con su lógica de movimiento
3. **FriendlyRobot.java**: Robots amigables (vidas y monedas)
4. **EvilRobot.java**: Robot malo que coloca trampas
5. **ConcurrentGame.java**: Maneja la lógica principal del juego
6. **GameLogger.java**: Sistema de logging thread-safe
7. **GameSimulator.java**: Clase principal para ejecutar la simulación

### Patrones de Diseño Concurrente Utilizados

1. **ReadWriteLock**: Para optimizar el acceso concurrente al tablero
2. **CountDownLatch**: Para sincronizar el inicio y fin del juego
3. **ReentrantLock**: Para secciones críticas del código
4. **ExecutorService**: Para manejar el pool de hilos
5. **ScheduledExecutorService**: Para tareas programadas
6. **Singleton**: Para el sistema de logging
7. **Producer-Consumer**: Para el sistema de logging

## Características Técnicas

### Sincronización
- Uso de `ReadWriteLock` para optimizar lecturas concurrentes del tablero
- `ReentrantLock` para secciones críticas
- `CountDownLatch` para sincronizar eventos del juego
- Estructuras de datos thread-safe

### Prevención de Deadlocks
- Orden consistente de adquisición de locks
- Timeouts en operaciones de espera
- Uso de `tryLock()` donde es apropiado

### Logging
- Sistema de logging thread-safe con locks
- Archivos de log con timestamp
- Logging de todas las acciones importantes

## Configuración

### Parámetros Ajustables
- Tamaño del tablero: `BOARD_SIZE = 10`
- Jugadores mínimos: `MIN_PLAYERS = 3`
- Duración del juego: `GAME_DURATION = 60` segundos
- Tiempos de espera entre acciones de robots
- Cantidad máxima de monedas y trampas (10% del tablero)

## Ejecución de la Simulación

1. Ejecutar `java GameSimulator`
2. El sistema agregará automáticamente 3 jugadores
3. Opción de agregar más jugadores
4. El juego se ejecuta automáticamente
5. Monitoreo en tiempo real del estado
6. Resultados al finalizar

## Archivos Generados

- `game_log_YYYYMMDD_HHMMSS.txt`: Log detallado de cada partida
- Salida en consola con visualización del tablero

## Mejoras Implementadas

1. **Visualización mejorada**: Tablero ASCII con colores y estadísticas
2. **Sistema de logging robusto**: Archivos de log con timestamps
3. **Monitoreo en tiempo real**: Estado del juego cada 5 segundos
4. **Manejo de excepciones**: Gestión robusta de errores
5. **Interfaz de usuario**: Menús interactivos para configuración
6. **Reinicio de partidas**: Posibilidad de jugar múltiples partidas
7. **Estadísticas detalladas**: Información completa de cada jugador

## Consideraciones de Rendimiento

- Uso eficiente de locks para minimizar contención
- Pool de hilos para manejar concurrencia
- Timeouts para evitar bloqueos indefinidos
- Limpieza adecuada de recursos

## Conclusión

Este proyecto demuestra el uso efectivo de patrones de concurrencia en Java para crear un sistema multijugador robusto y eficiente. La implementación incluye todas las mejores prácticas de programación concurrente y proporciona una base sólida para futuras mejoras.
=======
# entregable-2
>>>>>>> 841f8166cd597e5419226618d6597cb1508e0a3b
