package com.craftinginterpreters.lox

internal class Environment(val enclosing: Environment?) {
    private val values: HashMap<String, Any?> = hashMapOf()

    fun define(name: String, value: Any?) {
        values[name] = value
    }

    fun ancestor(distance: Int): Environment? {
        var environment: Environment? = this
        repeat(distance) {
            environment = environment?.enclosing
        }
        return environment
    }

    fun getAt(distance: Int, name: String?): Any? {
        return ancestor(distance)?.values?.get(name)
    }

    operator fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }
        if (enclosing != null) return enclosing[name]
        throw RuntimeError(
            name,
            "Undefined variable '" + name.lexeme + "'."
        )
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values[name.lexeme] = value
            return
        }
        if (enclosing != null) {
            enclosing.assign(name, value)
            return;
        }
        throw RuntimeError(
            name,
            "Undefined variable '" + name.lexeme + "'."
        )
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance)!!.values[name.lexeme] = value
    }

}