package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.Expr.*
import com.craftinginterpreters.lox.Lox.error
import com.craftinginterpreters.lox.Stmt.While
import java.util.*

private enum class FunctionType {
    NONE,
    FUNCTION,
    INITIALIZER,
    METHOD
}

private enum class ClassType {
    NONE,
    CLASS
}


internal class Resolver(private val interpreter: Interpreter) :
    Expr.Visitor<Void?>, Stmt.Visitor<Void?>{

    private val scopes = Stack<MutableMap<String, Boolean>>()
    private var currentFunction: FunctionType? = FunctionType.NONE
    private var currentClass = ClassType.NONE

    override fun visitBlockStmt(stmt: Stmt.Block): Void? {
        beginScope()
        resolve(stmt.statements)
        endScope()
        return null
    }

    override fun visitClassStmt(stmt: Stmt.Class): Void? {
        val enclosingClass: ClassType = currentClass
        currentClass = ClassType.CLASS

        declare(stmt.name!!)
        define(stmt.name)

        beginScope()
        scopes.peek()["this"] = true;

        for (method in stmt.methods!!) {
            var declaration: FunctionType? = FunctionType.METHOD
            if (method?.name?.lexeme.equals("init")) {
                declaration = FunctionType.INITIALIZER
            }
            resolveFunction(method!!, declaration!!)
        }

        endScope()
        currentClass = enclosingClass
        return null
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression): Void? {
        resolve(stmt.expression)
        return null
    }

    override fun visitFunctionStmt(stmt: Stmt.Function): Void? {
        declare(stmt.name)
        define(stmt.name)

        resolveFunction(stmt, FunctionType.FUNCTION)
        return null
    }

    override fun visitIfStmt(stmt: Stmt.If): Void? {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        if (stmt.elseBranch != null) resolve(stmt.elseBranch)
        return null
    }

    override fun visitPrintStmt(stmt: Stmt.Print): Void? {
        resolve(stmt.expression)
        return null
    }

    override fun visitReturnStmt(stmt: Stmt.Return): Void? {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Can't return from top-level code.");
        }

        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                Lox.error(stmt.keyword,
                    "Can't return a value from an initializer.");
            }
            resolve(stmt.value)
        }

        return null
    }

    override fun visitVarStmt(stmt: Stmt.Var): Void? {
        declare(stmt.name)
        if (stmt.initializer != null) {
            resolve(stmt.initializer)
        }
        define(stmt.name)
        return null
    }

    override fun visitWhileStmt(stmt: While): Void? {
        resolve(stmt.condition)
        resolve(stmt.body)
        return null
    }

    override fun visitAssignExpr(expr: Assign): Void? {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
        return null
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Void? {
        resolve(expr.left)
        resolve(expr.right)
        return null
    }

    override fun visitCallExpr(expr: Expr.Call): Void? {
        resolve(expr.callee)

        for (argument in expr.arguments) {
            resolve(argument)
        }

        return null
    }

    override fun visitGetExpr(expr: Get): Void? {
        resolve(expr.`object`!!)
        return null
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Void? {
        resolve(expr.expression)
        return null
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Void? {
        return null
    }

    override fun visitLogicalExpr(expr: Logical): Void? {
        resolve(expr.left)
        resolve(expr.right)
        return null
    }

    override fun visitSetExpr(expr: Expr.Set): Void? {
        resolve(expr.value!!)
        resolve(expr.`object`!!)
        return null
    }

    override fun visitThisExpr(expr: This): Void? {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword,
                "Can't use 'this' outside of a class.");
            return null
        }
        resolveLocal(expr, expr.keyword)
        return null
    }


    override fun visitUnaryExpr(expr: Expr.Unary): Void? {
        resolve(expr.right)
        return null
    }

    override fun visitVariableExpr(expr: Expr.Variable): Void? {
        if (!scopes.isEmpty() &&
            scopes.peek()[expr.name.lexeme] == java.lang.Boolean.FALSE
        ) {
            error(
                expr.name,
                "Can't read local variable in its own initializer."
            )
        }

        resolveLocal(expr, expr.name)
        return null
    }

    fun resolve(statements: List<Stmt?>) {
        for (statement in statements) {
            resolve(statement)
        }
    }

    private fun resolve(stmt: Stmt?) {
        stmt?.accept(this)
    }

    private fun resolve(expr: Expr) {
        expr.accept(this)
    }

    private fun resolveFunction(function: Stmt.Function, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type

        beginScope()
        for (param in function.params) {
            declare(param)
            define(param)
        }
        resolve(function.body)
        endScope()
        currentFunction = enclosingFunction;
    }

    private fun beginScope() {
        scopes.push(HashMap<String, Boolean>())
    }

    private fun endScope() {
        scopes.pop()
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) return
        val scope: MutableMap<String, Boolean> = scopes.peek()
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name,
                "Already a variable with this name in this scope.");
        }
        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) return
        scopes.peek()[name.lexeme] = true
    }

    private fun resolveLocal(expr: Expr?, name: Token) {
        for (i in scopes.indices.reversed()) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
        }
    }
}