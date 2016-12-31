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

class DisassemblerClassVisitor(val appendFrames: Boolean, val appender: Appender, api: Int = Opcodes.ASM5, parent: ClassVisitor? = null) : ClassVisitor(api, parent) {

    private val later = Appender.Later(appender)


    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        appender.append("")

        appender.append("version: $version (${Util.parseVersion(version)})")
        appender.append("access: $access (${Util.parseAccess(Util.CLASS, access)})")

        signature?.let {
            appender.append("signature: $it")
        }

        appender.append("")

        val modsStr = Util.parseAsModifiersStr(Util.CLASS, access)
        val ext = if (superName != null) " extends ${Util.parseType(superName)}" else ""
        val ext2 = if (interfaces != null && interfaces.isNotEmpty()) " implements ${interfaces.map { Util.parseType(it) }.joinToString()}" else ""
        later.append("$modsStr class ${Util.parseType(name)}$ext$ext2 {")

        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        later.flush()
        appender.append("")
        return DisassemblerMethodVisistor(
                appendFrames = appendFrames,
                access = access,
                name = name,
                desc = desc,
                signature = signature,
                exceptions = exceptions,
                appender = Appender.FourIdent(this.appender),
                api = this.api,
                parent = super.visitMethod(access, name, desc, signature, exceptions)
        )
    }

    override fun visitInnerClass(name: String?, outerName: String?, innerName: String?, access: Int) {
        appender.append("innerClass: $outerName.$name -> $innerName (access: $access (${Util.parseAccess(Util.CLASS, access)}))")
        super.visitInnerClass(name, outerName, innerName, access)
    }

    override fun visitSource(source: String?, debug: String?) {
        appender.append("source: $source, debug: $debug")
        super.visitSource(source, debug)
    }

    override fun visitOuterClass(owner: String?, name: String?, desc: String?) {
        appender.append("outerClass: $owner.$name ($desc)")
        super.visitOuterClass(owner, name, desc)
    }

    override fun visitField(access: Int, name: String?, desc: String?, signature: String?, value: Any?): FieldVisitor {
        later.flush()
        appender.append("")
        return DisassemblerFieldVisistor(
                access = access,
                name = name,
                desc = desc,
                signature = signature,
                value = value,
                appender = Appender.FourIdent(this.appender),
                api = this.api,
                parent = super.visitField(access, name, desc, signature, value)
        )
    }

    override fun visitEnd() {
        later.flush()
        appender.append("}")
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