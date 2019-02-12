package com.chip8.emulator;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class Main extends JFrame {
    
    static int pc;
    static int i;
    static int opcode;
    static int[] memory;
    static int[] generalPurposeRegisters;
    static int[] stack;
    static int stackPointer;
    static int delayTimer;
    static int soundTimer;

    static boolean[][] displayGrid;
    static boolean[] keyboardState;

    private final String filename;
    private final int fps;
    private final int scale;
    private final int windowWidth;
    private final int windowHeight;

    private BufferedImage backBuffer;
    private Insets insets;
    static InputHandler input;

    private Main() {
        fps = 60;
        scale = 8;
        windowWidth = 64 * scale;
        windowHeight = 32 * scale;
        filename = args[0];
    }

    public static void main(String[] args) {
        intializeEmulator();
        Main m = new Main();
        m.run();
        System.exit(0);
    }

    private void run() {

        initializeJFrame();
        boolean isRunning = true;

        while (isRunning) {
            long time = System.currentTimeMillis();
            // Clear the keyboard state before begining cycle
            keyboardStateUpdater(0, false);
            update();
            draw();

            if (pc >= 4096) {
                emulateCycle(false);
                isRunning = false;
            } else {
                emulateCycle(true);
            }

            time = (1000 / fps) - (System.currentTimeMillis() - time);

            if (time > 0) {
                try {
                    Thread.sleep(time);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void initializeJFrame() {
        setTitle("Chip8");
        setSize(windowWidth, windowHeight);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        insets = getInsets();
        setSize(insets.left + windowWidth + insets.right, insets.top + windowHeight + insets.bottom);

        backBuffer = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
        input = new InputHandler(this);
    }

    private void draw() {
        Graphics g = getGraphics();
        Graphics bbg = backBuffer.getGraphics();

        bbg.setColor(Color.BLACK);
        bbg.fillRect(0, 0, windowWidth, windowHeight);

        for (int row = 0; row < windowHeight; row++) {
            for (int col = 0; col < windowWidth; col++) {
                if (Main.displayGrid[row / scale][col / scale]) {
                    bbg.setColor(Color.WHITE);
                } else {
                    bbg.setColor(Color.BLACK);
                }
                bbg.drawRect(col-scale, row, scale, scale);
            }
        }
        g.drawImage(backBuffer, insets.left, insets.top, this);
    }

    private void update() {
        if (Main.input.isKeyDown(KeyEvent.VK_S)) {
            keyboardStateUpdater(0, true);
        } else if (Main.input.isKeyDown(KeyEvent.VK_1)) {
            keyboardStateUpdater(1, true);
        } else if (Main.input.isKeyDown(KeyEvent.VK_2)) {
            keyboardStateUpdater(2, true);
        } else if (Main.input.isKeyDown(KeyEvent.VK_3)) {
            keyboardStateUpdater(3, true);
        } else if (Main.input.isKeyDown(KeyEvent.VK_4)) {
            keyboardStateUpdater(4, true);
        } else if (Main.input.isKeyDown(KeyEvent.VK_Q)) {
            keyboardStateUpdater(5, true);
        } else if (Main.input.isKeyDown(KeyEvent.VK_W)) {
            keyboardStateUpdater(6, true);
        } else if (Main.input.isKeyDown(KeyEvent.VK_E)) {
            keyboardStateUpdater(7, true);
        } else if (Main.input.isKeyDown(KeyEvent.VK_R)) {
            keyboardStateUpdater(8, true);
        } else if (Main.input.isKeyDown(KeyEvent.VK_A)) {
            keyboardStateUpdater(9, true);
        } else if (Main.input.isKeyDown(KeyEvent.VK_D)) {
            keyboardStateUpdater(0xA, true);
        } else if (Main.input.isKeyDown(KeyEvent.VK_F)) {
            keyboardStateUpdater(0xB, true);
        } else if (Main.input.isKeyDown(KeyEvent.VK_Z)) {
            keyboardStateUpdater(0xC, true);
        } else if (Main.input.isKeyDown(KeyEvent.VK_X)) {
            keyboardStateUpdater(0xD, true);
        } else if (Main.input.isKeyDown(KeyEvent.VK_C)) {
            keyboardStateUpdater(0xE, true);
        } else if (Main.input.isKeyDown(KeyEvent.VK_V)) {
            keyboardStateUpdater(0xF, true);
        }
    }

    private void keyboardStateUpdater(int i, boolean toggle) {
        if (toggle == true) {
            keyboardState[i] = true;
            keyPressFlag = true;
        } else {
            keyboardState = new boolean[16];
            keyPressFlag = false;
        }
    }

    private static void emulateCycle(boolean performCycle) {
        if (performCycle) {
            opcode = ((memory[pc] << 8) & 0xFF00) + ((memory[pc + 1]) & 0xFF);
            decoder();
            pc += 2;
            decrementTimers();
        }
    }

    private static void loadProgram() {
        String workingDirectory = System.getProperty("user.dir");
        System.out.println("Gradles user.dir is: " + workingDirectory);
        String pathname = workingDirectory + "/src/com/firman/" + filename;
        File file = new File(pathname);
        byte[] fileData = new byte[(int) file.length()];
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(file));
            try {
                dis.readFully(fileData);
                dis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int max = fileData.length;
            for (int i = 0; i < max; i++) {
                memory[i + 512] = fileData[i] & 0xFFFF;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void decrementTimers() {
        if (delayTimer > 0) {
            delayTimer--;
        }
        if (soundTimer > 0) {
            soundTimer--;
        }
    }

    private static void initializeEmulator() {
        pc = 0x200;
        i = 0;

        memory = new int[4096];
        generalPurposeRegisters = new int[16];
        displayGrid = new boolean[32][64];
        keyboardState = new boolean[16];
        stack = new int[16];
        stackPointer = 0;
        delayTimer = 0;
        soundTimer = 0;
        opcode = 0;
        keyPressFlag = false;

        filename = "INVADERS";

        initializeHexSprites();
        loadProgram();
    }

    private static void decoder() {
        // String hex = Integer.toHexString(opcode);
        // System.out.println(pc + " " + hex);

        if ((opcode & 0xFFFF) == 0x00E0) {
            Instructions.i00E0();
        } else if ((opcode & 0xFFFF) == 0x00EE) {
            Instructions.i00EE();
        } else if ((opcode & 0xF000) == 0x1000) {
            Instructions.i1nnn();
        } else if ((opcode & 0xF000) == 0x2000) {
            Instructions.i2nnn();
        } else if ((opcode & 0xF000) == 0x3000) {
            Instructions.i3xkk();
        } else if ((opcode & 0xF000) == 0x4000) {
            Instructions.i4xkk();
        } else if ((opcode & 0xF000) == 0x5000) {
            Instructions.i5xy0(Vx, Vy);
        } else if ((opcode & 0xF000) == 0x6000) {
            Instructions.i6xkk(Vx, kk);
        } else if ((opcode & 0xF000) == 0x7000) {
            Instructions.i7xkk(Vx, kk);
        } else if ((opcode & 0xF000) == 0x8000) {
            decoder8(opcode & 0x000F);
        } else if ((opcode & 0xF00F) == 0x9000) {
            Instructions.i9xy0();
        } else if ((opcode & 0xF000) == 0xA000) {
            Instructions.iAnnn();
        } else if ((opcode & 0xF000) == 0xB000) {
            Instructions.iBnnn(nnn);
        } else if ((opcode & 0xF000) == 0xC000) {
            Instructions.iCxkk(Vx, kk);
        } else if ((opcode & 0xF000) == 0xD000) {
            Instructions.iDxyn(Vx, Vy, n);
        } else if ((opcode & 0xF0FF) == 0xE09E) {
            Instructions.iEx9E(Vx);
        } else if ((opcode & 0xF0FF) == 0xE0A1) {
            Instructions.iExA1(Vx);
        } else if ((opcode & 0xF000) == 0xF000) {
            decoderF(opcode & 0x00FF);
        }
    }

    private static void decoder8(int lsBits) {
        if (lsBits == 0) {
            Instructions.i8xy0(Vx, Vy);
        } else if (lsBits == 1) {
            Instructions.i8xy1(Vx, Vy);
        } else if (lsBits == 2) {
            Instructions.i8xy2(Vx, Vy);
        } else if (lsBits == 3) {
            Instructions.i8xy3(Vx, Vy);
        } else if (lsBits == 4) {
            Instructions.i8xy4(Vx, Vy);
        } else if (lsBits == 5) {
            Instructions.i8xy5(Vx, Vy);
        } else if (lsBits == 6) {
            Instructions.i8xy6(Vx);
        } else if (lsBits == 7) {
            Instructions.i8xy7(Vx, Vy);
        } else if (lsBits == 0xE) {
            Instructions.i8xyE(Vx);
        }
    }

    private static void decoderF(int lsByte) {
        if (lsByte == 0x07) {
            Instructions.iFx07(Vx);
        } else if (lsByte == 0x0A) {
            Instructions.iFx0A(Vx);
        } else if (lsByte == 0x15) {
            Instructions.iFx15(Vx);
        } else if (lsByte == 0x18) {
            Instructions.iFx18(Vx);
        } else if (lsByte == 0x1E) {
            Instructions.iFx1E(Vx);
        } else if (lsByte == 0x29) {
            Instructions.iFx29(Vx);
        } else if (lsByte == 0x33) {
            Instructions.iFx33(Vx);
        } else if (lsByte == 0x55) {
            Instructions.iFx55(Vx);
        } else if (lsByte == 0x65) {
            Instructions.iFx65(Vx);
        }
    }

    private static void initializeHexSprites() {
        char[] fontSet = {
                0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
                0x20, 0x60, 0x20, 0x20, 0x70, // 1
                0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
                0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
                0x90, 0x90, 0xF0, 0x10, 0x10, // 4
                0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
                0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
                0xF0, 0x10, 0x20, 0x40, 0x40, // 7
                0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
                0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
                0xF0, 0x90, 0xF0, 0x90, 0x90, // A
                0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
                0xF0, 0x80, 0x80, 0x80, 0xF0, // C
                0xE0, 0x90, 0x90, 0x90, 0xE0, // D
                0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
                0xF0, 0x80, 0xF0, 0x80, 0x80 // F
        };

        for (int i = 0; i < 80; i++) {
            memory[i] = (int) fontSet[i];
        }
    }
}