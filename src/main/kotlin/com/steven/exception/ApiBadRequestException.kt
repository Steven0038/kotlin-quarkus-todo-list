package com.steven.exception

class ApiBadRequestException(val code: Int, override val message: String) : RuntimeException()
