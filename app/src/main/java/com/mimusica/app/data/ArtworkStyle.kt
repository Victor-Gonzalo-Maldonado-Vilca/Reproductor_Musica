package com.mimusica.app.data

/** Forma de la carátula en "Ahora suena". */
enum class ArtworkBase(val label: String) {
    Cover("Portada"),
    Vinyl("Vinilo"),         // el disco de vinilo
}

/**
 * Efectos que se pueden combinar. Para añadir uno nuevo:
 * una línea aquí + su dibujo en AnimatedArtwork.
 */
enum class ArtworkEffect(
    val label: String,
    val description: String,
    val needsVinyl: Boolean = false,) {
    Spin("Girar", "El disco gira mientras suena la música", needsVinyl = true),
    Halo("Halo", "Un resplandor que se enciende al reproducir y se apaga en pausa", needsVinyl = true),
    Ring("Anillo", "Se llena al reproducir y se vacía en pausa", needsVinyl = true),
    Progress("Anillo de progreso", "Muestra el avance de la canción alrededor del borde", needsVinyl = true),
    Pulse("Rebote", "La carátula se encoge en pausa y rebota al reproducir"),
}