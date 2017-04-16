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
package org.spongepowered.despector.decompiler.ir;

import org.spongepowered.despector.util.DebugUtil;

import java.util.Map;

public class SwitchInsn extends Insn {

    private final Map<Integer, Integer> targets;
    private int dflt;

    public SwitchInsn(int op, Map<Integer, Integer> targets, int dflt) {
        super(op);
        this.targets = targets;
        this.dflt = dflt;
    }

    public Map<Integer, Integer> getTargets() {
        return this.targets;
    }

    public int getDefault() {
        return this.dflt;
    }

    public void setDefault(int target) {
        this.dflt = target;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(DebugUtil.opcodeToString(this.opcode));
        return str.toString();
    }

}
