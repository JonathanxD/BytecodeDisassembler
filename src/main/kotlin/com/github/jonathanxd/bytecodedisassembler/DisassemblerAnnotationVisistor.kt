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
import java.util.*

class DisassemblerAnnotationVisistor(val appender: Appender, val end: String? = null, api: Int = Opcodes.ASM5, parent: AnnotationVisitor? = null) : AnnotationVisitor(api, parent) {
    override fun visitEnd() {

        end?.let {
            appender.append(it)
        }

        super.visitEnd()
    }

    override fun visitAnnotation(name: String?, desc: String?): AnnotationVisitor {
        if(name != null) {
            appender.append("$name = @$desc(")
        } else {
            appender.append("@$desc(")
        }

        return DisassemblerAnnotationVisistor(this.appender, ")", api, super.visitAnnotation(name, desc))
    }

    override fun visitEnum(name: String?, desc: String?, value: String?) {
        if(name != null) {
            appender.append("$name = $desc.$value")
        } else {
            appender.append("$desc.$value")
        }

        super.visitEnum(name, desc, value)
    }

    override fun visit(name: String?, value: Any?) {

        if(name != null) {
            appender.append("$name = $value")
        } else {
            appender.append("$value")
        }

        super.visit(name, value)
    }

    override fun visitArray(name: String?): AnnotationVisitor {

        if(name != null) {
            appender.append("$name = {")
        } else {
            appender.append("{")
        }

        return DisassemblerAnnotationVisistor(this.appender, "}", api, super.visitArray(name))
    }
}