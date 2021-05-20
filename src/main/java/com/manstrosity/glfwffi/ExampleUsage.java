package com.manstrosity.glfwffi;

public class ExampleUsage {
    public static void main(String[] args) {
        if (!GLFW.init()) {
            throw new RuntimeException("Failed to initialize GLFW.");
        }

        // Maybe createWindow should throw an exception?
        GLFW.Window window = GLFW.createWindow(640, 480, "Hello World", null, null);
        assert( window != null );

        GLFW.makeContextCurrent(window);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
