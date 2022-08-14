package com.steven.exception

class ApiNotFoundException(val code: Int, override val message: String) : RuntimeException()
