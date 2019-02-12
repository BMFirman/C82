package com.chip8.emulator;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.awt.event.KeyEvent;

public class Main extends JFrame {

    static int pc;
    static int i;
    static int opcode;
    static int[] memory;
    static int[] gprs;
    static int[] stack;
    static int stackPointer;
    static int delayTimer;
    static int soundTimer;
    static String filename;
    static InputHandler input;
    static boolean keyPressFlag;

    static boolean[][] displayGrid;
    static boolean[] keyboardState;

    private final int fps;
    private final int scale;
    private final int windowWidth;
    private final int windowHeight;

    private BufferedImage backBuffer;
    private Insets insets;

    /**
     * Initialize default values for the emulator.
     * Preferences for window size, cps, and most changable default
     * values are editable in the Main class constructor
     */
    private Main() {
        fps = 180;
        scale = 8;
        windowWidth = 64 * scale;
        windowHeight = 32 * scale;
        pc = 0x200;
        i = 0;
        memory = new int[4096];
        gprs = new int[16];
        displayGrid = new boolean[32][64];
        keyboardState = new boolean[16];
        stack = new int[16];
        stackPointer = 0;
        delayTimer = 0;
        soundTimer = 0;
        opcode = 0;
        keyPressFlag = false;
        filename = "INVADERS";
        // filename = args[0];
    }


    public static void main(String[] args) {
        Main m = new Main();
        initializeHexSprites();
        loadProgram();
        m.run();
        System.exit(0);
    }
    
    private void run() {

        initializeJFrame();

        boolean isRunning = true;

        while (isRunning) {
            long time = System.currentTimeMillis();
            // Clear the keyboard state before beginning cycle
            keyboardStateUpdater(0, false);
            update();
            draw();

            if (pc >= 4096) {
                emulateCycle(false);
                isRunning = false;
            } else {
                String before = Integer.toHexString(gprs[0]);
                // System.out.println("Before V0: " + before);
                emulateCycle(true);
                String after = Integer.toHexString(gprs[0]);
                // System.out.println("After V0: " + after);
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

    /**
     * Initialize the JFrame, insets are used to ensure the size of the frame
     * does not interfere with the OS window. This preserves a scale * (64,32)
     * display to be used by the emulator.
     */
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

    /**
     * Once a cycle draw the display onto the screen.
     * a backBuffer is used to provide double buffering.
     * This is not yet optimal as the displays refresh rate is tied to cps.
     * TODO draw according to a frame rate and execute at a defined rate.
     */
    private void draw() {
        Graphics g = getGraphics();
        Graphics bbg = backBuffer.getGraphics();

        bbg.setColor(Color.BLACK);
        bbg.fillRect(0, 0, windowWidth, windowHeight);
        bbg.drawRect(0, 0, scale, scale);

        for (int row = 0; row < windowHeight; row++) {
            for (int col = 0; col < windowWidth; col++) {
                if (Main.displayGrid[row / scale][col / scale]) {
                    bbg.setColor(Color.WHITE);
                } else {
                    bbg.setColor(Color.BLACK);
                }
                bbg.drawRect(col, row, scale, scale);
            }
        }
        g.drawImage(backBuffer, insets.left, insets.top, this);
    }

    /**
     * This method that runs once a cycle checks for keys that
     * are in the down position and runs the keyboardStateUpdater
     * method to update keyboardState[]
     */
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

    /**
     * Used at the start of every cycle with the False
     * flag to clear the keyboard, Used in conjunction with update()
     * once a cycle to detect keys that have been pressed.
     */
    private void keyboardStateUpdater(int i, boolean toggle) {
        if (toggle == true) {
            keyboardState[i] = true;
            keyPressFlag = true;
        } else {
            keyboardState = new boolean[16];
            keyPressFlag = false;
        }
    }

    /**
     * Determine the opcode from two memory addresses,
     * run the decoder using the resolved opcode, increment the pc,
     * lastly timers are incremented when they are greater than 0
     */
    private static void emulateCycle(boolean performCycle) {
        if (performCycle) {
            opcode = ((memory[pc] << 8) & 0xFF00) + ((memory[pc + 1]) & 0xFF);
            decoder();
            pc += 2;
            decrementTimers();
        }
    }

    /**
     * Load the program from a specified file
     * using a DataInputStream that takes in a FileInputStream
     * then the resultant byte[] generated from the file is copied into
     * memory starting at 0x200
     */
    private static void loadProgram() {
        String workingDirectory = System.getProperty("user.dir");
        System.out.println("Gradles user.dir is: " + workingDirectory);
        String pathname = workingDirectory + "/src/com/chip8/emulator/" + filename;
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

    /**
     * After each cycle times are decremented by 1 if they
     * are greater than 0
     */
    private static void decrementTimers() {
        if (delayTimer > 0) {
            delayTimer--;
        }
        if (soundTimer > 0) {
            soundTimer--;
        }
    }

    /**
     * Using bit-masking of the opcode the decoder runs the
     * appropriate method according to the given opcode
     */
    private static void decoder() {
        // String hexOpcode = Integer.toHexString(pc);
        // String hex = Integer.toHexString(opcode);
        // System.out.println(hexOpcode + " " + hex);

        if ((opcode & 0xF000) == 0x0000) {
            if ((opcode & 0xFFFF) == 0x00E0) {
                Instructions.i00E0();
            } else if ((opcode & 0xFFFF) == 0x00EE) {
                Instructions.i00EE();
            } else {
                Instructions.i0nnn();
            }
        } else if ((opcode & 0xF000) == 0x1000) {
            Instructions.i1nnn();
        } else if ((opcode & 0xF000) == 0x2000) {
            Instructions.i2nnn();
        } else if ((opcode & 0xF000) == 0x3000) {
            Instructions.i3xkk();
        } else if ((opcode & 0xF000) == 0x4000) {
            Instructions.i4xkk();
        } else if ((opcode & 0xF000) == 0x5000) {
            Instructions.i5xy0();
        } else if ((opcode & 0xF000) == 0x6000) {
            Instructions.i6xkk();
        } else if ((opcode & 0xF000) == 0x7000) {
            Instructions.i7xkk();
        } else if ((opcode & 0xF000) == 0x8000) {
            decoder8(opcode & 0x000F);
        } else if ((opcode & 0xF00F) == 0x9000) {
            Instructions.i9xy0();
        } else if ((opcode & 0xF000) == 0xA000) {
            Instructions.iAnnn();
        } else if ((opcode & 0xF000) == 0xB000) {
            Instructions.iBnnn();
        } else if ((opcode & 0xF000) == 0xC000) {
            Instructions.iCxkk();
        } else if ((opcode & 0xF000) == 0xD000) {
            Instructions.iDxyn();
        } else if ((opcode & 0xF0FF) == 0xE09E) {
            Instructions.iEx9E();
        } else if ((opcode & 0xF0FF) == 0xE0A1) {
            Instructions.iExA1();
        } else if ((opcode & 0xF000) == 0xF000) {
            decoderF(opcode & 0x00FF);
        }
    }

    /**
     * Simplification method used to determine which 0x8000
     * Instruction to use
     */
    private static void decoder8(int lsBits) {
        if (lsBits == 0) {
            Instructions.i8xy0();
        } else if (lsBits == 1) {
            Instructions.i8xy1();
        } else if (lsBits == 2) {
            Instructions.i8xy2();
        } else if (lsBits == 3) {
            Instructions.i8xy3();
        } else if (lsBits == 4) {
            Instructions.i8xy4();
        } else if (lsBits == 5) {
            Instructions.i8xy5();
        } else if (lsBits == 6) {
            Instructions.i8xy6();
        } else if (lsBits == 7) {
            Instructions.i8xy7();
        } else if (lsBits == 0xE) {
            Instructions.i8xyE();
        }
    }

    /**
     * Simplification method used to determine which 0xF000
     * Instruction to use
     */
    private static void decoderF(int lsByte) {
        if (lsByte == 0x07) {
            Instructions.iFx07();
        } else if (lsByte == 0x0A) {
            Instructions.iFx0A();
        } else if (lsByte == 0x15) {
            Instructions.iFx15();
        } else if (lsByte == 0x18) {
            Instructions.iFx18();
        } else if (lsByte == 0x1E) {
            Instructions.iFx1E();
        } else if (lsByte == 0x29) {
            Instructions.iFx29();
        } else if (lsByte == 0x33) {
            Instructions.iFx33();
        } else if (lsByte == 0x55) {
            Instructions.iFx55();
        } else if (lsByte == 0x65) {
            Instructions.iFx65();
        }
    }

    /**
     * The built in character sprites are placed in the first
     * 80 locations in memory, accessed with index * 5.
     */
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
                0xF0, 0x80, 0xF0, 0x80, 0x80  // F
        };

        for (int i = 0; i < 80; i++) {
            memory[i] = (int) fontSet[i];
        }
    }
}