package com.craftinginterpreters.lox

class RuntimeError(val token: Token, message: String?) :
    RuntimeException(message)