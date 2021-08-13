/**
 *      BytecodeDisassembler - A bytecode printer written on top of Java ASM <https://github.com/JonathanxD/BytecodeDisassembler>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2021 JonathanxD <jonathan.scripter@programmer.net>
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
public class IndyConcat {

    private static final int b = compute();
    private static final String CONS = "AAA" + b;

    public static String concat(int a, int b, String c) {
        return ""+a+""+c+""+b;
    }

    public static String concatObjs(String a, double b, Object... objs) {
        return a+""+b+""+objs;
    }
    
    public static String concatObjs(String a, double b, int x, Object... objs) {
        return a+""+x+""+b+""+objs;
    }

    public static String concatObjs2(String a, double b, int x, Object... objs) {
        return a+" - x - "+x+" - x2 - "+b+" - x3 - "+objs;
    }

    public static String concatObjs2Cons(String a, double b, int x, Object... objs) {
        return a+" - x - "+x+" - x2 - "+b+" - x3 - "+objs+CONS;
    }
    
    private static int compute() {
        return 8;
    }
    
    private static String haba(String a, String b) {
       return a + b;
    }
    
    private static String habaUe(String a, String b) {
       return ""+""+"";
    }
    
    private static String habaUe2(String a, String b) {
       return ""+""+"" + String.class;
    }

    private static String habaUe23(String a, String b) {
       return a + b + "a"+"b"+a+"a"+b+"a"+a+"b";
    }
    
    private static String kkk() {
       String a = "hello";
       char c = ' ';
       String b = "world";
       return a + c + b;
    }

}
