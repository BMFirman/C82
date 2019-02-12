package com.chip8.emulator;

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
        Main.stackPointer--;
    }

    /**
     * Jump to location nnn.
     * The interpreter sets the program counter to nnn.
     */
    public static void i1nnn() {
        Main.pc = Main.opcode & 0x0FFF;
        Main.pc -= 2;
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
        Main.pc -= 2;
    }

    /**
     * Skip next instruction if Vx = kk.
     * The interpreter compares register Vx to kk,
     * and if they are equal, increments the program counter by 2.
     */
    public static void i3xkk() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int kk = Main.opcode & 0x00FF;
        if (Main.gprs[1] == 0x40 ) {
            Main.pc += 2;
        }
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
        if (Main.gprs[Vx] == Main.gprs[Vy]) {
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
        String hexOpcode2 = Integer.toHexString(Vx);
        String hexOpcode = Integer.toHexString(Main.gprs[Vx]);
        String hex = Integer.toHexString(kk);
        // System.out.println(hexOpcode2 + " " + hexOpcode + " " + hex);
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
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int Vy = (Main.opcode & 0x00F0) >> 4;
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
    public static void i8xy7() {
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
    public static void i8xyE() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int Vy = (Main.opcode & 0x00F0) >> 4;
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

    /**
     * Set Vx = random byte AND kk. The interpreter generates
     * a random number from 0 to 255, which is then ANDed with the value kk.
     * The results are stored in Vx.
     */
    public static void iCxkk() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int kk = Main.opcode & 0x00FF;
        Random randomByteObject = new Random();
        int randomByte = randomByteObject.nextInt(255);
        Main.gprs[Vx] = randomByte & kk;
    }

    /**
     * Display n-byte sprite starting at memory location I
     * at (Vx, Vy), set VF = collision.
     */
    public static void iDxyn() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        int Vy = (Main.opcode & 0x00F0) >> 4;
        int n = Main.opcode & 0x000F;
        int xPos = Main.gprs[Vx];
        int yPos = Main.gprs[Vy];
        // System.out.println("I tried to draw a sprite");

        for (int i = 0; i < n; i++) {
            boolean[] bitsInByte = new boolean[8];
            byte retrievedByte = (byte) Main.memory[Main.i + i];
            bitsInByte[0] = (retrievedByte & 0x0080) > 0;
            bitsInByte[1] = (retrievedByte & 0x0040) > 0;
            bitsInByte[2] = (retrievedByte & 0x0020) > 0;
            bitsInByte[3] = (retrievedByte & 0x0010) > 0;
            bitsInByte[4] = (retrievedByte & 0x0008) > 0;
            bitsInByte[5] = (retrievedByte & 0x0004) > 0;
            bitsInByte[6] = (retrievedByte & 0x0002) > 0;
            bitsInByte[7] = (retrievedByte & 0x0001) > 0;

            for (int z = 0; z < 8; z++) {
                if ((yPos + i > 31) | (xPos + z > 63)) {
                    break;
                } else {
                    Main.displayGrid[yPos + i][xPos + z] = Main.displayGrid[yPos + i][xPos + z] ^ bitsInByte[z];
                    if (Main.displayGrid[yPos + i][xPos + z] ^ bitsInByte[z] == false) {
                        Main.gprs[15] = 1;
                    }
                }
            }
            /*
            for (int x = 0; x < 8; x++) {
                // Set VF to 1 if a screen pixel flipped from active to inactive
                if ((yPos + i) > 31 | (xPos + 8 - x) > 63) {
                    break;
                } else {
                    if ((Main.displayGrid[Vy + i][Vx + 8 - x] ^ bitsInByte[x]) == false) {
                        Main.gprs[15] = 1;
                    }
                    // System.out.println("Sprite was drawn to the screen at: " + (xPos + 8 - x)  + " " + (yPos + i));
                    Main.displayGrid[yPos + i][xPos + 8 - x] = Main.displayGrid[yPos + i][xPos + 8 - x] ^ bitsInByte[x];
                }
            }
            */
        }
    }

    /**
     * Skip next instruction if key with the value of Vx is pressed.
     * Checks the keyboard, and if the key corresponding
     * to the value of Vx is currently in the down position,
     * PC is increased by 2.
     */
    public static void iEx9E() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        if (Main.keyboardState[Main.gprs[Vx]]) {
            Main.pc += 2;
        }
    }

    /**
     * Skip next instruction if key with the value of Vx is not pressed.
     * Checks the keyboard, and if the key corresponding
     * to the value of Vx is currently in the up position,
     * PC is increased by 2.
     */
    public static void iExA1() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        if (!Main.keyboardState[Main.gprs[Vx]]) {
            Main.pc += 2;
        }
    }

    /**
     * Set Vx = delay timer value.
     * The value of DT is placed into Vx.
     */
    public static void iFx07() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        Main.gprs[Vx] = Main.delayTimer;
    }

    /**
     * Wait for a key press, store the value of the key in Vx.
     * All execution stops until a key is pressed,
     * then the value of that key is stored in Vx.
     */
    public static void iFx0A() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        boolean flag = true;
        while (flag) {
            if (Main.input.isKeyDown(KeyEvent.VK_S)) {
                flag = afterKeyPress(0, Vx);
            } else if (Main.input.isKeyDown(KeyEvent.VK_1)) {
                flag = afterKeyPress(1, Vx);
            } else if (Main.input.isKeyDown(KeyEvent.VK_2)) {
                flag = afterKeyPress(2, Vx);
            } else if (Main.input.isKeyDown(KeyEvent.VK_3)) {
                flag = afterKeyPress(3, Vx);
            } else if (Main.input.isKeyDown(KeyEvent.VK_4)) {
                flag = afterKeyPress(4, Vx);
            } else if (Main.input.isKeyDown(KeyEvent.VK_Q)) {
                flag = afterKeyPress(5, Vx);
            } else if (Main.input.isKeyDown(KeyEvent.VK_W)) {
                flag = afterKeyPress(6, Vx);
            } else if (Main.input.isKeyDown(KeyEvent.VK_E)) {
                flag = afterKeyPress(7, Vx);
            } else if (Main.input.isKeyDown(KeyEvent.VK_R)) {
                flag = afterKeyPress(8, Vx);
            } else if (Main.input.isKeyDown(KeyEvent.VK_A)) {
                flag = afterKeyPress(9, Vx);
            } else if (Main.input.isKeyDown(KeyEvent.VK_D)) {
                flag = afterKeyPress(10, Vx);
            } else if (Main.input.isKeyDown(KeyEvent.VK_F)) {
                flag = afterKeyPress(11, Vx);
            } else if (Main.input.isKeyDown(KeyEvent.VK_Z)) {
                flag = afterKeyPress(12, Vx);
            } else if (Main.input.isKeyDown(KeyEvent.VK_X)) {
                flag = afterKeyPress(13, Vx);
            } else if (Main.input.isKeyDown(KeyEvent.VK_C)) {
                flag = afterKeyPress(14, Vx);
            } else if (Main.input.isKeyDown(KeyEvent.VK_V)) {
                flag = afterKeyPress(15, Vx);
            }
        }
    }

    /**
     * Used only in conjunction with iFx0A
     * Clears keyboard state after keypress and places value
     * back into Vx.
     */
    private static boolean afterKeyPress(int i, int Vx) {
        Main.keyboardState = new boolean[16];
        Main.gprs[Vx] = i;
        // System.out.println("Value stored in " + i + " " + Main.gprs[Vx]);
        return false;
    }

    /**
     * Set delay timer = Vx.
     * DT is set equal to the value of Vx.
     */
    public static void iFx15() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        Main.delayTimer = Main.gprs[Vx];
    }

    /**
     * Set sound timer = Vx.
     * ST is set equal to the value of Vx.
     */
    public static void iFx18() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        Main.soundTimer = Main.gprs[Vx];
    }

    /**
     * Set I = I + Vx.
     * The values of I and Vx are added, and the results are stored in I.
     */
    public static void iFx1E() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        Main.i = (Main.i + Main.gprs[Vx]) & 0xFFFF;
        // String hexOpcode = Integer.toHexString(Main.i);
        // System.out.println("i: " + hexOpcode);
    }

    /**
     * Set I = location of sprite for digit Vx.
     * The value of I is set to the location for the hexadecimal
     * sprite corresponding to the value of Vx. See section 2.4,
     * Display, for more information on the Chip-8 hexadecimal font.
     */
    public static void iFx29() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        Main.i = Main.gprs[Vx] * 5;
    }

    /**
     * Store BCD representation of Vx in memory locations I, I+1, and I+2.
     * The interpreter takes the decimal value of Vx,
     * and places the hundreds digit in memory at location in I,
     * the tens digit at location I+1, and the ones digit at location I+2.
     */
    public static void iFx33() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        Main.memory[Main.i] = Main.gprs[Vx] / 100;
        Main.memory[Main.i + 1] = (Main.gprs[Vx] % 100) / 10;
        Main.memory[Main.i + 2] = (Main.gprs[Vx] % 100) % 10;
    }

    /**
     * Store registers V0 through Vx in memory starting at location I.
     * The interpreter copies the values of registers V0 through Vx
     * into memory, starting at the address in I.
     */
    public static void iFx55() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        System.arraycopy(Main.gprs, 0, Main.memory, Main.i, Vx);
    }

    /**
     * Fx65 - LD Vx, [I]
     * Read registers V0 through Vx from memory starting at location I.
     * The interpreter reads values from memory starting at location I
     * into registers V0 through Vx.
     */
    public static void iFx65() {
        int Vx = (Main.opcode & 0x0F00) >> 8;
        // System.out.println(Vx);
        System.arraycopy(Main.memory, Main.i, Main.gprs, 0, Vx);
    }
}