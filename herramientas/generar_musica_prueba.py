"""Genera 5 canciones de prueba (.wav de 45 s) para probar Nocturne en el emulador.

Uso:
    python herramientas/generar_musica_prueba.py musica_prueba

Luego cópialas al emulador (ver README, sección "Cómo probarlo").
"""
import math
import os
import struct
import sys
import wave

CANCIONES = {
    "Midnight Horizon": 220,
    "Neon Velocity": 262,
    "Solar Flare": 294,
    "Starlight Dreamer": 330,
    "Quantum Drift": 196,
}
NOTAS = [1, 1.25, 1.5, 2, 1.5, 1.25]  # arpegio simple
FRECUENCIA_MUESTREO = 16000
SEGUNDOS = 45  # más de 30 s para que no se oculten como "audio corto"


def generar(carpeta: str) -> None:
    os.makedirs(carpeta, exist_ok=True)
    for nombre, base in CANCIONES.items():
        ruta = os.path.join(carpeta, f"{nombre}.wav")
        with wave.open(ruta, "wb") as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(FRECUENCIA_MUESTREO)
            datos = bytearray()
            for i in range(FRECUENCIA_MUESTREO * SEGUNDOS):
                t = i / FRECUENCIA_MUESTREO
                f = base * NOTAS[int(t * 2) % len(NOTAS)]
                v = 0.25 * math.sin(2 * math.pi * f * t) * (1 - (t * 2 % 1) * 0.6)
                datos += struct.pack("<h", int(v * 32767))
            w.writeframes(bytes(datos))
        print("Creada:", ruta)


if __name__ == "__main__":
    generar(sys.argv[1] if len(sys.argv) > 1 else "musica_prueba")
