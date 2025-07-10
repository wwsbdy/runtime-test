package com.zj.runtimetest.debug;


import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.breakpoints.XLineBreakpointType;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.java.debugger.JavaDebuggerEditorsProvider;

/**
 * @author jie.zhou
 */
public class RuntimeTestBreakpointType extends XLineBreakpointType<RuntimeTestBreakpointProperties> {

    public static final String ID = "runtime-test-breakpoint";
    public static final String TITLE = "Runtime Test Breakpoint";

    public RuntimeTestBreakpointType() {
        super(ID, TITLE);
    }

    @Override
    public @Nullable RuntimeTestBreakpointProperties createBreakpointProperties(@NotNull VirtualFile file, int line) {
        return new RuntimeTestBreakpointProperties();
    }

    @Override
    public boolean isSuspendThreadSupported() {
        return true;
    }

    @Override
    public @Nullable XDebuggerEditorsProvider getEditorsProvider(@NotNull XLineBreakpoint<RuntimeTestBreakpointProperties> breakpoint, @NotNull Project project) {
        return new JavaDebuggerEditorsProvider();
    }

    @Override
    public @Nullable RuntimeTestBreakpointProperties createProperties() {
        return new RuntimeTestBreakpointProperties();
    }

}