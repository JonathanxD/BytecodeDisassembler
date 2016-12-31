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

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*

object Disassembler {

    @JvmStatic
    @JvmOverloads
    fun disassemble(path: Path, appendLastModifiedTime: Boolean = false, hash: Boolean = false, appendFrames: Boolean = true, classVisitor: ClassVisitor? = null): String {
        val appender = Appender.Joiner(StringJoiner("\n"))

        if (appendLastModifiedTime) {
            appender.append("Last modified time: ${Files.getLastModifiedTime(path)}")
        }

        return disassemble(
                appendFrames = appendFrames,
                bytes = Files.readAllBytes(path),
                appender = appender,
                classVisitor = classVisitor,
                hash = hash
        )
    }

    @JvmStatic
    @JvmOverloads
    fun disassemble(bytes: ByteArray, hash: Boolean = false, appendFrames: Boolean = true, appender: Appender = Appender.Joiner(StringJoiner("\n")), classVisitor: ClassVisitor? = null): String {
        if (hash) {
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(bytes)
            val hex = StringBuilder()

            for (i in 0..hash.size - 1) {
                if (0xff and hash[i].toInt() < 0x10) {
                    hex.append("0" + Integer.toHexString(0xFF and hash[i].toInt()))
                } else {
                    hex.append(Integer.toHexString(0xFF and hash[i].toInt()))
                }
            }

            appender.append("md5: $hex")
        }

        val cr = ClassReader(bytes)


        cr.accept(
                DisassemblerClassVisitor(
                        appendFrames = appendFrames,
                        appender = appender,
                        api = Opcodes.ASM5,
                        parent = classVisitor
                ),
                0
        )

        return appender.toString()
    }

}