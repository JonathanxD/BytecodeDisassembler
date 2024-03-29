/**
 *      BytecodeDisassembler - A bytecode printer written on top of Java ASM <https://github.com/JonathanxD/BytecodeDisassembler>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2021 JonathanxD <jonathan.scripter@programmer.net>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.jonathanxd.bytecodedisassembler

import java.util.*

interface Appender {

    fun append(str: String)

    fun flush() {
    }

    override fun toString(): String

    class FourIndent(private val appender: Appender) : Appender {

        override fun append(str: String) {
            this.appender.append(str indent "    ")
        }

        override fun flush() {
            this.appender.flush()
        }

        override fun toString(): String = this.appender.toString()
    }

    class TwoIndent(private val appender: Appender) : Appender {

        override fun append(str: String) {
            this.appender.append(str indent "  ")
        }

        override fun flush() {
            this.appender.flush()
        }

        override fun toString(): String = this.appender.toString()
    }

    class Joiner(private val joiner: StringJoiner) : Appender {
        override fun append(str: String) {
            this.joiner.add(str)
        }

        override fun toString(): String = this.joiner.toString()
    }

    companion object {
        internal infix fun String.indent(ident: String) = this.split("\n").map { "$ident$it" }.joinToString("\n")
    }
}