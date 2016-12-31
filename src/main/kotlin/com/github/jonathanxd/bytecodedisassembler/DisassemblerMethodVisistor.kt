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

import org.objectweb.asm.*

class DisassemblerMethodVisistor(
        val appendFrames: Boolean = true,
        val access: Int,
        val name: String?,
        val desc: String?,
        val signature: String?,
        val exceptions: Array<out String>?,
        val appender: Appender,
        api: Int = Opcodes.ASM5,
        parent: MethodVisitor? = null) : MethodVisitor(api, parent) {

    private val subAppender = Appender.FourIdent(this.appender)
    private val wAppender = Appender.TwoIdent(this.appender)
    private var visitCode = false

    private val labelMapper = LabelMapper()

    override fun visitAnnotationDefault(): AnnotationVisitor {

        if(!visitCode)
            visit() // Fix visit

        val buffered = Appender.Buffered(this.wAppender)

        buffered.append("default: ")

        return Util.visitAnnotation(buffered, null, api, "", { super.visitAnnotationDefault() })
    }

    override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor {
        return Util.visitAnnotation(appender, desc, api, "", {super.visitAnnotation(desc, visible)})
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        wAppender.append("MAX_STACK: $maxStack, MAX_LOCALS: $maxLocals")

        super.visitMaxs(maxStack, maxLocals)
    }


    private fun visit() {
        appender.append("!access: $access (${Util.parseAccess(Util.METHOD, access)})")

        signature?.let{
            appender.append("!signature: $it")
        }

        val modsStr = Util.parseAsModifiersStr(Util.METHOD, access)
        val ext = if(exceptions != null && exceptions.isNotEmpty()) " throws ${exceptions.map { Util.parseType(it) }.joinToString()}" else ""

        val ret = Util.parseRet(desc)
        val parm = Util.parseParams(desc)

        appender.append("$modsStr $ret $name($parm)$ext {")

        visitCode = true
    }

    override fun visitCode() {

        visit()

        super.visitCode()
    }

    override fun visitMultiANewArrayInsn(desc: String?, dims: Int) {
        subAppender.append("multiANewArray $desc $dims")

        super.visitMultiANewArrayInsn(desc, dims)
    }

    override fun visitFrame(type: Int, nLocal: Int, local: Array<out Any?>?, nStack: Int, stack: Array<out Any?>?) {
        if(appendFrames) {
            val typeName = Util.getFrameName(type)
            val localStr = Util.parseLocalsOrStack(local)
            val stackStr = Util.parseLocalsOrStack(stack)

            wAppender.append(" FRAME[type: $typeName, locals: $nLocal, local: $localStr, stacks: $nStack, stack: $stackStr]")
        }

        super.visitFrame(type, nLocal, local, nStack, stack)
    }

    override fun visitVarInsn(opcode: Int, `var`: Int) {

        subAppender.append("${Util.getOpcodeName(opcode)} $`var`")

        super.visitVarInsn(opcode, `var`)
    }

    override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {

        subAppender.append("!tryCatchBlock[type: $type, start: ${labelMapper.getLabelName(start)}, end: ${labelMapper.getLabelName(end)}, handler: ${labelMapper.getLabelName(handler)}]")

        super.visitTryCatchBlock(start, end, handler, type)
    }

    override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) {
        subAppender.append("tableSwitch($min..$max) {")

        val ex = Appender.FourIdent(subAppender)

        labels.forEachIndexed { i, label ->
            ex.append("handler $i: ${labelMapper.getLabelName(label)}")
        }

        ex.append("default: ${labelMapper.getLabelName(dflt)}")

        subAppender.append("}")

        super.visitTableSwitchInsn(min, max, dflt, *labels)
    }

    override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray?, labels: Array<out Label>?) {
        subAppender.append("lookupSwitch {")

        val ex = Appender.FourIdent(subAppender)

        labels?.forEachIndexed { i, label ->
            ex.append("handler ${keys!![i]}: ${labelMapper.getLabelName(label)}")
        }

        ex.append("default: ${labelMapper.getLabelName(dflt)}")

        subAppender.append("}")
        super.visitLookupSwitchInsn(dflt, keys, labels)
    }

    override fun visitJumpInsn(opcode: Int, label: Label?) {
        subAppender.append("${Util.getOpcodeName(opcode)} ${labelMapper.getLabelName(label)}")

        super.visitJumpInsn(opcode, label)
    }

    override fun visitLdcInsn(cst: Any?) {
        val cste = if(cst is String) {
            "\"$cst\""
        } else {
            cst
        }

        subAppender.append("ldc $cste")

        super.visitLdcInsn(cst)
    }

    override fun visitIntInsn(opcode: Int, operand: Int) {

        if(operand == Opcodes.NEWARRAY) {
            subAppender.append("newarray ${Util.getTypeName(operand)}")
        } else {
            subAppender.append("${Util.getOpcodeName(opcode)} $operand")
        }

        super.visitIntInsn(opcode, operand)
    }

    override fun visitTypeInsn(opcode: Int, type: String?) {
        subAppender.append("${Util.getOpcodeName(opcode)} ${Util.parseType(type)}")

        super.visitTypeInsn(opcode, type)
    }

    override fun visitInvokeDynamicInsn(name: String?, desc: String?, bsm: Handle?, vararg bsmArgs: Any?) {

        fun Handle?.asStr() =
            if(this == null) "null" else "${Util.parseType(this.owner)}.${this.name}${Util.parseDesc(this.desc)} (tag: ${Util.getHandleTagName(this.tag)}, itf: ${this.isInterface})"

        val bsmArgsStr = bsmArgs.map { (it as? Handle)?.asStr() ?: it.toString() }

        subAppender.append("invokedynamic $name${Util.parseDesc(desc)} (bootstrap: ${bsm.asStr()}, args: $bsmArgsStr)")

        super.visitInvokeDynamicInsn(name, desc, bsm, *bsmArgs)
    }

    override fun visitLabel(label: Label?) {
        label?.let {
            val count = labelMapper.getLabelName(it)
            this.wAppender.append("$count:")
        }

        super.visitLabel(label)
    }

    override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?) {
        subAppender.append("${Util.getOpcodeName(opcode)} ${Util.parseType(owner)}.$name${Util.parseDesc(desc)}")

        super.visitMethodInsn(opcode, owner, name, desc)
    }

    override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
        subAppender.append("${Util.getOpcodeName(opcode)} ${Util.parseType(owner)}.$name${Util.parseDesc(desc)} (ownerIsInterface: $itf)")

        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }

    override fun visitInsn(opcode: Int) {
        subAppender.append(Util.getOpcodeName(opcode))

        super.visitInsn(opcode)
    }

    override fun visitIincInsn(`var`: Int, increment: Int) {
        subAppender.append("iinc $`var` $increment")

        super.visitIincInsn(`var`, increment)
    }

    override fun visitLineNumber(line: Int, start: Label?) {
        val ext = if(start != null) " -> ${labelMapper.getLabelName(start)}" else ""
        wAppender.append(" LINE $line$ext")

        super.visitLineNumber(line, start)
    }

    override fun visitParameterAnnotation(parameter: Int, desc: String?, visible: Boolean): AnnotationVisitor {

        val buffered = Appender.Buffered(this.appender)

        buffered.append("!parameterAnnotation(parameter: $parameter) [")

        return Util.visitAnnotation(buffered, desc, api, "]", {super.visitParameterAnnotation(parameter, desc, visible)})
    }

    override fun visitLocalVariable(name: String?, desc: String?, signature: String?, start: Label?, end: Label?, index: Int) {

        wAppender.append("LOCAL_VARIABLE[index: $index, name: $name, desc: $desc, signature: $signature, start: ${labelMapper.getLabelName(start)}, end: ${labelMapper.getLabelName(end)}]")

        super.visitLocalVariable(name, desc, signature, start, end, index)
    }

    override fun visitParameter(name: String?, access: Int) {
        subAppender.append("!parameter[name: $name, access: $access (${Util.parseAccess(Util.PARAMETER, access)})]")

        super.visitParameter(name, access)
    }

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, desc: String?) {
        subAppender.append("${Util.getOpcodeName(opcode)} ${Util.parseType(owner)}.$name (type: ${Util.parseType(desc)})")

        super.visitFieldInsn(opcode, owner, name, desc)
    }

    override fun visitEnd() {
        if(!visitCode)
            visit()

        appender.append("}")
        appender.flush()
        super.visitEnd()
    }

    override fun visitAttribute(attr: Attribute?) {
        super.visitAttribute(attr)
    }

    override fun visitLocalVariableAnnotation(typeRef: Int, typePath: TypePath?, start: Array<out Label>?, end: Array<out Label>?, index: IntArray?, desc: String?, visible: Boolean): AnnotationVisitor {
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible)
    }

    override fun visitInsnAnnotation(typeRef: Int, typePath: TypePath?, desc: String?, visible: Boolean): AnnotationVisitor {
        return super.visitInsnAnnotation(typeRef, typePath, desc, visible)
    }

    override fun visitTryCatchAnnotation(typeRef: Int, typePath: TypePath?, desc: String?, visible: Boolean): AnnotationVisitor {
        return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible)
    }

    override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, desc: String?, visible: Boolean): AnnotationVisitor {
        return super.visitTypeAnnotation(typeRef, typePath, desc, visible)
    }

}