# Instrucciones de Instalación y Ejecución

## Requisitos Previos

### Instalación de Java

#### En macOS:
1. Instalar Homebrew (si no está instalado):
   ```bash
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
   ```

2. Instalar Java:
   ```bash
   brew install openjdk@11
   ```

3. Configurar variables de entorno:
   ```bash
   echo 'export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"' >> ~/.zshrc
   source ~/.zshrc
   ```

#### En Windows:
1. Descargar Java JDK desde: https://www.oracle.com/java/technologies/downloads/
2. Instalar siguiendo las instrucciones del instalador
3. Configurar variable de entorno JAVA_HOME

#### En Linux (Ubuntu/Debian):
```bash
sudo apt update
sudo apt install openjdk-11-jdk
```

## Verificación de Instalación

Verificar que Java esté instalado correctamente:
```bash
java -version
javac -version
```

## Compilación y Ejecución

### Método 1: Script Automático
```bash
./compile_and_run.sh
```

### Método 2: Manual
```bash
# Compilar
javac *.java

# Ejecutar
java GameSimulator
```

## Estructura de Archivos

```
prog avanzada/
├── GameBoard.java          # Tablero del juego
├── Player.java             # Clase jugador
├── FriendlyRobot.java      # Robots amigables
├── EvilRobot.java          # Robot malo
├── ConcurrentGame.java     # Lógica principal
├── GameLogger.java         # Sistema de logging
├── GameSimulator.java      # Clase principal
├── compile_and_run.sh      # Script de compilación
├── README.md               # Documentación general
├── DOCUMENTACION.md        # Documentación técnica
└── INSTALACION.md          # Este archivo
```

## Solución de Problemas

### Error: "Unable to locate a Java Runtime"
- Instalar Java JDK siguiendo las instrucciones arriba
- Verificar que las variables de entorno estén configuradas

### Error de compilación
- Verificar que todos los archivos .java estén en el mismo directorio
- Verificar que la versión de Java sea 8 o superior

### Error de ejecución
- Verificar que la compilación haya sido exitosa
- Verificar que no haya archivos .class corruptos (eliminar y recompilar)

## Características del Juego

- **Tablero**: 10x10 casillas
- **Jugadores**: Mínimo 3, máximo 6
- **Duración**: 60 segundos por partida
- **Objetivo**: Recolectar la mayor cantidad de monedas y sobrevivir

## Archivos Generados

- `game_log_YYYYMMDD_HHMMSS.txt`: Log detallado de cada partida
- Archivos .class: Código compilado de Java

## Soporte

Para problemas técnicos, revisar:
1. La documentación técnica (DOCUMENTACION.md)
2. Los logs generados durante la ejecución
3. Los mensajes de error en la consola
