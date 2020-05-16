import pro.schmid.sbbtsp.db.Database

suspend fun main() {
    val firstConnection = downloadConnections("8501120", "8501008")
    Database.create(
        firstConnection.fromId,
        firstConnection.toId,
        firstConnection.minDuration,
        firstConnection.medianDuration
    )
}

