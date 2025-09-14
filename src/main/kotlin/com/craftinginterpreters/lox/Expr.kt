package com.craftinginterpreters.lox


internal abstract class Expr {
    internal interface Visitor<R> {
        fun visitBinaryExpr(expr: Binary): R
        fun visitGroupingExpr(expr: Grouping): R
        fun visitLiteralExpr(expr: Literal): R
        fun visitUnaryExpr(expr: Unary): R
        fun visitVariableExpr(expr: Variable): R
        fun visitAssignExpr(expr: Assign): R
        fun visitLogicalExpr(expr: Logical): R
        fun visitCallExpr(expr: Call): R
        fun visitGetExpr(expr: Get): R
        fun visitSetExpr(expr: Set): R
        fun visitThisExpr(expr: This): R

    }

    abstract fun <R> accept(visitor: Visitor<R>): R

    internal class Binary(val left: Expr, val operator: Token, val right: Expr) :
        Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitBinaryExpr(this)
        }
    }

    internal class Get(val `object`: Expr?, val name: Token?) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitGetExpr(this)
        }
    }

    internal class Set(val `object`: Expr?, val name: Token?, val value: Expr?) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitSetExpr(this)
        }
    }

    internal class Grouping(val expression: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitGroupingExpr(this)
        }
    }

    internal class Literal(val value: Any?) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLiteralExpr(this)
        }
    }

    internal class Unary(val operator: Token, val right: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitUnaryExpr(this)
        }
    }
    internal class Variable(val name: Token) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitVariableExpr(this)
        }
    }

    internal class Assign(val name: Token, val value: Expr) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitAssignExpr(this)
        }
    }
    internal class Logical(val left: Expr, val operator: Token, val right: Expr) :
        Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitLogicalExpr(this)
        }
    }
    internal class Call(val callee: Expr, val paren: Token, val arguments: List<Expr>) :
        Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitCallExpr(this)
        }
    }

    internal class This(val keyword: Token) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R {
            return visitor.visitThisExpr(this)
        }
    }
}