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
package org.spongepowered.despector.ast.stmt.branch;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import org.spongepowered.despector.ast.AstVisitor;
import org.spongepowered.despector.ast.Locals.LocalInstance;
import org.spongepowered.despector.ast.stmt.Statement;
import org.spongepowered.despector.ast.stmt.StatementBlock;
import org.spongepowered.despector.ast.stmt.StatementVisitor;
import org.spongepowered.despector.util.serialization.AstSerializer;
import org.spongepowered.despector.util.serialization.MessagePacker;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

/**
 * A try-catch block.
 */
public class TryCatch implements Statement {

    private StatementBlock block;
    final List<CatchBlock> catch_blocks = Lists.newArrayList();

    public TryCatch(StatementBlock block) {
        this.block = checkNotNull(block, "block");
    }

    /**
     * Gets the body of the try block.
     */
    public StatementBlock getTryBlock() {
        return this.block;
    }

    /**
     * Sets the body of the try block.
     */
    public void setBlock(StatementBlock block) {
        this.block = checkNotNull(block, "block");
    }

    /**
     * Gets all attached catch blocks.
     */
    public List<CatchBlock> getCatchBlocks() {
        return this.catch_blocks;
    }

    @Override
    public void accept(AstVisitor visitor) {
        if (visitor instanceof StatementVisitor) {
            ((StatementVisitor) visitor).visitTryCatch(this);
        }
        for (Statement stmt : this.block.getStatements()) {
            stmt.accept(visitor);
        }
        for (CatchBlock catch_block : this.catch_blocks) {
            catch_block.accept(visitor);
        }
    }

    @Override
    public void writeTo(MessagePacker pack) throws IOException {
        pack.startMap(3);
        pack.writeString("id").writeInt(AstSerializer.STATEMENT_ID_TRY_CATCH);
        pack.writeString("body");
        pack.startArray(this.block.getStatementCount());
        for (Statement stmt : this.block.getStatements()) {
            stmt.writeTo(pack);
        }
        pack.endArray();
        pack.writeString("catch").startArray(this.catch_blocks.size());
        for (CatchBlock cat : this.catch_blocks) {
            pack.startMap(3);
            pack.writeString("exceptions").startArray(cat.getExceptions().size());
            for (String ex : cat.getExceptions()) {
                pack.writeString(ex);
            }
            pack.endArray();
            pack.writeString("block");
            pack.startArray(cat.getBlock().getStatementCount());
            for (Statement stmt : cat.getBlock().getStatements()) {
                stmt.writeTo(pack);
            }
            pack.endArray();
            if (cat.getExceptionLocal() != null) {
                pack.writeString("local");
                cat.getExceptionLocal().writeToSimple(pack);
            } else {
                pack.writeString("dummy_name").writeString(cat.getDummyName());
            }
            pack.endMap();
        }
        pack.endArray();
        pack.endMap();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TryCatch)) {
            return false;
        }
        TryCatch insn = (TryCatch) obj;
        return this.block.equals(insn.block) && this.catch_blocks.equals(insn.catch_blocks);
    }

    @Override
    public int hashCode() {
        int h = 1;
        h = h * 37 + this.block.hashCode();
        for (CatchBlock cat : this.catch_blocks) {
            h = h * 37 + cat.hashCode();
        }
        return h;
    }

    /**
     * A catch block.
     */
    public class CatchBlock {

        private final List<String> exceptions;
        private StatementBlock block;
        private LocalInstance exception_local;
        private String dummy_name;

        public CatchBlock(LocalInstance exception_local, List<String> ex, StatementBlock block) {
            this.exception_local = checkNotNull(exception_local, "local");
            this.dummy_name = null;
            this.exceptions = ex;
            this.block = block;
            TryCatch.this.catch_blocks.add(this);
        }

        public CatchBlock(String dummy_name, List<String> ex, StatementBlock block) {
            this.exception_local = null;
            this.dummy_name = checkNotNull(dummy_name, "name");
            this.exceptions = ex;
            this.block = block;
            TryCatch.this.catch_blocks.add(this);
        }

        /**
         * Gets the local that the exception is placed into.
         */
        @Nullable
        public LocalInstance getExceptionLocal() {
            return this.exception_local;
        }

        /**
         * Sets the local that the exception is placed into.
         */
        public void setExceptionLocal(@Nullable LocalInstance local) {
            if (local == null && this.dummy_name == null) {
                throw new IllegalStateException("Cannot have both a null exception local and dummy name in catch block.");
            }
            this.exception_local = local;
        }

        /**
         * Gets the dummy name for this variable. This name is ignored if the
         * {@link #getExceptionLocal()} is not null.
         */
        public String getDummyName() {
            if (this.exception_local != null) {
                return this.exception_local.getName();
            }
            return this.dummy_name;
        }

        /**
         * Sets the dummy name for this variable. This name is ignored if the
         * {@link #getExceptionLocal()} is not null.
         */
        public void setDummyName(String name) {
            if (name == null && this.exception_local == null) {
                throw new IllegalStateException("Cannot have both a null exception local and dummy name in catch block.");
            }
            this.dummy_name = name;
        }

        /**
         * Gets all exceptions that this catch block is catching.
         */
        public List<String> getExceptions() {
            return this.exceptions;
        }

        /**
         * Gets the body of this catch block.
         */
        public StatementBlock getBlock() {
            return this.block;
        }

        /**
         * Sets the body of this catch block.
         */
        public void setBlock(StatementBlock block) {
            this.block = checkNotNull(block, "block");
        }

        /**
         * Accepts the given visitor.
         */
        public void accept(AstVisitor visitor) {
            if (visitor instanceof StatementVisitor) {
                ((StatementVisitor) visitor).visitCatchBlock(this);
            }
            for (Statement stmt : this.block.getStatements()) {
                stmt.accept(visitor);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof CatchBlock)) {
                return false;
            }
            CatchBlock insn = (CatchBlock) obj;
            return this.exception_local.equals(insn.exception_local) && this.exceptions.equals(insn.exceptions) && this.block.equals(insn.block)
                    && this.dummy_name.equals(insn.dummy_name);
        }

        @Override
        public int hashCode() {
            int h = 1;
            h = h * 37 + this.exception_local.hashCode();
            h = h * 37 + this.exceptions.hashCode();
            h = h * 37 + this.block.hashCode();
            h = h * 37 + this.dummy_name.hashCode();
            return h;
        }

    }

}
