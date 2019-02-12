package com.firman;

import java.awt.event.KeyEvent;
import java.util.Random;

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

    /**
    * Set Vx = kk.
    * The interpreter puts the value kk into register Vx.
    */
    public static void i6xkk() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int kk = Main.opcode & 0x00FF; 
        Main.gprs[Vx] = kk;
    }

    /**
    * Set Vx = Vx + kk.
    * Adds the value kk to the value of register Vx,
    * then stores the result in Vx. 
    */
    public static void i7xkk() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int kk = Main.opcode & 0x00FF; 
        Main.gprs[Vx] += kk;
    }

    /**
    * Set Vx = Vy.
    * Stores the value of register Vy in register Vx.
    */
    public static void i8xy0() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int Vy = (Main.opcode & 0x00F0) >> 4; 
        Main.gprs[Vx] = Main.gprs[Vy];
    }

    /**
    * Set Vx = Vx OR Vy.
    * Performs a bitwise OR on the values of Vx and Vy,
    * then stores the result in Vx. A bitwise OR compares
    * the corrseponding bits from two values, and if either bit is 1,
    * then the same bit in the result is also 1. Otherwise, it is 0. 
    */
    public static void i8xy1() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int Vy = (Main.opcode & 0x00F0) >> 4; 
        Main.gprs[Vx] = Main.gprs[Vx] | Main.gprs[Vy];
    }

    /**
    * Set Vx = Vx AND Vy.
    * Performs a bitwise AND on the values of Vx and Vy,
    * then stores the result in Vx. A bitwise AND compares
    * the corrseponding bits from two values, and if both bits are 1,
    * then the same bit in the result is also 1. Otherwise, it is 0. 
    */
    public static void i8xy2() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int Vy = (Main.opcode & 0x00F0) >> 4; 
        Main.gprs[Vx] = Main.gprs[Vx] & Main.gprs[Vy];
    }

    /**
    * Set Vx = Vx XOR Vy.
    * Performs a bitwise exclusive OR on the values of Vx and Vy,
    * then stores the result in Vx. An exclusive OR compares
    * the corrseponding bits from two values, and if the bits
    * are not both the same, then the corresponding bit in
    * the result is set to 1. Otherwise, it is 0. 
    */
    public static void i8xy3() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int Vy = (Main.opcode & 0x00F0) >> 4; 
        Main.gprs[Vx] = Main.gprs[Vx] ^ Main.gprs[Vy];
    }

    /**
    * Set Vx = Vx + Vy, set VF = carry.
    * The values of Vx and Vy are added together.
    * If the result is greater than 8 bits (i.e., > 255,)
    * VF is set to 1, otherwise 0. Only the lowest 8 bits of the result
    * are kept,and stored in Vx.
    */
    public static void i8xy4() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int Vy = (Main.opcode & 0x00F0) >> 4; 
        int result = Main.gprs[Vx] + Main.gprs[Vy];
        if (result > 255) {
            Main.gprs[15] = 1;
        } else {
            Main.gprs[15] = 0;
        }
        Main.gprs[Vx] = result & 0x00FF;
    }

    /**
    * Set Vx = Vx - Vy, set VF = NOT borrow.
    * If Vx > Vy, then VF is set to 1, otherwise 0.
    * Then Vy is subtracted from Vx, and the results stored in Vx.
    */
    public static void i8xy5() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int Vy = (Main.opcode & 0x00F0) >> 4; 
        int result = Main.gprs[Vx] - Main.gprs[Vy];
        if (Main.gprs[Vx] > Main.gprs[Vy]) {
            Main.gprs[15] = 1;
        } else {
            Main.gprs[15] = 0;
        }
        Main.gprs[Vx] = result & 0x00FF;
    }

    /**
    * Set Vx = Vx SHR 1.
    * If the least-significant bit of Vx is 1,
    * then VF is set to 1, otherwise 0. Then Vx is divided by 2.
    */
    public static void i8xy6() {
        boolean lsb = (Main.opcode & 0x000F) == 1;
        if (lsb) {
            Main.gprs[15] = 1;
        } else {
            Main.gprs[15] = 0;
        }
        Main.gprs[Vx] = Main.gprs[Vx] >> 1;
    }

    /**
    * Set Vx = Vy - Vx, set VF = NOT borrow.
    * If Vy > Vx, then VF is set to 1, otherwise 0.
    * Then Vx is subtracted from Vy, and the results stored in Vx.
    */
    public static void i8xy5() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int Vy = (Main.opcode & 0x00F0) >> 4; 
        if (Main.gprs[Vy] > Main.gprs[Vx]) {
            Main.gprs[15] = 1;
        } else {
            Main.gprs[15] = 0;
        }
        Main.gprs[Vx] = Main.gprs[Vy] - Main.gprs[Vx];
    }

    /**
    * Set Vx = Vx SHL 1. If the most-significant bit of Vx is 1,
    * then VF is set to 1, otherwise to 0. Then Vx is multiplied by 2.
    */
    public static void i8xy6() {
        boolean lsb = (Main.opcode & 0x0080) == 1;
        if (lsb) {
            Main.gprs[15] = 1;
        } else {
            Main.gprs[15] = 0;
        }
        Main.gprs[Vx] = Main.gprs[Vx] << 1;
    }

    /**
    * Skip next instruction if Vx != Vy. 
    * The values of Vx and Vy are compared, and if they are not equal,
    * the program counter is increased by 2.
    */
    public static void i9xy0() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int Vy = (Main.opcode & 0x00F0) >> 4;
        if (Main.gprs[Vx] != Main.gprs[Vy]) {
            Main.pc++;
        }
    }

    /**
    * Set I = nnn.
    * The value of register I is set to nnn.
    */
    public static void iAnnn() {
        int nnn = Main.opcode & 0x0FFF;
        Main.i = nnn;
    }

    /**
    * Jump to location nnn + V0.
    * The program counter is set to nnn plus the value of V0.
    */
    public static void iBnnn() {
        int nnn = Main.opcode & 0x0FFF;
        Main.pc = nnn + Main.gprs[0];
    }

