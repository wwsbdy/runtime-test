package com.zj.runtimetest.ui.debug;


import com.intellij.debugger.ui.breakpoints.Breakpoint;
import com.intellij.debugger.ui.breakpoints.JavaMethodBreakpointType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.debugger.breakpoints.properties.JavaMethodBreakpointProperties;

/**
 * @author jie.zhou
 */
public class RuntimeTestBreakpointType extends JavaMethodBreakpointType {

    public static final String ID = "runtime-test-breakpoint";
    public static final String TITLE = "Runtime Test Breakpoint";

    public RuntimeTestBreakpointType() {
        super(ID, TITLE);
    }
    // idea version 243
//    @Override
//    public @NotNull Breakpoint<JavaMethodBreakpointProperties> createJavaBreakpoint(Project project, XBreakpoint<JavaMethodBreakpointProperties> breakpoint) {
//        return new RuntimeTestBreakpoint(project, breakpoint);
////        return super.createJavaBreakpoint(project, breakpoint);
//    }
    // idea version 223
    @Override
    public @NotNull Breakpoint<JavaMethodBreakpointProperties> createJavaBreakpoint(Project project, XBreakpoint breakpoint) {
        return new RuntimeTestBreakpoint(project, breakpoint);
    }

    @Override
    public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project) {
        return false;
    }
}