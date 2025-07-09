package com.zj.runtimetest.debug;

import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.JavaBreakpointHandler;
import com.intellij.debugger.engine.JavaBreakpointHandlerFactory;
import com.intellij.debugger.engine.events.SuspendContextCommandImpl;
import com.intellij.debugger.ui.breakpoints.LineBreakpoint;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.sun.jdi.event.LocatableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author 19242
 */
public class JavaBreakpointHandlerFactoryImpl implements JavaBreakpointHandlerFactory {
    @Override
    public JavaBreakpointHandler createHandler(DebugProcessImpl debugProcess) {
        return new JavaBreakpointHandler(MyDebugBreakpointType.class, debugProcess) {
            @Override
            protected @NotNull LineBreakpoint<MyBreakpointProperties> createJavaBreakpoint(@NotNull XBreakpoint xBreakpoint) {
                return new LineBreakpoint<>(debugProcess.getProject(), xBreakpoint) {
                    @Override
                    protected @NotNull MyBreakpointProperties getProperties() {
                        return super.getProperties();
                    }

                    @Override
                    public boolean processLocatableEvent(@NotNull SuspendContextCommandImpl action, LocatableEvent event) throws EventProcessingException {
                        boolean b = super.processLocatableEvent(action, event);
                        XDebuggerManager.getInstance(getProject()).getBreakpointManager().removeBreakpoint(this.getXBreakpoint());
                        return b;
                    }
                };
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
