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

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.*

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Arguments: [file] <appendLastModifiedTime(true/false)> <hash(true/false)> <appendFrames(true/false)>")
        return
    }

    val file = args[0]
    val appendLastModifiedTime = args.getOrElse(1, { "true" }).toBoolean()
    val hash = args.getOrElse(2, { "true" }).toBoolean()
    val appendFrames = args.getOrElse(3, { "true" }).toBoolean()

    println(Disassembler.disassemble(
            path = Paths.get(file),
            appendLastModifiedTime = appendLastModifiedTime,
            hash = hash,
            appendFrames = appendFrames)
    )
}

object Disassembler {

    @JvmStatic
    @JvmOverloads
    fun disassemble(path: Path, appendLastModifiedTime: Boolean = false, hash: Boolean = false, appendFrames: Boolean = true): String {
        val appender = Appender.Joiner(StringJoiner("\n"))

        if (appendLastModifiedTime) {
            appender.append("Last modified time: ${Files.getLastModifiedTime(path)}")
        }

        return disassemble(
                bytes = Files.readAllBytes(path),
                appendHash = hash,
                appendFrames = appendFrames,
                appender = appender
        )
    }

    @JvmStatic
    @JvmOverloads
    fun disassemble(bytes: ByteArray,
                    appendHash: Boolean = false,
                    appendFrames: Boolean = true,
                    appender: Appender = Appender.Joiner(StringJoiner("\n"))): String {
        if (appendHash) {
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(bytes)
            val hex = StringBuilder()

            for (i in 0 until hash.size) {
                if (0xff and hash[i].toInt() < 0x10) {
                    hex.append("0" + Integer.toHexString(0xFF and hash[i].toInt()))
                } else {
                    hex.append(Integer.toHexString(0xFF and hash[i].toInt()))
                }
            }

            appender.append("md5: $hex")
        }

        val cr = ClassReader(bytes)
        val cn = ClassNode(Opcodes.ASM5)
        cr.accept(cn, 0)


        ClassNodeParser.parse(cn, appender, appendFrames)

        return appender.toString()
    }

}
