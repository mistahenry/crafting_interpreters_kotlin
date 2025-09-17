package com.craftinginterpreters.lox

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths


object Lox {
    private val interpreter = Interpreter()
    var hadError = false
    var hadRuntimeError = false

    @Throws(IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val script = script()
        if(script != null){
            run(script)
        } else if (args.size > 1) {
            println("Usage: jlox [script]")
            System.exit(64)
        } else if (args.size == 1) {
            runFile(args[0])
        } else {
            runPrompt()
        }
    }

    private fun script(): String? {
//        return """
//            fun makeCounter() {
//              var i = 0;
//              fun count() {
//                i = i + 1;
//                print i;
//              }
//
//              return count;
//            }
//
//            var counter = makeCounter();
//            counter(); // "1".
//            counter(); // "2".
//        """.trimIndent()

        return """
            fun fib(n) {
              if (n < 2) return n;
              return fib(n - 1) + fib(n - 2); 
            }
            
            var before = clock();
            print fib(40);
            var after = clock();
            print after - before;
        """.trimIndent()
    }

    @Throws(IOException::class)
    private fun runFile(path: String) {
        val bytes = Files.readAllBytes(Paths.get(path))
        run(String(bytes, Charset.defaultCharset()))

        if (hadError) System.exit(65)
        if (hadRuntimeError) System.exit(70)
    }

    @Throws(IOException::class)
    private fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)
        while (true) {
            print("> ")
            val line = reader.readLine() ?: break
            run(line)
            hadError = false
        }
    }

    private fun run(source: String) {
        val scanner = Scanner(source)
        val tokens: List<Token> = scanner.scanTokens() ?: emptyList()
        val parser = Parser(tokens)
        val statements = parser.parse()

        // Stop if there was a syntax error.
        if (hadError) return

        val resolver = Resolver(interpreter)
        resolver.resolve(statements)
        if (hadError) return

        interpreter.interpret(statements)
//        println(AstPrinter().print(expression!!))

//        // For now, just print the tokens.
//        for (token in tokens) {
//            System.out.println(token)
//        }
    }

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    private fun report(
        line: Int, where: String,
        message: String
    ) {
        System.err.println(
            "[line $line] Error$where: $message"
        )
        hadError = true
    }

    fun error(token: Token, message: String?) {
        if (token.type === TokenType.EOF) {
            report(token.line, " at end", message!!)
        } else {
            report(token.line, " at '" + token.lexeme + "'", message!!)
        }
    }
    fun runtimeError(error: RuntimeError) {
        System.err.println(
            """
            ${error.message}
            [line ${error.token.line}]
            """.trimIndent()
        )
        hadRuntimeError = true
    }
}