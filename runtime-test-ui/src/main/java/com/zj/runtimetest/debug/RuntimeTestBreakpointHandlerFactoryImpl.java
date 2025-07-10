package com.zj.runtimetest.debug;

import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.JavaBreakpointHandler;
import com.intellij.debugger.engine.JavaBreakpointHandlerFactory;
import com.intellij.debugger.ui.breakpoints.LineBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import org.jetbrains.annotations.NotNull;

/**
 * @author 19242
 */
public class RuntimeTestBreakpointHandlerFactoryImpl implements JavaBreakpointHandlerFactory {
    @Override
    public JavaBreakpointHandler createHandler(DebugProcessImpl debugProcess) {
        return new JavaBreakpointHandler(RuntimeTestBreakpointType.class, debugProcess) {
            @Override
            protected @NotNull LineBreakpoint<RuntimeTestBreakpointProperties> createJavaBreakpoint(@NotNull XBreakpoint xBreakpoint) {
                return new RuntimeTestBreakpoint(debugProcess.getProject(), xBreakpoint);
            }

            @Override
            public void registerBreakpoint(@NotNull XBreakpoint breakpoint) {
                super.registerBreakpoint(breakpoint);
            }

            @Override
            public void unregisterBreakpoint(@NotNull XBreakpoint breakpoint, boolean temporary) {
                super.unregisterBreakpoint(breakpoint, temporary);
            }

        };
    }
}
