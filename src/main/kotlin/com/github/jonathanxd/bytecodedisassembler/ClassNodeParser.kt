/**
 *      BytecodeDisassembler - A bytecode printer written on top of Java ASM <https://github.com/JonathanxD/BytecodeDisassembler>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2017 JonathanxD <jonathan.scripter@programmer.net>
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

import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import java.util.*

object ClassNodeParser {

    @Suppress("UNCHECKED_CAST")
    fun parse(classNode: ClassNode, appender: Appender, appendFrames: Boolean) {
        appender.append("")

        appender.append("version: ${Util.parseVersion(classNode.version)} (${classNode.version})")
        appender.append("access: ${Util.parseAccess(Util.CLASS, classNode.access)} (${classNode.access})")

        appender.append("")

        classNode.sourceFile?.let {
            appender.append("source: $it")
        }

        classNode.sourceDebug?.let {
            appender.append("debug {")
            appender.append("  $it")
            appender.append("}")
        }

        if (classNode.sourceFile != null || classNode.sourceDebug != null)
            appender.append("")

        classNode.outerClass?.let {
            appender.append("outerClass: ${Util.parseType(it)}")
        }

        classNode.outerMethod?.let {
            appender.append("outerMethod: ${Util.parseType(it)}")
        }

        classNode.outerMethodDesc?.let {
            appender.append("outerMethodDesc: ${Util.parseDesc(it)}")
        }

        if (classNode.outerClass != null || classNode.outerMethod != null || classNode.outerMethodDesc != null)
            appender.append("")

        val modsStr = Util.parseAsModifiersStr(Util.CLASS, classNode.access)
        val ext = if (classNode.superName != null) " extends ${Util.parseType(classNode.superName)}" else ""
        val ext2 = if (classNode.interfaces != null && classNode.interfaces.isNotEmpty()) " implements ${classNode.interfaces.map { Util.parseType(it as String) }.joinToString()}" else ""

        val annotations = (classNode.visibleAnnotations.orEmpty() + classNode.invisibleAnnotations.orEmpty()) as List<AnnotationNode>

        annotations.forEach {
            appender.append(parseAnnotationNode(it))
        }

        appender.append("$modsStr class ${Util.parseType(classNode.name)}$ext$ext2 {")

        appender.append("")

        val indented = Appender.FourIndent(appender)

        // Inner classes
        val innerClasses = classNode.innerClasses.orEmpty() as List<InnerClassNode>

        innerClasses.forEach {
            indented.append(parseInnerClassNode(it))
        }

        if (innerClasses.isNotEmpty())
            appender.append("")

        // Fields

        val fields = classNode.fields.orEmpty() as List<FieldNode>

        fields.forEach {
            indented.append(parseFieldNode(it))
        }

        if (fields.isNotEmpty())
            appender.append("")

        // Methods

        val methods = classNode.methods.orEmpty() as List<MethodNode>

        methods.forEach {
            indented.append(parseMethodNode(it, appendFrames))
        }

        appender.append("}")

    }

    @Suppress("UNCHECKED_CAST")
    private fun parseAnnotationNode(annotationNode: AnnotationNode): String {
        val desc = annotationNode.desc
        val buffer = StringBuilder()

        buffer.append("@${Util.parseType(desc)}(")

        fun parse(name: String?, value: Any?) = this.parseAnnotationValue(name, value, buffer)

        val values = annotationNode.values.orEmpty()

        for (i in values.indices step 2) {
            parse(values[i] as String, values[i + 1])
        }

        buffer.append(")")
        return buffer.toString()
    }

    private fun parseAnnotationValue(name: String?, value: Any?, buffer: StringBuilder) {
        name?.let {
            buffer.append("$it = ")
        }

        @Suppress("UNCHECKED_CAST")
        if (value == null)
            buffer.append("null")
        else if (value is Number || value is Boolean)
            buffer.append("$value")
        else if (value is String)
            buffer.append("\"$value\"")
        else if (value is Char)
            buffer.append("\'$value\'")
        else if (value is Type)
            buffer.append("${Util.parseType(value.className)}.class")
        else if (value.javaClass.isArray && value.javaClass == Array<String>::class.java && (value as Array<String>).size == 2)
            buffer.append("${Util.parseType(value[0])}.${value[1]}")
        else if (value is AnnotationNode)
            buffer.append(parseAnnotationNode(value))
        else if (value is List<*>) {
            buffer.append("{")

            value.forEachIndexed { i, any ->
                this.parseAnnotationValue(null, any, buffer)

                if (i + 1 < value.size)
                    buffer.append(", ")
            }

            buffer.append("}")
        } else
            buffer.append(Util.parseArrayValue(value))
    }

    private fun parseInnerClassNode(innerClassNode: InnerClassNode): String {
        return "!access: ${Util.parseAccess(Util.CLASS, innerClassNode.access)} (${innerClassNode.access})\n" +
                "${Util.parseAsModifiersStr(Util.CLASS, innerClassNode.access)} ${Util.parseType(innerClassNode.outerName)}.${innerClassNode.innerName} -> ${Util.parseType(innerClassNode.name)}"
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseFieldNode(fieldNode: FieldNode): String {
        val buffer = StringBuilder()


        buffer.append("!access: ${Util.parseAccess(Util.FIELD, fieldNode.access)} (${fieldNode.access})\n")

        fieldNode.signature?.let {
            buffer.append("!signature: $it\n")
        }

        val annotations = (fieldNode.visibleAnnotations.orEmpty() + fieldNode.invisibleAnnotations.orEmpty()) as List<AnnotationNode>
        if (annotations.isNotEmpty()) {
            annotations.forEach {
                buffer.append(parseAnnotationNode(it))
                buffer.append("\n")
            }
        }

        buffer.append("${Util.parseAsModifiersStr(Util.FIELD, fieldNode.access)} ${Util.parseType(fieldNode.desc)} ${fieldNode.name}")

        if (fieldNode.value != null)
            buffer.append(" = ${fieldNode.value}")

        return buffer.toString()
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseMethodNode(methodNode: MethodNode, appendFrames: Boolean): String {
        val appender = Appender.Joiner(StringJoiner("\n"))

        appender.append("!access: ${Util.parseAccess(Util.METHOD, methodNode.access)} (${methodNode.access})")

        methodNode.signature?.let {
            appender.append("!signature: $it")
        }

        methodNode.annotationDefault?.let {
            appender.append("!annotationDefault: ${parseAnnotationValue(null, it, StringBuilder())}")
        }

        val throws = methodNode.exceptions?.let {
            it as List<String>
            if (it.isNotEmpty())
                " throws ${it.map { Util.parseType(it) }.joinToString(" ")}"
            else
                ""
        } ?: ""


        val parameterList = methodNode.parameters.orEmpty() as List<ParameterNode>

        parameterList.forEach {
            appender.append("!parameter[name: ${it.name}, access: ${Util.parseAccess(Util.PARAMETER, it.access)} (${it.access})]")
        }

        val parameterAnnotations = ((methodNode.visibleParameterAnnotations.orEmpty() as Array<List<*>>) + (methodNode.invisibleParameterAnnotations.orEmpty() as Array<List<*>>)) as Array<List<AnnotationNode>>

        if (parameterAnnotations.isNotEmpty()) {
            appender.append("!parametersAnnotations [")

            val twoIndented = Appender.TwoIndent(appender)

            parameterAnnotations.forEachIndexed { i, list ->
                val indent = Appender.TwoIndent(twoIndented)

                twoIndented.append("[$i] {")

                list.forEach {
                    indent.append(parseAnnotationNode(it))
                }

                twoIndented.append("}")
            }

            appender.append("]")
        }

        val annotations = (methodNode.visibleAnnotations.orEmpty() + methodNode.invisibleAnnotations.orEmpty()) as List<AnnotationNode>

        if (annotations.isNotEmpty()) {
            annotations.forEach {
                appender.append(parseAnnotationNode(it))
            }
        }

        appender.append("${Util.parseAsModifiersStr(Util.METHOD, methodNode.access)} ${Util.parseRet(methodNode.desc)} ${methodNode.name}(${Util.parseParams(methodNode.desc)})$throws {")

        val twoIndented = Appender.TwoIndent(appender)

        twoIndented.append("desc: ${methodNode.desc} ")
        twoIndented.append("maxStack: ${methodNode.maxStack}, maxLocals: ${methodNode.maxLocals} ")

        val indented = Appender.FourIndent(appender)

        val mapper = LabelMapper()


        val insnList = methodNode.instructions

        insnList?.let { if (it.size() > 0) it[0] }

        val instructions = insnList?.toArray().orEmpty().filterNotNull()

        // Map label names
        instructions.forEach { if (it is LabelNode) mapper.getLabelName(it.label) }

        MethodInstructionsParser.parse(instructions, mapper, indented, twoIndented, appendFrames)

        val tryCatchBlocks = methodNode.tryCatchBlocks.orEmpty() as List<TryCatchBlockNode>

        if (tryCatchBlocks.isNotEmpty()) {
            twoIndented.append("TryCatchBlocks {")

            tryCatchBlocks.forEach {
                val startName = mapper.getLabelName(it.start.label)
                val endName = mapper.getLabelName(it.end.label)
                val handlerName = mapper.getLabelName(it.handler.label)
                val exceptionName = Util.parseType(it.type)

                indented.append("start: $startName, end: $endName, handler: $handlerName, exception: $exceptionName")
            }

            twoIndented.append("}")
        }

        val localVariables = methodNode.localVariables.orEmpty() as List<LocalVariableNode>

        if (localVariables.isNotEmpty()) {
            twoIndented.append("LocalVariables {")

            localVariables.forEach {
                val startName = mapper.getLabelName(it.start.label)
                val endName = mapper.getLabelName(it.end.label)
                val signature = it.signature
                val type = Util.parseType(it.desc)

                indented.append("index: ${it.index}, name: ${it.name}, start: $startName, end: $endName, type: $type, signature: $signature")
            }

            twoIndented.append("}")
        }

        appender.append("}")

        appender.append("")

        return appender.toString()
    }
}

