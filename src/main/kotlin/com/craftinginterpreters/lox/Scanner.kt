package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.Lox.error
import com.craftinginterpreters.lox.TokenType.*

internal class Scanner(private val source: String) {

    private val tokens: ArrayList<Token> = ArrayList()

    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current
            scanToken()
        }
        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        val c: Char = advance()
        when (c) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) BANG_EQUAL else TokenType.BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else TokenType.GREATER)
            '/' -> if (match('/')) {
                // A comment goes until the end of the line.
                while (peek() !== '\n' && !isAtEnd()) advance()
            } else {
                addToken(SLASH)
            }

            // bypass whitespace
            ' ',
            '\r',
            '\t' -> {}
            '\n' -> line++

            '"' -> string()

            else -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier()
                }
                else{
                    error(line, "Unexpected character.")
                }

            }
        }
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val text = source.substring(start, current)
        var type: TokenType = when(text){
            "and" -> AND
            "class" -> CLASS
            "else" -> ELSE
            "false" -> FALSE
            "for" -> FOR
            "fun" -> FUN
            "if" ->  IF
            "nil" -> NIL
            "or" ->  OR
            "print" -> PRINT
            "return" -> RETURN
            "super" -> SUPER
            "this" -> THIS
            "true" -> TRUE
            "var" -> VAR
            "while" -> WHILE
            else -> TokenType.IDENTIFIER
        }
        addToken(type)
    }

    private fun number() {
        while (isDigit(peek())) advance()

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance()
            while (isDigit(peek())) advance()
        }
        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }
        if (isAtEnd()) {
            error(line, "Unterminated string.")
            return
        }

        // The closing "
        advance()

        // Trim the surrounding quotes.
        val value = source.substring(start + 1, current - 1)
        addToken(STRING, value)
    }

    // it's a conditional advance if `expected` matches the current
    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }

    private fun peek(): Char {
        return if (isAtEnd()) '\u0000' else source[current]
    }

    private fun peekNext(): Char {
        return if (current + 1 >= source.length) '\u0000' else source[current + 1]
    }

    private fun isAlpha(c: Char): Boolean {
        return (c in 'a'..'z') || (c in 'A'..'Z') || c == '_'
    }

    private fun isAlphaNumeric(c: Char): Boolean {
        return isAlpha(c) || isDigit(c)
    }

    private fun isDigit(c: Char): Boolean {
        return c in '0'..'9'
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun advance(): Char {
        return source[current++]
    }

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun tmp() {
        val c = advance()
        when (c) {
            '"' -> string()
        }
    }
}

