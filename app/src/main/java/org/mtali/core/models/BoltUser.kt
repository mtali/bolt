package org.mtali.core.models

data class BoltUser(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val type: String = UserType.PASSENGER.value,
    val status: String = UserStatus.INACTIVE.value,
    val avatarPhoto: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)