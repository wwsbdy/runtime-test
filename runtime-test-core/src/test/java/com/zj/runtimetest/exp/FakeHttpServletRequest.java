//package com.zj.runtimetest.utils;
//
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.servlet.*;
//import javax.servlet.http.*;
//import java.io.BufferedReader;
//import java.security.Principal;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//public class FakeHttpServletRequest implements HttpServletRequest {
//    private final Map<String, Object> attributes, headers;
//    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
//    private static final String[] DATE_FORMATS = {"EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, dd-MMM-yy HH:mm:ss zzz", "EEE MMM dd HH:mm:ss yyyy"};
//    public FakeHttpServletRequest(Map<String, Object> attributes, Map<String, Object> headers) { this.attributes = attributes;this.headers = headers;RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(this));}
//    @Override public String getAuthType() { return ""; }
//    @Override public Cookie[] getCookies() { return new Cookie[0]; }
//    @Override public long getDateHeader(String s) { return headers.get(s) instanceof Date ? ((Date) headers.get(s)).getTime() : headers.get(s) instanceof Number ? ((Number) headers.get(s)).longValue() : headers.get(s) instanceof String ? Arrays.stream(DATE_FORMATS).mapToLong(fmt -> { try { return new SimpleDateFormat(fmt, Locale.US) {{ setTimeZone(GMT); }}.parse((String) headers.get(s)).getTime(); } catch (ParseException e) { return -1L; } }).filter(v -> v != -1L).findFirst().orElseThrow(() -> new IllegalArgumentException("Cannot parse date value '" + headers.get(s) + "' for '" + s + "' header")) : headers.get(s) == null ? -1L : throw new IllegalArgumentException("Value for header '" + s + "' is not a Date, Number, or String: " + headers.get(s)); }
//    @Override public String getHeader(String s) { return Objects.toString(headers.get(s), null); }
//    @Override public Enumeration<String> getHeaders(String s) { return Collections.enumeration((Collection<String>) headers.get(s)); }
//    @Override public Enumeration<String> getHeaderNames() { return Collections.enumeration(headers.keySet()); }
//    @Override public int getIntHeader(String s) { return headers.get(s) instanceof Number ? ((Number) headers.get(s)).intValue() : -1; }
//    @Override public String getMethod() { return ""; }
//    @Override public String getPathInfo() { return ""; }
//    @Override public String getPathTranslated() { return ""; }
//    @Override public String getContextPath() { return ""; }
//    @Override public String getQueryString() { return ""; }
//    @Override public String getRemoteUser() { return ""; }
//    @Override public boolean isUserInRole(String s) { return false; }
//    @Override public Principal getUserPrincipal() { return null; }
//    @Override public String getRequestedSessionId() { return ""; }
//    @Override public String getRequestURI() { return ""; }
//    @Override public StringBuffer getRequestURL() { return null; }
//    @Override public String getServletPath() { return ""; }
//    @Override public HttpSession getSession(boolean b) { return null; }
//    @Override public HttpSession getSession() { return null; }
//    @Override public String changeSessionId() { return ""; }
//    @Override public boolean isRequestedSessionIdValid() { return false; }
//    @Override public boolean isRequestedSessionIdFromCookie() { return false; }
//    @Override public boolean isRequestedSessionIdFromURL() { return false; }
//    @Override public boolean isRequestedSessionIdFromUrl() { return false; }
//    @Override public boolean authenticate(HttpServletResponse httpServletResponse) { return false; }
//    @Override public void login(String s, String s1) {}
//    @Override public void logout() {}
//    @Override public Collection<Part> getParts() { return Collections.emptyList(); }
//    @Override public Part getPart(String s) { return null; }
//    @Override public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) { return null; }
//    @Override public Object getAttribute(String s) { return attributes.get(s); }
//    @Override public Enumeration<String> getAttributeNames() { return Collections.enumeration(attributes.keySet()); }
//    @Override public String getCharacterEncoding() { return ""; }
//    @Override public void setCharacterEncoding(String s) {}
//    @Override public int getContentLength() { return 0; }
//    @Override public long getContentLengthLong() { return 0; }
//    @Override public String getContentType() { return ""; }
//    @Override public ServletInputStream getInputStream() { return null; }
//    @Override public String getParameter(String s) { return ""; }
//    @Override public Enumeration<String> getParameterNames() { return null; }
//    @Override public String[] getParameterValues(String s) { return new String[0]; }
//    @Override public Map<String, String[]> getParameterMap() { return Collections.emptyMap(); }
//    @Override public String getProtocol() { return ""; }
//    @Override public String getScheme() { return ""; }
//    @Override public String getServerName() { return ""; }
//    @Override public int getServerPort() { return 0; }
//    @Override public BufferedReader getReader() { return null; }
//    @Override public String getRemoteAddr() { return ""; }
//    @Override public String getRemoteHost() { return ""; }
//    @Override public void setAttribute(String s, Object o) { attributes.put(s, o); }
//    @Override public void removeAttribute(String s) { attributes.remove(s); }
//    @Override public Locale getLocale() { return Locale.getDefault(); }
//    @Override public Enumeration<Locale> getLocales() { return Collections.enumeration(Collections.singleton(Locale.getDefault())); }
//    @Override public boolean isSecure() { return false; }
//    @Override public RequestDispatcher getRequestDispatcher(String s) { return null; }
//    @Override public String getRealPath(String s) { return ""; }
//    @Override public int getRemotePort() { return 0; }
//    @Override public String getLocalName() { return ""; }
//    @Override public String getLocalAddr() { return ""; }
//    @Override public int getLocalPort() { return 0; }
//    @Override public ServletContext getServletContext() { return null; }
//    @Override public AsyncContext startAsync() { return null; }
//    @Override public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) { return null; }
//    @Override public boolean isAsyncStarted() { return false; }
//    @Override public boolean isAsyncSupported() { return false; }
//    @Override public AsyncContext getAsyncContext() { return null; }
//    @Override public DispatcherType getDispatcherType() { return null; }
//}
