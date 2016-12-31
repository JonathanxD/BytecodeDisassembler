/**
 * BytecodeDisassembler - A bytecode printer written on top of Java ASM
 * <https://github.com/JonathanxD/BytecodeDisassembler>
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 JonathanxD <${email}> Copyright (c) contributors
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.jonathanxd.bytecodedisassembler.test;

import com.github.jonathanxd.bytecodedisassembler.Disassembler;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;

import kotlin.io.ByteStreamsKt;
import kotlin.io.ConstantsKt;

public class DisassemblerTest {

    @Test
    public void disassemblerTest() throws IOException {

        final String disassembled = disassemble("/TestBytecode_Invocations_Result.class");
        System.out.println(disassembled);
    }

    @Test
    public void annotatedTest() {
        final String disassembled = disassemble("/AnnotatedTest_AnnotatedTestClass_Result.class");
        System.out.println(disassembled);
    }

    @Test
    public void annotationTest() {
        final String disassembled = disassemble("/AnnotationTest_MyAnnotation_Result.class");
        System.out.println(disassembled);
    }

    private String disassemble(String resourceName) {
        URL resource = DisassemblerTest.class.getResource(resourceName);

        String file = resource.getFile();
        try {
            if (file.isEmpty()) {
                InputStream inputStream = resource.openStream();
                byte[] bytes = ByteStreamsKt.readBytes(inputStream, ConstantsKt.DEFAULT_BUFFER_SIZE);

                return Disassembler.disassemble(bytes, true);
            } else {
                return Disassembler.disassemble(Paths.get(file), true, true);
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
