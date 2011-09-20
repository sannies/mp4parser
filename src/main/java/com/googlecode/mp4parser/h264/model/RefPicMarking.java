/* Copyright */
package com.googlecode.mp4parser.h264.model;


/**
 * A script of instructions applied to reference picture list
 *
 * @author Stanislav Vitvitskiy
 */
public class RefPicMarking {

    public static enum InstrType {
        REMOVE_SHORT, REMOVE_LONG, CONVERT_INTO_LONG, TRUNK_LONG, CLEAR, MARK_LONG
    }

    ;

    public static class Instruction {
        private InstrType type;
        private int arg1;
        private int arg2;

        public Instruction(InstrType type, int arg1, int arg2) {
            this.type = type;
            this.arg1 = arg1;
            this.arg2 = arg2;
        }

        public InstrType getType() {
            return type;
        }

        public int getArg1() {
            return arg1;
        }

        public int getArg2() {
            return arg2;
        }
    }

    private Instruction[] instructions;

    public RefPicMarking(Instruction[] instructions) {
        this.instructions = instructions;
    }

    public Instruction[] getInstructions() {
        return instructions;
    }
}
