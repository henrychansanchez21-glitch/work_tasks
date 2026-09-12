package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class VideoUserStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("video_sentinel_user_store", Context.MODE_PRIVATE)

    fun getFavoriteIds(): Set<String> {
        return prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    }

    fun toggleFavorite(videoId: String): Boolean {
        val current = getFavoriteIds().toMutableSet()
        val isNowFav = if (current.contains(videoId)) {
            current.remove(videoId)
            false
        } else {
            current.add(videoId)
            true
        }
        prefs.edit().putStringSet("favorites", current).apply()
        return isNowFav
    }

    fun isFavorite(videoId: String): Boolean {
        return getFavoriteIds().contains(videoId)
    }

    fun getCustomVideos(): List<VideoItem> {
        val jsonString = prefs.getString("custom_videos_json", null) ?: return emptyList()
        val list = mutableListOf<VideoItem>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    VideoItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        description = obj.optString("description", "Video personalizado"),
                        videoUrl = obj.getString("videoUrl"),
                        thumbnailUrl = obj.optString("thumbnailUrl", ""),
                        duration = obj.optString("duration", "Stream"),
                        category = "Mis Videos",
                        author = obj.optString("author", "Usuario"),
                        views = "Personal",
                        resolution = obj.optString("resolution", "HD"),
                        isCustom = true
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun addCustomVideo(title: String, url: String, description: String): VideoItem {
        val customList = getCustomVideos().toMutableList()
        val newVideo = VideoItem(
            id = "custom_" + System.currentTimeMillis(),
            title = title,
            description = if (description.isNotBlank()) description else "Enlace de video personalizado",
            videoUrl = url,
            thumbnailUrl = "",
            duration = "Stream MP4",
            category = "Mis Videos",
            author = "Agregado por Usuario",
            views = "Local",
            resolution = "1080p Stream",
            isCustom = true
        )
        customList.add(0, newVideo)

        val array = JSONArray()
        for (item in customList) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("description", item.description)
                put("videoUrl", item.videoUrl)
                put("thumbnailUrl", item.thumbnailUrl)
                put("duration", item.duration)
                put("author", item.author)
                put("resolution", item.resolution)
            }
            array.put(obj)
        }
        prefs.edit().putString("custom_videos_json", array.toString()).apply()
        return newVideo
    }
}
