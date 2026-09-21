package com.southboundstudios.tripcheq.domain.model

data class BilledLeg(
    val entry: String,
    val exit: String,
    val note: String = "",
)