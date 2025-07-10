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
public class MyDebugBreakpointType extends XLineBreakpointType<MyBreakpointProperties> {

    public static final String ID = "my-debug-breakpoint";
    public static final String TITLE = "My Debug Breakpoint";

    public MyDebugBreakpointType() {
        super(ID, TITLE);
    }

    @Override
    public @Nullable MyBreakpointProperties createBreakpointProperties(@NotNull VirtualFile file, int line) {
        return new MyBreakpointProperties();
    }

    @Override
    public boolean isSuspendThreadSupported() {
        return true;
    }

    @Override
    public @Nullable XDebuggerEditorsProvider getEditorsProvider(@NotNull XLineBreakpoint<MyBreakpointProperties> breakpoint, @NotNull Project project) {
        return new JavaDebuggerEditorsProvider();
    }

    @Override
    public @Nullable MyBreakpointProperties createProperties() {
        return new MyBreakpointProperties();
    }

}