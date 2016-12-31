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

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.util.Printer
import java.util.*

internal object Util {

    val specialCache = mapOf(
            0 to "TOP",
            1 to "INTEGER",
            2 to "FLOAT",
            3 to "DOUBLE",
            4 to "LONG",
            5 to "NULL",
            6 to "UNINITIALIZED_THIS"

    )

    val frameCache = mapOf(
            1 to "F_NEW",

            0 to "F_FULL",

            1 to "F_APPEND",

            2 to "F_CHOP",

            3 to "F_SAME",

            4 to "F_SAME1"

    )

    const val CLASS = 0
    const val FIELD = 1
    const val METHOD = 2
    const val PARAMETER = 3

    fun parseRet(desc: String?): String? {
        if (desc == null)
            return null

        return Type.getMethodType(desc).returnType.className
    }

    fun parseParams(desc: String?): String? {
        if (desc == null)
            return null

        return Type.getMethodType(desc).argumentTypes.map { it.className }.joinToString()
    }

    fun parseDesc(desc: String?): String? {
        if (desc == null)
            return null

        return "(${parseParams(desc)})${parseRet(desc)}"
    }

    fun parseType(type: String?): String? {
        if (type == null)
            return null

        val clName = Type.getType(type).className

        return clName ?: Type.getObjectType(type).className ?: type
    }

    fun parseVersion(version: Int) = when (version) {
        53 -> "Java 9"
        52 -> "Java 8"
        51 -> "Java 7"
        50 -> "Java 6"
        49 -> "Java 5"
        48 -> "Java 1.4"
        47 -> "Java 1.3"
        46 -> "Java 1.2"
        45 -> "Java 1.1"
        else -> "Unknown"
    }

    fun parseAccess(elementType: Int, access: Int): String {
        return fromAccess(elementType, access).joinToString()
    }

    fun parseAsModifiersStr(elementType: Int, access: Int): String {
        val joiner = StringJoiner(" ")

        if (elementType == CLASS || elementType == FIELD || elementType == METHOD) {
            joiner.addNotNull(
                    when {
                        access eq Opcodes.ACC_PUBLIC -> "public"
                        access eq Opcodes.ACC_PRIVATE -> "private"
                        access eq Opcodes.ACC_PROTECTED -> "protected"
                        else -> null
                    }
            )
        }

        if (elementType == FIELD) {
            joiner.addNotNull(
                    when {
                        access eq Opcodes.ACC_VOLATILE -> "volatile"
                        access eq Opcodes.ACC_TRANSIENT -> "transient"
                        else -> null
                    }
            )
        }

        if (elementType == CLASS || elementType == METHOD) {
            joiner.addNotNull(
                    when {
                        access eq Opcodes.ACC_ABSTRACT -> "abstract"
                        else -> null
                    }
            )
        }

        if (elementType == METHOD) {
            joiner.addNotNull(
                    when {
                        access eq Opcodes.ACC_SYNCHRONIZED -> "synchronized"
                        access eq Opcodes.ACC_NATIVE -> "native"
                        access eq Opcodes.ACC_STRICT -> "strict"
                        else -> null
                    }
            )
        }

        if (elementType == FIELD || elementType == METHOD) {
            joiner.addNotNull(
                    when {
                        access eq Opcodes.ACC_STATIC -> "static"
                        else -> null
                    }
            )
        }

        if (elementType == CLASS || elementType == FIELD || elementType == METHOD || elementType == PARAMETER) {
            joiner.addNotNull(
                    when {
                        access eq Opcodes.ACC_FINAL -> "final"
                        else -> null
                    }
            )
        }

        return joiner.toString()
    }

    fun fromAccess(elementType: Int, access: Int): Collection<String> {

        val modifiers = mutableSetOf<String>()

        if (elementType == CLASS || elementType == FIELD || elementType == METHOD) {

            if (access.eq(Opcodes.ACC_PUBLIC)) {
                modifiers.add("ACC_PUBLIC")
            } else if (access.eq(Opcodes.ACC_PRIVATE)) {
                modifiers.add("ACC_PRIVATE")
            } else if (access.eq(Opcodes.ACC_PROTECTED)) {
                modifiers.add("ACC_PROTECTED")
            } else {
                modifiers.add("PACKAGE_PRIVATE")
            }

        }

        if (elementType == FIELD) {
            if (access.eq(Opcodes.ACC_VOLATILE)) {
                modifiers.add("ACC_VOLATILE")
            }

            if (access.eq(Opcodes.ACC_TRANSIENT)) {
                modifiers.add("ACC_TRANSIENT")
            }

        }

        if (elementType == METHOD) {
            if (access.eq(Opcodes.ACC_SYNCHRONIZED)) {
                modifiers.add("ACC_SYNCHRONIZED")
            }

            if (access.eq(Opcodes.ACC_BRIDGE)) {
                modifiers.add("ACC_BRIDGE")
            }

            if (access.eq(Opcodes.ACC_VARARGS)) {
                modifiers.add("ACC_VARARGS")
            }

            if (access.eq(Opcodes.ACC_NATIVE)) {
                modifiers.add("ACC_NATIVE")
            }

            if (access.eq(Opcodes.ACC_STRICT)) {
                modifiers.add("ACC_STRICT")
            }

        }

        if (elementType == PARAMETER) {
            if (access.eq(Opcodes.ACC_MANDATED)) {
                modifiers.add("ACC_MANDATED")
            }
        }

        if (elementType == CLASS || elementType == METHOD) {
            if (access.eq(Opcodes.ACC_ABSTRACT)) {
                modifiers.add("ACC_ABSTRACT")
            }
        }

        if (elementType == FIELD || elementType == METHOD) {
            if (access.eq(Opcodes.ACC_STATIC)) {
                modifiers.add("ACC_STATIC")
            }
        }

        if (elementType == CLASS || elementType == FIELD || elementType == METHOD || elementType == PARAMETER) {
            if (access.eq(Opcodes.ACC_FINAL)) {
                modifiers.add("ACC_FINAL")
            }

            if (access.eq(Opcodes.ACC_SYNTHETIC)) {
                modifiers.add("ACC_SYNTHETIC")
            }
        }

        return modifiers
    }

    @Suppress("NOTHING_TO_INLINE")
    inline infix fun Int.eq(other: Int) = (this and other) != 0

    @Suppress("NOTHING_TO_INLINE")
    inline fun StringJoiner.addNotNull(str: String?): Unit {
        if (str != null) this.add(str)
    }

    inline fun visitAnnotation(appender: Appender, desc: String?, api: Int, end: String = "", superInvk: () -> AnnotationVisitor?): AnnotationVisitor {
        return this.visitAnnotation(appender, { Appender.Buffered(it) }, desc, api, end, superInvk)
    }

    inline fun visitAnnotation(appender: Appender, func: (Appender) -> Appender, desc: String?, api: Int, end: String = "", superInvk: () -> AnnotationVisitor?): AnnotationVisitor {

        val bufferedAppender = func(appender)
        appender.append("")

        val x = if (desc != null) {
            bufferedAppender.append("@${Util.parseType(desc)}(")
            ")"
        } else ""

        return DisassemblerAnnotationVisitor(Appender.BufferedJoiner(StringJoiner(", "), bufferedAppender), "$x$end", api, superInvk())
    }

    fun parseArrayValue(value: Any?): String {
        if (value == null)
            return "null"

        if (value is Type)
            return value.className

        if (value.javaClass.isArray) {
            val length = java.lang.reflect.Array.getLength(value)
            val ret = Array<Any?>(length) { java.lang.reflect.Array.get(value, it) }

            return parseArray(ret)
        }

        return value.toString()
    }

    private fun parseArray(array: Array<out Any?>): String {
        return array.map { this.parseArrayValue(it) }.joinToString(separator = ", ", prefix = "{", postfix = "}")
    }

    fun parseLocalsOrStack(array: Array<out Any?>?): String {
        if (array == null)
            return "{}"

        if (array.all { it == null })
            return "{}"

        val joiner = StringJoiner(", ", "{", "}")

        array.forEach {
            joiner.add(
                    if (it == null)
                        "null"
                    else if (it is Int)
                        Util.getSpecialName(it)
                    else if (it is String)
                        Util.parseType(it)
                    else
                        it.toString()
            )
        }

        return joiner.toString()
    }

    fun getOpcodeName(opcode: Int): String {

        if (opcode < 0 || opcode >= Printer.OPCODES.size)
            throw IllegalArgumentException("Cannot find $opcode in opcode name cache")

        return Printer.OPCODES[opcode]!!.toLowerCase()
    }

    fun getSpecialName(specialCode: Int): String {
        if (!specialCache.containsKey(specialCode))
            throw IllegalArgumentException("Cannot find $specialCode in special opcode name cache")

        return specialCache[specialCode]!!.toLowerCase()
    }

    fun getFrameName(frameCode: Int): String {
        if (!frameCache.containsKey(frameCode))
            throw IllegalArgumentException("Cannot find $frameCode in frame opcode name cache")

        return frameCache[frameCode]!!
    }

    fun getTypeName(typeCode: Int): String {
        if (typeCode < 0 || typeCode >= Printer.TYPES.size)
            throw IllegalArgumentException("Cannot find $typeCode in typeCode name cache")

        return Printer.TYPES[typeCode]!!.toLowerCase()
    }

    fun getHandleTagName(handleTagCode: Int): String {
        if (handleTagCode < 0 || handleTagCode >= Printer.HANDLE_TAG.size)
            throw IllegalArgumentException("Cannot find $handleTagCode in handle tag name cache")

        return Printer.HANDLE_TAG[handleTagCode]!!.toLowerCase()
    }

}