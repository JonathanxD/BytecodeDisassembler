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

class DisassemblerFieldVisistor(val access: Int,
                                val name: String?,
                                val desc: String?,
                                val signature: String?,
                                val value: Any?,
                                val appender: Appender,
                                api: Int = Opcodes.ASM5,
                                parent: FieldVisitor? = null) : FieldVisitor(api, parent) {
    override fun visitEnd() {
        appender.append("!access: $access (${Util.parseAccess(Util.FIELD, access)})")

        signature?.let {
            appender.append("!signature: $it")
        }

        val ext = if(value != null) " = $value" else ""

        val modsStr = Util.parseAsModifiersStr(Util.FIELD, access)

        appender.append("$modsStr ${Util.parseType(desc)} $name$ext")

        appender.flush()

        super.visitEnd()
    }

    override fun visitAnnotation(desc: String?, visible: Boolean): AnnotationVisitor {

        return Util.visitAnnotation(appender, desc, api, "", { super.visitAnnotation(desc, visible) })
    }

    override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, desc: String?, visible: Boolean): AnnotationVisitor {
        return super.visitTypeAnnotation(typeRef, typePath, desc, visible)
    }

    override fun visitAttribute(attr: Attribute?) {
        super.visitAttribute(attr)
    }
}