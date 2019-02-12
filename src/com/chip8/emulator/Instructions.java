package com.firman;

import java.awt.event.KeyEvent;
import java.util.Random;
i

class Instructions {

    /**
    * Jump to a machine code routine at nnn.
    */
    public static void i0nnn() {
        Main.pc = Main.opcode & 0x0FFF;
    }

    /**
    * Clear the display.
    */
    public static void i00E0() {
        Main.displayGrid = new boolean[32][64];
    }

    /**
    * Return from a subroutine.
    * The interpreter sets the program counter to the
    * address at the top of the stack, then subtracts 1
    * from the stack pointer.
    */
    public static void i00EE() {
        Main.pc = Main.stack[Main.stackPointer];
        Main.stackPointer 
    }

    /**
    * Jump to location nnn.
    * The interpreter sets the program counter to nnn.
    */
    public static void i1nnn() {
        Main.pc = Main.opcode & 0x0FFF;
    }

    /**
    * Call subroutine at nnn.
    * The interpreter increments the stack pointer,
    * then puts the current PC on the top of the stack.
    * The PC is then set to nnn.
    */
    public static void i2nnn() {
        Main.stackPointer++;
        Main.stack[Main.stackPointer] = Main.pc;
        Main.pc = Main.opcode & 0x0FFF;
    }

    /**
    * Skip next instruction if Vx = kk.
    * The interpreter compares register Vx to kk,
    * and if they are equal, increments the program counter by 2.
    */
    public static void i3xkk() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int kk = Main.opcode & 0x00FF; 
        if (Main.gprs[Vx] == kk) {
            Main.pc += 2;
        }
    }

    /**
    * Skip next instruction if Vx != kk.
    * The interpreter compares register Vx to kk,
    * and if they are equal, increments the program counter by 2.
    */
    public static void i4xkk() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int kk = Main.opcode & 0x00FF; 
        if (Main.gprs[Vx] != kk) {
            Main.pc += 2;
        }
    }

    /**
    * Skip next instruction if Vx == Vy.
    * The interpreter compares register Vx to register Vy,
    * and if they are equal, increments the program counter by 2.
    */
    public static void i5xy0() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int Vy = (Main.opcode & 0x00F0) >> 4; 
        if (Main.gprs[Vx] != kk) {
            Main.pc += 2;
        }
    }



