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
package org.spongepowered.despector.decompiler.method.graph.create;

import org.spongepowered.despector.decompiler.ir.Insn;
import org.spongepowered.despector.decompiler.ir.InsnBlock;
import org.spongepowered.despector.decompiler.ir.JumpInsn;
import org.spongepowered.despector.decompiler.method.PartialMethod;
import org.spongepowered.despector.decompiler.method.graph.GraphOperation;
import org.spongepowered.despector.decompiler.method.graph.GraphProducerStep;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.ConditionalOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.GotoOpcodeBlock;
import org.spongepowered.despector.decompiler.method.graph.data.opcode.OpcodeBlock;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A graph producer that creates opcode blocks for condition and unconditional
 * jumps.
 */
public class JumpGraphProducerStep implements GraphProducerStep {

    @Override
    public void collectBreakpoints(PartialMethod partial, Set<Integer> break_points) {
        InsnBlock instructions = partial.getOpcodes();

        for (int i = 0; i < instructions.size(); i++) {
            Insn next = instructions.get(i);
            if (next instanceof JumpInsn) {
                break_points.add(i);
                // also break before labels targetted by jump opcodes to have a
                // break between the body of an if block and the statements
                // after it
                break_points.add(((JumpInsn) next).getTarget());
                continue;
            }
            int op = next.getOpcode();
            if (op == Insn.RETURN || op == Insn.ARETURN) {
                break_points.add(i);
            }
        }
    }

    @Override
    public void formEdges(PartialMethod partial, Map<Integer, OpcodeBlock> blocks, List<Integer> sorted_break_points, List<OpcodeBlock> block_list) {
        for (Map.Entry<Integer, OpcodeBlock> e : blocks.entrySet()) {
            // Now we go through and form an edge from any block and the block
            // it flows (or jumps) into next.
            OpcodeBlock block = e.getValue();
            if (block.getLast() instanceof JumpInsn) {
                int label = ((JumpInsn) block.getLast()).getTarget();
                if(block.getLast().getOpcode() == Insn.GOTO) {
                    GotoOpcodeBlock replacement = new GotoOpcodeBlock(block.getBreakpoint());
                    e.setValue(replacement);
                    block_list.set(block_list.indexOf(block), replacement);
                    replacement.getOpcodes().addAll(block.getOpcodes());
                    replacement.setTarget(blocks.get(sorted_break_points.get(sorted_break_points.indexOf(label) + 1)));
                    GraphOperation.remap(block_list, block, replacement);
                } else {
                    ConditionalOpcodeBlock replacement = new ConditionalOpcodeBlock(block.getBreakpoint());
                    e.setValue(replacement);
                    block_list.set(block_list.indexOf(block), replacement);
                    replacement.getOpcodes().addAll(block.getOpcodes());
                    replacement.setTarget(blocks.get(sorted_break_points.get(sorted_break_points.indexOf(label) + 1)));
                    OpcodeBlock next = blocks.get(sorted_break_points.get(sorted_break_points.indexOf(e.getKey()) + 1));
                    replacement.setElseTarget(next);
                    GraphOperation.remap(block_list, block, replacement);
                }
            }
        }
    }

}
