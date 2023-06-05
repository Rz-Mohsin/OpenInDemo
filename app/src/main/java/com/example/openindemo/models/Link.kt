package com.example.openindemo.models

data class Link (
    val thumbnail : Any?,
    val created_at: String,
    val smart_link: String,
    val times_ago: String,
    val title: String,
    val total_clicks: Int,
    val url_id: Int
)