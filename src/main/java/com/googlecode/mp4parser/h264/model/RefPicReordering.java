/* Copyright */
package com.googlecode.mp4parser.h264.model;

/**
 * Contains reordering instructions for reference picture list
 *
 * @author Stanislav Vitvitskiy
 */
public class RefPicReordering {

    public static enum InstrType {
        FORWARD, BACKWARD, LONG_TERM
    }

    ;

    public static class ReorderOp {
        private InstrType type;
        private int param;

        public ReorderOp(InstrType type, int param) {
            this.type = type;
            this.param = param;
        }

        public InstrType getType() {
            return type;
        }

        public int getParam() {
            return param;
        }
    }

    private ReorderOp[] instructions;

    public RefPicReordering(ReorderOp[] instructions) {
        this.instructions = instructions;
    }

    public ReorderOp[] getInstructions() {
        return instructions;
    }
}
