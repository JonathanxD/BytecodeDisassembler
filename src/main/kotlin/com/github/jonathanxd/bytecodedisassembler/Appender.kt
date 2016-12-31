/**
 *      BytecodeDisassembler - A bytecode printer written on top of Java ASM <https://github.com/JonathanxD/BytecodeDisassembler>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2016 JonathanxD <${email}>
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

    class FourIdent(private val appender: Appender): Appender {

        override fun append(str: String) {
            this.appender.append("    $str")
        }

        override fun flush() {
            this.appender.flush()
        }

        override fun toString(): String = this.appender.toString()
    }

    class TwoIdent(private val appender: Appender): Appender {

        override fun append(str: String) {
            this.appender.append("  $str")
        }

        override fun flush() {
            this.appender.flush()
        }

        override fun toString(): String = this.appender.toString()
    }

    class Joiner(private val joiner: StringJoiner): Appender {
        override fun append(str: String) {
            this.joiner.add(str)
        }

        override fun toString(): String = this.joiner.toString()
    }

    class Later(private val appender: Appender): Appender {
        private val appends = mutableListOf<String>()

        override fun append(str: String) {
            this.appends.add(str)
        }

        override fun flush() {
            this.appends.forEach {
                this.appender.append(it)
            }

            this.appends.clear()
            this.appender.flush()
        }

        override fun toString(): String = this.appender.toString()
    }

    class BufferedJoiner(private val joiner: StringJoiner, val appender: Appender): Appender {
        var flushed = false

        override fun append(str: String) {
            this.joiner.add(str)
        }

        override fun flush() {
            if(!flushed) {
                this.appender.append(this.joiner.toString())
                this.appender.flush()
                flushed = true
            }

        }

        override fun toString(): String = this.joiner.toString()
    }

    class Buffered(private val appender: Appender): Appender {
        private val buffer = StringBuilder()

        override fun append(str: String) {
            this.buffer.append(str)
        }

        override fun flush() {
            val str = this.buffer.toString()
            this.buffer.setLength(0)
            this.appender.append(str)
            this.appender.flush()
        }

        override fun toString(): String = this.appender.toString()
    }

    class NoFlush(private val appender: Appender): Appender by appender {
        override fun flush() {

        }

        override fun toString(): String = this.appender.toString()
    }

}