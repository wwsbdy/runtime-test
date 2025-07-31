package com.zj.runtimetest.utils;

import com.zj.runtimetest.AgentContextHolder;
import com.zj.runtimetest.exp.PureECJCompiler;
import com.zj.runtimetest.vo.IHttpServletRequest;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.Optional;

/**
 * @author : jie.zhou
 * @date : 2025/7/24
 */
public class HttpServletRequestUtil {

    private static Boolean HAS_HTTP_SERVLET_REQUEST;
    private static final ThreadLocal<IHttpServletRequest> HTTP_SERVLET_REQUEST = ThreadLocal.withInitial(() -> null);

    public static boolean isHttpServletRequest(Class<?> aClass) {
        return hasHttpServletRequest() && "javax.servlet.http.HttpServletRequest".equals(aClass.getName());
    }

    public static synchronized boolean hasHttpServletRequest() {
        if (Objects.nonNull(HAS_HTTP_SERVLET_REQUEST)) {
            return HAS_HTTP_SERVLET_REQUEST;
        }
        try {
            Class<?> httpServletRequestClass = ClassUtil.getClass("javax.servlet.http.HttpServletRequest", AgentContextHolder.DEFAULT_CLASS_LOADER);
            Class<?> requestContextHolderClass = ClassUtil.getClass("org.springframework.web.context.request.RequestContextHolder", AgentContextHolder.DEFAULT_CLASS_LOADER);
            Class<?> servletRequestAttributesClass = ClassUtil.getClass("org.springframework.web.context.request.ServletRequestAttributes", AgentContextHolder.DEFAULT_CLASS_LOADER);
            if (Objects.nonNull(httpServletRequestClass) && Objects.nonNull(requestContextHolderClass) && Objects.nonNull(servletRequestAttributesClass)) {
                return HAS_HTTP_SERVLET_REQUEST = true;
            }
        } catch (Exception e) {
//            LogUtil.alwaysLog(ThrowUtil.printStackTrace(e));
            return HAS_HTTP_SERVLET_REQUEST = false;
        }
        return HAS_HTTP_SERVLET_REQUEST = false;
    }

    public static void addHeader(String name, Object value) {
        if (!HttpServletRequestUtil.hasHttpServletRequest()) {
            LogUtil.err("[Agent more] addHeader: java.lang.ClassNotFoundException: javax.servlet.http.HttpServletRequest");
            return;
        }
        Optional.ofNullable(getHttpServletRequest()).ifPresent(request -> request.addHeader(name, value));
    }

    public static void setAttribute(String name, Object value) {
        if (!HttpServletRequestUtil.hasHttpServletRequest()) {
            LogUtil.err("[Agent more] setAttribute: java.lang.ClassNotFoundException: javax.servlet.http.HttpServletRequest");
            return;
        }
        Optional.ofNullable(getHttpServletRequest()).ifPresent(request -> request.setAttribute(name, value));
    }

    public static synchronized IHttpServletRequest getHttpServletRequest() {
        if (!HttpServletRequestUtil.hasHttpServletRequest()) {
            LogUtil.err("[Agent more] getHttpServletRequest: java.lang.ClassNotFoundException: javax.servlet.http.HttpServletRequest");
            return null;
        }
        IHttpServletRequest request = HTTP_SERVLET_REQUEST.get();
        if (Objects.nonNull(request)) {
            return request;
        }
        Constructor<IHttpServletRequest> constructor = FakeHttpServletRequestBuilder.CONSTRUCTOR;
        if (Objects.isNull(constructor)) {
            LogUtil.alwaysErr("[Agent] FakeHttpServletRequest constructor build fail");
            return null;
        }
        try {
            IHttpServletRequest fakeHttpServletRequest = constructor.newInstance();
            LogUtil.log("[Agent more] FakeHttpServletRequest build success");
            HTTP_SERVLET_REQUEST.set(fakeHttpServletRequest);
            return fakeHttpServletRequest;
        } catch (Exception e) {
            LogUtil.alwaysErr("[Agent] FakeHttpServletRequest build fail: " + ThrowUtil.printStackTrace(e));
        }
        return null;
    }


    /**
     * 清除当前线程的ThreadLocal状态（防止内存泄漏）
     */
    public static void clear() {
        HTTP_SERVLET_REQUEST.remove();
    }

    @SuppressWarnings("unchecked")
    public static class FakeHttpServletRequestBuilder {
        static final Constructor<IHttpServletRequest> CONSTRUCTOR;

        static {
            Constructor<IHttpServletRequest> constructor = null;
            try {
                String fakeHttpServletRequestStr = "package agent;\n" +
                        "import org.springframework.web.context.request.RequestContextHolder;import org.springframework.web.context.request.ServletRequestAttributes;import javax.servlet.*;import javax.servlet.http.*;import java.io.BufferedReader;import java.security.Principal;import java.text.ParseException;import java.text.SimpleDateFormat;import java.util.*;\n" +
                        "public class FakeHttpServletRequest implements HttpServletRequest, com.zj.runtimetest.vo.IHttpServletRequest {\n" +
                        "    private final Map<String, Object> attributes, headers;\n" +
                        "    private static final TimeZone GMT = TimeZone.getTimeZone(\"GMT\");\n" +
                        "    private static final String[] DATE_FORMATS = {\"EEE, dd MMM yyyy HH:mm:ss zzz\", \"EEE, dd-MMM-yy HH:mm:ss zzz\", \"EEE MMM dd HH:mm:ss yyyy\"};\n" +
                        "    public FakeHttpServletRequest() { this.attributes = new LinkedHashMap<>();this.headers = new LinkedHashMap<>();RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(this), true);}\n" +
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
                Class<IHttpServletRequest> clazz = (Class<IHttpServletRequest>) PureECJCompiler.buildClass("agent.FakeHttpServletRequest", fakeHttpServletRequestStr);
                constructor = clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                LogUtil.alwaysErr("[Agent] FakeHttpServletRequestClass init fail" + ThrowUtil.printStackTrace(e));
            }
            CONSTRUCTOR = constructor;
        }
    }
}
