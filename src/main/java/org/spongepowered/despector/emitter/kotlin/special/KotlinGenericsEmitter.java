/*
 * The MIT License (MIT)
 *
 * Copyright (c) Despector <https://despector.voxelgenesis.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.despector.emitter.kotlin.special;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.GenericClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.generic.TypeVariableSignature;
import org.spongepowered.despector.ast.generic.VoidTypeSignature;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.emitter.java.special.GenericsEmitter;
import org.spongepowered.despector.emitter.kotlin.KotlinEmitterUtil;

/**
 * An emitter for kotlin generics.
 */
public class KotlinGenericsEmitter extends GenericsEmitter {

    @Override
    public void emitTypeSignature(JavaEmitterContext ctx, TypeSignature sig, boolean is_varargs) {
        if (sig instanceof TypeVariableSignature) {
            int array_depth = 0;
            String type = ((TypeVariableSignature) sig).getIdentifier();
            while (type.startsWith("[")) {
                array_depth++;
                type = type.substring(1);
                ctx.printString("Array<");
            }
            ctx.printString(type.substring(1, type.length() - 1));
            for (int i = 0; i < array_depth; i++) {
                ctx.printString(">");
            }
        } else if (sig instanceof ClassTypeSignature) {
            ClassTypeSignature cls = (ClassTypeSignature) sig;
            int array_depth = 0;
            String type = cls.getType();
            while (type.startsWith("[")) {
                array_depth++;
                type = type.substring(1);
                ctx.printString("Array<");
            }
            KotlinEmitterUtil.emitType(ctx, type);
            for (int i = 0; i < array_depth; i++) {
                ctx.printString(">");
            }
        } else if (sig instanceof GenericClassTypeSignature) {
            GenericClassTypeSignature cls = (GenericClassTypeSignature) sig;
            int array_depth = 0;
            String type = cls.getType();
            while (type.startsWith("[")) {
                array_depth++;
                type = type.substring(1);
                ctx.printString("Array<");
            }
            KotlinEmitterUtil.emitType(ctx, type);
            emitTypeArguments(ctx, cls.getArguments());
            for (int i = 0; i < array_depth; i++) {
                ctx.printString(">");
            }
        } else if (sig instanceof VoidTypeSignature) {
            ctx.printString("void");
        }
    }

}
