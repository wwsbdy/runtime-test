package com.zj.runtimetest.utils;

import com.zj.runtimetest.AgentContextHolder;
import com.zj.runtimetest.exp.PureECJCompiler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author : jie.zhou
 * @date : 2025/7/24
 */
public class HttpServletRequestUtil {

    public static Object setRequestAttributes(Object httpServletRequest, Map<String, Object> attributes, Map<String, Object> headers) {
        if (attributes == null || attributes.isEmpty()) {
            attributes = new LinkedHashMap<>();
        }
        if (headers == null || headers.isEmpty()) {
            headers = new LinkedHashMap<>();
        }
        if (Objects.isNull(httpServletRequest)) {
            return setRequestAttributes(attributes, headers);
        }
        Method addHeader = FakeHttpServletRequestBuilder.ADD_HEADER;
        Method setAttribute = FakeHttpServletRequestBuilder.SET_ATTRIBUTE;
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            if (Objects.nonNull(addHeader)) {
                try {
                    addHeader.invoke(httpServletRequest, entry.getKey(), entry.getValue());
                } catch (IllegalAccessException | InvocationTargetException ignored) {
                }
            }
            if (Objects.nonNull(setAttribute)) {
                try {
                    setAttribute.invoke(httpServletRequest, entry.getKey(), entry.getValue());
                } catch (IllegalAccessException | InvocationTargetException ignored) {
                }
            }
        }
        return httpServletRequest;
    }

    public static Object setRequestAttributes(Map<String, Object> attributes, Map<String, Object> headers) {
        if (attributes == null || attributes.isEmpty()) {
            attributes = new LinkedHashMap<>();
        }
        if (headers == null || headers.isEmpty()) {
            headers = new LinkedHashMap<>();
        }
        Constructor<?> constructor = FakeHttpServletRequestBuilder.CONSTRUCTOR;
        if (Objects.isNull(constructor)) {
            System.err.println("[Agent] FakeHttpServletRequest constructor build fail");
            return null;
        }
        try {
            return constructor.newInstance(attributes, headers);
        } catch (Exception e) {
            System.err.println("[Agent] FakeHttpServletRequest build fail");
        }
        return null;
    }


    public static boolean isHttpServletRequest(Class<?> aClass) {
        return "javax.servlet.http.HttpServletRequest".equals(aClass.getName());
    }

    public static boolean hasHttpServletRequest() {
        try {
            Class<?> aClass = ClassUtil.getClass("javax.servlet.http.HttpServletRequest", AgentContextHolder.DEFAULT_CLASS_LOADER);
            if (Objects.nonNull(aClass)) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            return false;
        }
        return false;
    }

    public static class FakeHttpServletRequestBuilder {
        static final Constructor<?> CONSTRUCTOR;
        static final Method ADD_HEADER;
        static final Method SET_ATTRIBUTE;

        static {
            Constructor<?> constructor = null;
            Method addHeader = null;
            Method setAttribute = null;
            try {
                String fakeHttpServletRequestStr = "package agent;\n" +
                        "import org.springframework.web.context.request.RequestContextHolder;import org.springframework.web.context.request.ServletRequestAttributes;import javax.servlet.*;import javax.servlet.http.*;import java.io.BufferedReader;import java.security.Principal;import java.text.ParseException;import java.text.SimpleDateFormat;import java.util.*;\n" +
                        "public class FakeHttpServletRequest implements HttpServletRequest {\n" +
                        "    private final Map<String, Object> attributes, headers;\n" +
                        "    private static final TimeZone GMT = TimeZone.getTimeZone(\"GMT\");\n" +
                        "    private static final String[] DATE_FORMATS = {\"EEE, dd MMM yyyy HH:mm:ss zzz\", \"EEE, dd-MMM-yy HH:mm:ss zzz\", \"EEE MMM dd HH:mm:ss yyyy\"};\n" +
                        "    public FakeHttpServletRequest(Map<String, Object> attributes, Map<String, Object> headers) { this.attributes = attributes;this.headers = headers;RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(this), true);}\n" +
                        "    @Override public String getAuthType() { return \"\"; }\n" +
                        "    @Override public Cookie[] getCookies() { return new Cookie[0]; }\n" +
                        "    @Override public long getDateHeader(String s) { Object value = headers.get(s); if (Objects.isNull(value)) { return -1; } if (value instanceof Date) { return ((Date) value).getTime(); } else if (value instanceof Number) { return ((Number) value).longValue(); } else if (value instanceof String) { return parseDateHeader(s, (String) value); } throw new IllegalArgumentException( \"Value for header '\" + s + \"' is not a Date, Number, or String: \" + value); }\n" +
                        "    private long parseDateHeader(String name, String value) { for (String dateFormat : DATE_FORMATS) { SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US); simpleDateFormat.setTimeZone(GMT); try { return simpleDateFormat.parse(value).getTime(); }  catch (ParseException ignored) { } } throw new IllegalArgumentException(\"Cannot parse date value '\" + value + \"' for '\" + name + \"' header\"); }\n" +
                        "    @Override public String getHeader(String s) { return Objects.toString(headers.get(s), null); }\n" +
                        "    @Override public void addHeader(String name, Object value) { headers.put(name, value); }\n" +
                        "    @Override public Enumeration<String> getHeaders(String s) { return Collections.enumeration((Collection<String>) headers.get(s)); }\n" +
                        "    @Override public Enumeration<String> getHeaderNames() { return Collections.enumeration(headers.keySet()); }\n" +
                        "    @Override public int getIntHeader(String s) { return headers.get(s) instanceof Number ? ((Number) headers.get(s)).intValue() : -1; }\n" +
                        "    @Override public String getMethod() { return \"\"; }\n" +
                        "    @Override public String getPathInfo() { return \"\"; }\n" +
                        "    @Override public String getPathTranslated() { return \"\"; }\n" +
                        "    @Override public String getContextPath() { return \"\"; }\n" +
                        "    @Override public String getQueryString() { return \"\"; }\n" +
                        "    @Override public String getRemoteUser() { return \"\"; }\n" +
                        "    @Override public boolean isUserInRole(String s) { return false; }\n" +
                        "    @Override public Principal getUserPrincipal() { return null; }\n" +
                        "    @Override public String getRequestedSessionId() { return \"\"; }\n" +
                        "    @Override public String getRequestURI() { return \"\"; }\n" +
                        "    @Override public StringBuffer getRequestURL() { return null; }\n" +
                        "    @Override public String getServletPath() { return \"\"; }\n" +
                        "    @Override public HttpSession getSession(boolean b) { return null; }\n" +
                        "    @Override public HttpSession getSession() { return null; }\n" +
                        "    @Override public String changeSessionId() { return \"\"; }\n" +
                        "    @Override public boolean isRequestedSessionIdValid() { return false; }\n" +
                        "    @Override public boolean isRequestedSessionIdFromCookie() { return false; }\n" +
                        "    @Override public boolean isRequestedSessionIdFromURL() { return false; }\n" +
                        "    @Override public boolean isRequestedSessionIdFromUrl() { return false; }\n" +
                        "    @Override public boolean authenticate(HttpServletResponse httpServletResponse) { return false; }\n" +
                        "    @Override public void login(String s, String s1) {}\n" +
                        "    @Override public void logout() {}\n" +
                        "    @Override public Collection<Part> getParts() { return Collections.emptyList(); }\n" +
                        "    @Override public Part getPart(String s) { return null; }\n" +
                        "    @Override public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) { return null; }\n" +
                        "    @Override public Object getAttribute(String s) { return attributes.get(s); }\n" +
                        "    @Override public Enumeration<String> getAttributeNames() { return Collections.enumeration(attributes.keySet()); }\n" +
                        "    @Override public String getCharacterEncoding() { return \"\"; }\n" +
                        "    @Override public void setCharacterEncoding(String s) {}\n" +
                        "    @Override public int getContentLength() { return 0; }\n" +
                        "    @Override public long getContentLengthLong() { return 0; }\n" +
                        "    @Override public String getContentType() { return \"\"; }\n" +
                        "    @Override public ServletInputStream getInputStream() { return null; }\n" +
                        "    @Override public String getParameter(String s) { return \"\"; }\n" +
                        "    @Override public Enumeration<String> getParameterNames() { return null; }\n" +
                        "    @Override public String[] getParameterValues(String s) { return new String[0]; }\n" +
                        "    @Override public Map<String, String[]> getParameterMap() { return Collections.emptyMap(); }\n" +
                        "    @Override public String getProtocol() { return \"\"; }\n" +
                        "    @Override public String getScheme() { return \"\"; }\n" +
                        "    @Override public String getServerName() { return \"\"; }\n" +
                        "    @Override public int getServerPort() { return 0; }\n" +
                        "    @Override public BufferedReader getReader() { return null; }\n" +
                        "    @Override public String getRemoteAddr() { return \"\"; }\n" +
                        "    @Override public String getRemoteHost() { return \"\"; }\n" +
                        "    @Override public void setAttribute(String s, Object o) { attributes.put(s, o); }\n" +
                        "    @Override public void removeAttribute(String s) { attributes.remove(s); }\n" +
                        "    @Override public Locale getLocale() { return Locale.getDefault(); }\n" +
                        "    @Override public Enumeration<Locale> getLocales() { return Collections.enumeration(Collections.singleton(Locale.getDefault())); }\n" +
                        "    @Override public boolean isSecure() { return false; }\n" +
                        "    @Override public RequestDispatcher getRequestDispatcher(String s) { return null; }\n" +
                        "    @Override public String getRealPath(String s) { return \"\"; }\n" +
                        "    @Override public int getRemotePort() { return 0; }\n" +
                        "    @Override public String getLocalName() { return \"\"; }\n" +
                        "    @Override public String getLocalAddr() { return \"\"; }\n" +
                        "    @Override public int getLocalPort() { return 0; }\n" +
                        "    @Override public ServletContext getServletContext() { return null; }\n" +
                        "    @Override public AsyncContext startAsync() { return null; }\n" +
                        "    @Override public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) { return null; }\n" +
                        "    @Override public boolean isAsyncStarted() { return false; }\n" +
                        "    @Override public boolean isAsyncSupported() { return false; }\n" +
                        "    @Override public AsyncContext getAsyncContext() { return null; }\n" +
                        "    @Override public DispatcherType getDispatcherType() { return null; }\n" +
                        "}";
                Class<?> clazz = PureECJCompiler.buildClass("agent.FakeHttpServletRequest", fakeHttpServletRequestStr);
                constructor = clazz.getConstructor(Map.class, Map.class);
                // addHeader(String name, Object value)
                addHeader = clazz.getMethod("addHeader", String.class, Object.class);
                // setAttribute(String s, Object o)
                setAttribute = clazz.getMethod("setAttribute", String.class, Object.class);
            } catch (NoSuchMethodException e) {
                System.err.println("[Agent] FakeHttpServletRequestClass init fail");
            }
            CONSTRUCTOR = constructor;
            ADD_HEADER = addHeader;
            SET_ATTRIBUTE = setAttribute;
        }
    }
}
