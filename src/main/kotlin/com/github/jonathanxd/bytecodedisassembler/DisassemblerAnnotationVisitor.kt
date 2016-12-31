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

class DisassemblerAnnotationVisitor(val appender: Appender, val end: String? = null, api: Int = Opcodes.ASM5, parent: AnnotationVisitor? = null) : AnnotationVisitor(api, parent) {
    override fun visitEnd() {

        end?.let {
            if(it.isNotEmpty()) {
                if(appender is Appender.BufferedJoiner) {
                    appender.flush()
                    appender.appender.append(it)
                } else {
                    appender.append(it)
                }
            }
        }

        appender.flush()

        super.visitEnd()
    }

    override fun visitAnnotation(name: String?, desc: String?): AnnotationVisitor {
        if(name != null) {
            appender.append("$name = @$desc(")
        } else {
            appender.append("@$desc(")
        }

        return Util.visitAnnotation(Appender.NoFlush(this.appender), desc, api, ")", {super.visitAnnotation(name, desc)})
    }

    override fun visitEnum(name: String?, desc: String?, value: String?) {
        val descStr = Util.parseType(desc)

        if(name != null) {
            appender.append("$name = $descStr.$value")
        } else {
            appender.append("$descStr.$value")
        }

        super.visitEnum(name, desc, value)
    }

    override fun visit(name: String?, value: Any?) {

        val valueStr = Util.parseArrayValue(value)

        if(name != null) {
            appender.append("$name = $valueStr")
        } else {
            appender.append(valueStr)
        }

        super.visit(name, value)
    }



    override fun visitArray(name: String?): AnnotationVisitor {

        if(name != null) {
            appender.append("$name = {")
        } else {
            appender.append("{")
        }

        return Util.visitAnnotation(appender, {Appender.BufferedJoiner(StringJoiner(", "), Appender.NoFlush(it))}, null, api, "}", {super.visitArray(name)})
    }
}