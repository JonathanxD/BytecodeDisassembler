/**
 *      BytecodeDisassembler - A bytecode printer written on top of Java ASM <https://github.com/JonathanxD/BytecodeDisassembler>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2020 JonathanxD <jonathan.scripter@programmer.net>
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

import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

object MethodInstructionsParser {

    internal fun parse(instructions: List<AbstractInsnNode>,
                       labelMapper: LabelMapper,
                       normalAppender: Appender,
                       shortAppender: Appender,
                       appendFrames: Boolean) {

        instructions.forEachIndexed { _, it ->
            when (it) {
                is MultiANewArrayInsnNode -> normalAppender.append("multiANewArray ${Util.parseType(it.desc)} ${it.dims}")
                is FrameNode -> {
                    if (appendFrames) {
                        val typeName = Util.getFrameName(it.type)
                        val localStr = Util.parseLocalsOrStack(it.local?.toTypedArray().orEmpty(), labelMapper)
                        val stackStr = Util.parseLocalsOrStack(it.stack?.toTypedArray().orEmpty(), labelMapper)

                        shortAppender.append(" FRAME[type: $typeName, locals: ${it.local.orEmpty().size}, local: $localStr, stacks: ${it.stack.orEmpty().size}, stack: $stackStr]")
                    }
                }
                is VarInsnNode -> normalAppender.append("${Util.getOpcodeName(it.opcode)} ${it.`var`}")
                is TableSwitchInsnNode -> {
                    normalAppender.append("tableSwitch(${it.min}..${it.max}) {")

                    val ex = Appender.FourIndent(normalAppender)

                    @Suppress("UNCHECKED_CAST")
                    (it.labels as List<LabelNode>?).orEmpty().forEachIndexed { i, label ->
                        ex.append("handler $i: ${labelMapper.getLabelName(label)}")
                    }

                    ex.append("default: ${labelMapper.getLabelName(it.dflt)}")

                    normalAppender.append("}")
                }
                is LookupSwitchInsnNode -> {
                    @Suppress("UNCHECKED_CAST")
                    val labels = (it.labels as List<LabelNode>?).orEmpty()
                    normalAppender.append("lookupSwitch {")

                    val ex = Appender.FourIndent(normalAppender)

                    labels.forEachIndexed { i, label ->
                        ex.append("handler ${it.keys!![i]}: ${labelMapper.getLabelName(label)}")
                    }

                    ex.append("default: ${labelMapper.getLabelName(it.dflt)}")

                    normalAppender.append("}")
                }
                is JumpInsnNode -> normalAppender.append("${Util.getOpcodeName(it.opcode)} ${labelMapper.getLabelName(it.label)}")
                is LdcInsnNode -> {
                    val cst = it.cst.let {
                        if (it is String)
                            "\"$it\""
                        else it
                    }

                    val type = if (cst is Type)
                        Class::class.java.canonicalName
                    else
                        cst.javaClass.canonicalName

                    normalAppender.append("ldc $cst              // type: $type")
                }
                is IntInsnNode -> {
                    if (it.operand == Opcodes.NEWARRAY) {
                        normalAppender.append("newarray ${Util.getTypeName(it.operand)}")
                    } else {
                        normalAppender.append("${Util.getOpcodeName(it.opcode)} ${it.operand}")
                    }
                }
                is TypeInsnNode -> normalAppender.append("${Util.getOpcodeName(it.opcode)} ${Util.parseType(it.desc)}")
                is InvokeDynamicInsnNode -> {
                    fun Handle?.asStr() =
                            if (this == null) "null" else "${Util.parseType(this.owner)}.${this.name}${Util.parseDesc(this.desc)} (tag: ${Util.getHandleTagName(this.tag)}, itf: ${this.isInterface})"

                    val bsmArgsStr = it.bsmArgs.orEmpty().map { (it as? Handle)?.asStr() ?: it.toString() }

                    normalAppender.append("invokedynamic ${it.name}${Util.parseDesc(it.desc)} [")

                    val ex = Appender.TwoIndent(normalAppender)

                    ex.append("// Bootstrap method")
                    ex.append("${it.bsm.asStr()} [")

                    val ex2 = Appender.FourIndent(normalAppender)

                    if (bsmArgsStr.isNotEmpty()) {
                        ex2.append("// Arguments")
                        bsmArgsStr.forEach {
                            ex2.append(it)
                        }
                    }

                    ex.append("]")

                    normalAppender.append("]")
                }
                is LabelNode -> {
                    it.label?.let {
                        val name = labelMapper.getLabelName(it)
                        shortAppender.append("$name:")
                    }
                }
                is MethodInsnNode -> it.apply {
                    normalAppender.append("${Util.getOpcodeName(opcode)} ${Util.parseType(owner)}.$name${Util.parseDesc(desc)} (ownerIsInterface: $itf)")
                }
                is InsnNode -> it.apply { normalAppender.append(Util.getOpcodeName(opcode)) }
                is IincInsnNode -> it.apply { normalAppender.append("iinc $`var` $incr") }
                is LineNumberNode -> it.apply {
                    val ext = if (start != null) " -> ${labelMapper.getLabelName(start)}" else ""
                    shortAppender.append(" LINE $line$ext")
                }
                is FieldInsnNode -> it.apply {
                    normalAppender.append("${Util.getOpcodeName(opcode)} ${Util.parseType(owner)}.$name (type: ${Util.parseType(desc)})")
                }
                else -> normalAppender.append("// Unknown AbstractInsnNode: OPCODE '${it.opcode}', TYPE '${it.type}' (instance: $it, class: ${it.javaClass.canonicalName})")
            }
        }

    }
}