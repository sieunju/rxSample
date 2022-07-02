package com.hmju.rx.network.model

import kotlinx.serialization.Serializable

/**
 * Description :
 *
 * Created by juhongmin on 2022/07/02
 */
@Serializable
data class SimpleResponse(
    val status : Boolean = false,
    val data : SimpleData
)

@Serializable
data class SimpleData(
    val payload : SimplePayload
)

@Serializable
data class SimplePayload(
    val id : Long = -1L
)