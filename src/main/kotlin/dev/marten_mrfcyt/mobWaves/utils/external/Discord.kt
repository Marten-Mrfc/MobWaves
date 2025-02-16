package dev.marten_mrfcyt.mobWaves.utils.external

import dev.marten_mrfcyt.mobWaves.MobWaves
import java.awt.Color
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets

class Discord {
    private var content: String? = null
    private var username: String? = null
    private var avatarUrl: String? = null
    private var tts: Boolean = false
    private val embeds = mutableListOf<EmbedObject>()

    fun setUsername(username: String) { this.username = username }
    fun addEmbed(embed: EmbedObject) { embeds.add(embed) }
    fun setAvatarUrl(avatarUrl: String) { this.avatarUrl = avatarUrl }
    fun execute() {
        if (content == null && embeds.isEmpty()) throw IllegalArgumentException("Set content or add at least one EmbedObject")

        val json = JSONObject().apply {
            put("content", content)
            put("username", username)
            put("avatar_url", avatarUrl)
            put("tts", tts)
            if (embeds.isNotEmpty()) put("embeds", embeds.map { it.toJson() })
        }

        val webhookUrl = MobWaves.Companion.instance.config.getString("webhookURL") ?: return
        val connection = URI(webhookUrl).toURL().openConnection() as HttpURLConnection
        connection.apply {
            addRequestProperty("Content-Type", "application/json")
            addRequestProperty("User-Agent", "Kotlin-DiscordWebhook")
            doOutput = true
            requestMethod = "POST"
        }

        connection.outputStream.use { it.write(json.toString().toByteArray(StandardCharsets.UTF_8)) }
        connection.inputStream.use { it.readBytes() }
        connection.disconnect()
    }

    class EmbedObject {
        var title: String? = null
        var description: String? = null
        var url: String? = null
        var color: Color? = null
        var footer: Footer? = null
        var thumbnail: Thumbnail? = null
        var image: Image? = null
        var author: Author? = null
        val fields = mutableListOf<Field>()

        fun toJson() = JSONObject().apply {
            put("title", title)
            put("description", description)
            put("url", url)
            color?.let { put("color", it.rgb and 0xFFFFFF) }
            footer?.let { put("footer", it.toJson()) }
            thumbnail?.let { put("thumbnail", it.toJson()) }
            image?.let { put("image", it.toJson()) }
            author?.let { put("author", it.toJson()) }
            if (fields.isNotEmpty()) put("fields", fields.map { it.toJson() })
        }

        data class Footer(val text: String, val iconUrl: String?) {
            fun toJson() = JSONObject().apply {
                put("text", text)
                put("icon_url", iconUrl)
            }
        }

        data class Thumbnail(val url: String) {
            fun toJson() = JSONObject().apply { put("url", url) }
        }

        data class Image(val url: String) {
            fun toJson() = JSONObject().apply { put("url", url) }
        }

        data class Author(val name: String, val url: String?, val iconUrl: String?) {
            fun toJson() = JSONObject().apply {
                put("name", name)
                put("url", url)
                put("icon_url", iconUrl)
            }
        }

        data class Field(val name: String, val value: String, val inline: Boolean) {
            fun toJson() = JSONObject().apply {
                put("name", name)
                put("value", value)
                put("inline", inline)
            }
        }
    }

    class JSONObject {
        private val map = mutableMapOf<String, Any?>()

        fun put(key: String, value: Any?) {
            value?.let { map[key] = it }
        }

        override fun toString() = map.entries.joinToString(prefix = "{", postfix = "}") { (key, value) ->
            "\"$key\":${value.toJsonString()}"
        }

        private fun Any?.toJsonString(): String {
            return when (this) {
                is String -> "\"$this\""
                is Number, is Boolean -> this.toString()
                is JSONObject -> this.toString()
                is List<*> -> this.joinToString(prefix = "[", postfix = "]") { it.toJsonString() }
                else -> throw IllegalArgumentException("Unsupported type: ${this?.javaClass}")
            }
        }
    }

    private fun sendPlayerNotification(
        totalPlayers: Int,
        waveName: String?,
        round: Int?,
        title: String,
        description: String,
        color: Color
    ) {
        setUsername("MobWaves Bot")
        setAvatarUrl("https://drive.usercontent.google.com/download?id=1ivFJymb8AS4U_k0uTiMfJJSy8tpPRMmY")
        addEmbed(EmbedObject().apply {
            this.title = title
            this.description = description
            this.color = color
            fields.add(EmbedObject.Field("Total players", totalPlayers.toString(), true))
            fields.add(EmbedObject.Field("Wave", waveName ?: "None", true))
            fields.add(EmbedObject.Field("Round", round?.toString() ?: "None", true))
            footer = EmbedObject.Footer(
                "Marten Mrfc © 2024-2025",
                "https://drive.usercontent.google.com/download?id=1ivFJymb8AS4U_k0uTiMfJJSy8tpPRMmY"
            )
            url = "https://www.youtube.com/channel/UCSa3q7Rg1Ju0j46J4Ss8icQ"
        })
        execute()
    }

    fun sendPlayerJoinNotification(playerName: String, totalPlayers: Int, waveName: String?, round: Int?) {
        sendPlayerNotification(
            totalPlayers,
            waveName,
            round,
            "Player joined MobWave region",
            "Player $playerName has joined the $waveName MobWave region!",
            Color.ORANGE
        )
    }

    fun sendPlayerLeaveNotification(playerName: String, totalPlayers: Int, waveName: String?, round: Int?) {
        sendPlayerNotification(
            totalPlayers,
            waveName,
            round,
            "Player left MobWave region",
            "Player $playerName has left the $waveName MobWave region!",
            Color.RED
        )
    }
}