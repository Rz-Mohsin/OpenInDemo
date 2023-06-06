package com.example.openindemo.models

data class Data(
    val overall_url_chart: Map<String,Int>,
    val recent_links: List<RecentLink>,
    val top_links: List<TopLink>
)