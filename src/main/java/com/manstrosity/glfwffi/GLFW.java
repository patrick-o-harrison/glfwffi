package com.manstrosity.glfwffi;

import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.LibraryLookup;
import jdk.incubator.foreign.MemoryAddress;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class GLFW {
    public static final int FALSE = 0;
    public static final int TRUE = 1;

    private final static MethodHandle glfwInitHandle;
    private final static MethodHandle glfwTerminateHandle;
    private final static MethodHandle glfwCreateWindowHandle;
    private final static MethodHandle glfwMakeContextCurrentHandle;

    static {
        var libglfw = LibraryLookup.ofLibrary("glfw");
        var linker = CLinker.getInstance();

        glfwInitHandle = setupGlfwInit(libglfw, linker);
        glfwTerminateHandle = setupGlfwTerminate(libglfw, linker);
        glfwCreateWindowHandle = setupGlfwCreateWindow(libglfw, linker);
        glfwMakeContextCurrentHandle = setupGlfwMakeContextCurrentHandle(libglfw, linker);
    }

    private GLFW() {
    }

    private static @NotNull MethodHandle setupGlfwMakeContextCurrentHandle(@NotNull LibraryLookup libglfw, @NotNull CLinker linker) {
        var glfwMakeContextCurrentSymbol = loadSymbol(libglfw, "glfwMakeContextCurrent");
        var methodType = MethodType.methodType(void.class, MemoryAddress.class);
        var functionDescriptor = FunctionDescriptor.ofVoid(CLinker.C_POINTER);

        return linker.downcallHandle(glfwMakeContextCurrentSymbol, methodType, functionDescriptor);
    }

    private static @NotNull MethodHandle setupGlfwInit(@NotNull LibraryLookup libglfw, @NotNull CLinker linker) {
        var glfwInitSymbol = loadSymbol(libglfw, "glfwInit");
        var methodType = MethodType.methodType(int.class);
        var functionDescriptor = FunctionDescriptor.of(CLinker.C_INT);

        return linker.downcallHandle(glfwInitSymbol, methodType, functionDescriptor);
    }

    private static @NotNull MethodHandle setupGlfwTerminate(@NotNull LibraryLookup libglfw, @NotNull CLinker linker) {
        var glfwTerminateSymbol = loadSymbol(libglfw, "glfwTerminate");
        var methodType = MethodType.methodType(void.class);
        var functionDescriptor = FunctionDescriptor.ofVoid();

        return linker.downcallHandle(glfwTerminateSymbol, methodType, functionDescriptor);
    }

    private static @NotNull MethodHandle setupGlfwCreateWindow(@NotNull LibraryLookup libglfw, @NotNull CLinker linker) {
        var glfwCreateWindowSymbol = loadSymbol(libglfw, "glfwCreateWindow");
        var methodType = MethodType.methodType(
                MemoryAddress.class,
                int.class,
                int.class,
                MemoryAddress.class,
                MemoryAddress.class,
                MemoryAddress.class
        );
        var functionDescriptor = FunctionDescriptor.of(
                CLinker.C_POINTER,
                CLinker.C_INT,
                CLinker.C_INT,
                CLinker.C_POINTER,
                CLinker.C_POINTER,
                CLinker.C_POINTER
        );

        return linker.downcallHandle(glfwCreateWindowSymbol, methodType, functionDescriptor);
    }

    private static @NotNull LibraryLookup.Symbol loadSymbol(@NotNull LibraryLookup libraryLookup, @NotNull String symbolName) {
        var symbol = libraryLookup.lookup(symbolName);
        if (symbol.isEmpty()) {
            throw new RuntimeException("Failed to lookup symbol `%s`".formatted(symbolName));
        }
        return symbol.get();
    }

    public static boolean init() {
        int result;
        try {
            result = (int) glfwInitHandle.invokeExact();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        }
        return result == TRUE;
    }

    public static void terminate() {
        try {
            glfwTerminateHandle.invokeExact();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    // TODO: Maybe this should be @NotNull.  Throwing an exception instead of returning null seems better.
    public static Window createWindow(int width, int height, @NotNull String title, Monitor monitor, Window share) {
        try {
            // TODO: Let's figure out how to create NativeScope objects and use them here.
            var titlestr = CLinker.toCString(title);
            var monitorAddress = monitor != null ? monitor.address : MemoryAddress.NULL;
            var shareAddress = share != null ? share.address : MemoryAddress.NULL;
            var windowAddress = (MemoryAddress) glfwCreateWindowHandle.invokeExact(width, height, titlestr.address(), monitorAddress, shareAddress);
            if (windowAddress == MemoryAddress.NULL) {
                // TODO: Let's do some error checking here.
                return null;
            }
            return new Window(windowAddress);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    public static void makeContextCurrent(@NotNull Window window) {
        try {
            glfwMakeContextCurrentHandle.invokeExact(window.address);
        } catch (Throwable throwable) {
            // TODO: Perhaps we should throw an exception here.
            throwable.printStackTrace();
        }
    }

    public static class Window {
        private final MemoryAddress address;

        private Window(@NotNull MemoryAddress address) {
            this.address = address;
        }
    }

    public static class Monitor {
        private final MemoryAddress address;

        private Monitor(@NotNull MemoryAddress address) {
            this.address = address;
        }

    }
}
