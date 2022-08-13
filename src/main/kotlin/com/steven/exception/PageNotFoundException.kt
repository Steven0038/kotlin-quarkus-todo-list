package com.steven.exception

class PageNotFoundException(val code: Int, override val message: String) : RuntimeException()
