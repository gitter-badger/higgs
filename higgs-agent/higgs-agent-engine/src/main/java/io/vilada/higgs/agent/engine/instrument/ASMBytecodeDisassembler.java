/*
 * Copyright 2018 The Higgs Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vilada.higgs.agent.engine.instrument;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author ethan
 */
public class ASMBytecodeDisassembler {

    private final int cwFlag;
    private final int crFlag;

    public ASMBytecodeDisassembler() {
        this(0, 0);
    }

    public ASMBytecodeDisassembler(int crFlag, int cwFlag) {
        this.cwFlag = cwFlag;
        this.crFlag = crFlag;
    }

    public String dumpBytecode(final byte[] bytecode) {
        if (bytecode == null) {
            throw new NullPointerException("bytecode must not be null");
        }

        return writeBytecode(bytecode, new Textifier());
    }


    public String dumpASM(byte[] bytecode) {
        if (bytecode == null) {
            throw new NullPointerException("bytecode must not be null");
        }

        return writeBytecode(bytecode, new ASMifier());
    }

    private String writeBytecode(byte[] bytecode, Printer printer) {

        final StringWriter out = new StringWriter();
        final PrintWriter writer = new PrintWriter(out);

        accept(bytecode, printer, writer);

        return out.toString();
    }

    private void accept(byte[] bytecode, Printer printer, PrintWriter writer) {

        final ClassReader cr = new ClassReader(bytecode);
        final ClassWriter cw = new ClassWriter(this.cwFlag);
        final TraceClassVisitor tcv = new TraceClassVisitor(cw, printer, writer);
        cr.accept(tcv, this.crFlag);
    }

    public String dumpVerify(byte[] bytecode, ClassLoader classLoader) {
        if (bytecode == null) {
            throw new NullPointerException("bytecode must not be null");
        }
        if (classLoader == null) {
            throw new NullPointerException("classLoader must not be null");
        }

        final StringWriter out = new StringWriter();
        final PrintWriter writer = new PrintWriter(out);

        final ClassReader cr = new ClassReader(bytecode);
        CheckClassAdapter.verify(cr, classLoader, true, writer);

        return out.toString();
    }


}
