package com.topdon.lib.core.repository

data class WsResponse<T>(
    val cmd: Int,
    val data: T?,
    val id: String,
)

data class WsPseudoColor(
    val enable: Boolean?,
    val mode: Int?,
)

data class WsRange(
    val state: Int?
)

data class WsLight(
    val brightness: Int?
)

data class WsPip(
    val enable: Int?,
)

data class WsZoom(
    val enable: Boolean?,
    val factor: Int?,
)
