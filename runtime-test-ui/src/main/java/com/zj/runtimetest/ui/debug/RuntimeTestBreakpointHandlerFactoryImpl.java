package com.zj.runtimetest.ui.debug;

import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.JavaBreakpointHandler;
import com.intellij.debugger.engine.JavaBreakpointHandlerFactory;

/**
 * @author 19242
 */
public class RuntimeTestBreakpointHandlerFactoryImpl implements JavaBreakpointHandlerFactory {
    @Override
    public JavaBreakpointHandler createHandler(DebugProcessImpl debugProcess) {
        return new JavaBreakpointHandler(RuntimeTestBreakpointType.class, debugProcess);
    }
}
