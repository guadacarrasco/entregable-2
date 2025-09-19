#!/bin/bash

echo "=== Compilando Juego Concurrente ==="

# Compilar todos los archivos Java
javac *.java

if [ $? -eq 0 ]; then
    echo "Compilación exitosa!"
    echo ""
    echo "=== Ejecutando Simulación ==="
    java GameSimulator
else
    echo "Error en la compilación. Verifique los errores arriba."
    exit 1
fi
