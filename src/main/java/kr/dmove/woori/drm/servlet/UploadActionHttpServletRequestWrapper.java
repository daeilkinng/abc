package kr.dmove.woori.drm.servlet;

import org.apache.log4j.Logger;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UploadActionHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final Logger logger = Logger.getLogger(UploadActionHttpServletRequestWrapper.class);

    private byte[] newBuffer;
    private int newBufferSize;

    /**
     * 생성자
     */
    public UploadActionHttpServletRequestWrapper(HttpServletRequest req, String plainFilterPath) throws IOException {
        super(req);

        logger.debug("UploadActionHttpServletRequestWrapper constructor : ");

        try {
            FileInputStream fis = new FileInputStream(new File(plainFilterPath));
            this.newBufferSize = (int)fis.getChannel().size();
            logger.debug("file size : " + this.newBufferSize);
            this.newBuffer = new byte[this.newBufferSize];
            BufferedInputStream reader = new BufferedInputStream(fis);

            byte[] tempBuffer = new byte[1024];
            int bytesTotalRead = 0;
            int bytesRead = 0;
            while ((bytesRead = reader.read(tempBuffer)) != -1) {
                System.arraycopy(tempBuffer, 0, this.newBuffer, bytesTotalRead, bytesRead);
                bytesTotalRead += bytesRead;
            }
            reader.close();
        } catch (Exception e) {
            throw new IOException("Cannot parse underlying request : " + e.toString());
        }
    }

    /**
     * HttpServletRequestWrapper Override
     */
    private HttpServletRequest _getHttpServletRequest() {
        return (HttpServletRequest) super.getRequest();
    }

//    @Override
//    public String getAuthType() {
//        return this._getHttpServletRequest().getAuthType();
//    }

//    @Override
//    public Cookie[] getCookies() {
//        return this._getHttpServletRequest().getCookies();
//    }

//    @Override
//    public long getDateHeader(String name) {
//        return this._getHttpServletRequest().getDateHeader(name);
//    }

//    @Override
//    public String getHeader(String name) {
//        return this._getHttpServletRequest().getHeader(name);
//    }

//    @Override
//    public Enumeration<String> getHeaders(String name) {
//        return this._getHttpServletRequest().getHeaders(name);
//    }

//    @Override
//    public Enumeration<String> getHeaderNames() {
//        return this._getHttpServletRequest().getHeaderNames();
//    }

//    @Override
//    public int getIntHeader(String name) {
//        return this._getHttpServletRequest().getIntHeader(name);
//    }

//    @Override
//    public String getMethod() {
//        return this._getHttpServletRequest().getMethod();
//    }

//    @Override
//    public String getPathInfo() {
//        return this._getHttpServletRequest().getPathInfo();
//    }

//    @Override
//    public String getPathTranslated() {
//        return this._getHttpServletRequest().getPathTranslated();
//    }

//    @Override
//    public String getContextPath() {
//        return this._getHttpServletRequest().getContextPath();
//    }

    @Override
    public String getQueryString() {
        String queryString = this._getHttpServletRequest().getQueryString();
        logger.debug("getQueryString after-encoding: " + queryString);
        int index = queryString.indexOf("size=");
        if (index > 0) {
            String sizeRearString = queryString.substring(index);
            int indexAmp = sizeRearString.indexOf("&");
            if (indexAmp > 0) {
                logger.debug("getQueryString after-encoding: " + queryString.substring(0, index) + "size=" + Integer.toString(this.newBufferSize) + sizeRearString.substring(indexAmp));
                return queryString.substring(0, index) + "size=" + Integer.toString(this.newBufferSize) + sizeRearString.substring(indexAmp);
            } else {
                return queryString.substring(0, index) + "size=" + Integer.toString(this.newBufferSize);
            }
        } else {
            return this._getHttpServletRequest().getQueryString();
        }
    }

//    @Override
//    public String getRemoteUser() {
//        return this._getHttpServletRequest().getRemoteUser();
//    }

//    @Override
//    public boolean isUserInRole(String role) {
//        return this._getHttpServletRequest().isUserInRole(role);
//    }

//    @Override
//    public java.security.Principal getUserPrincipal() {
//        return this._getHttpServletRequest().getUserPrincipal();
//    }

//    @Override
//    public String getRequestedSessionId() {
//        return this._getHttpServletRequest().getRequestedSessionId();
//    }

//    @Override
//    public String getRequestURI() {
//        return this._getHttpServletRequest().getRequestURI();
//    }

//    @Override
//    public StringBuffer getRequestURL() {
//        return this._getHttpServletRequest().getRequestURL();
//    }

//    @Override
//    public String getServletPath() {
//        return this._getHttpServletRequest().getServletPath();
//    }

//    @Override
//    public HttpSession getSession(boolean create) {
//        return this._getHttpServletRequest().getSession(create);
//    }

//    @Override
//    public String changeSessionId() {
//        return this._getHttpServletRequest().changeSessionId();
//    }

//    @Override
//    public boolean isRequestedSessionIdValid() {
//        return this._getHttpServletRequest().isRequestedSessionIdValid();
//    }

//    @Override
//    public boolean isRequestedSessionIdFromCookie() {
//        return this._getHttpServletRequest().isRequestedSessionIdFromCookie();
//    }

//    @Override
//    public boolean isRequestedSessionIdFromURL() {
//        return this._getHttpServletRequest().isRequestedSessionIdFromURL();
//    }

//    @Override
//    public boolean isRequestedSessionIdFromUrl() {
//        return this._getHttpServletRequest().isRequestedSessionIdFromUrl();
//    }

//    @Override
//    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
//        return this._getHttpServletRequest().authenticate(response);
//    }

//    @Override
//    public void login(String username, String password) throws ServletException {
//        this._getHttpServletRequest().login(username, password);
//    }

//    @Override
//    public void logout() throws ServletException {
//        this._getHttpServletRequest().logout();
//    }

//    @Override
//    public Collection<Part> getParts() throws IOException, ServletException {
//        return this._getHttpServletRequest().getParts();
//    }

//    @Override
//    public Part getPart(String name) throws IOException, ServletException {
//        return this._getHttpServletRequest().getPart(name);
//    }

//    @Override
//    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
//        return this._getHttpServletRequest().upgrade(handlerClass);
//    }

    /**
     * ServletRequestWrapper Override
     */
//    private ServletRequest request;

//    @Override
//    public ServletRequest getRequest() {
//        return this.request;
//    }

//    @Override
//    public void sendRequest(ServletRequest request) {
//        if (request == null) {
//            throw new IllegalArgumentException("Request cannot be null");
//        }
//        this.request = request;
//    }

//    @Override
//    public Object getAttribute(String name) {
//        return this.request.getAttribute(name);
//    }

//    @Override
//    public String getCharacterEncoding() {
//        return this.request.getCharacterEncoding();
//    }

//    @Override
//    public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
//        this.request.setCharacterEncoding(enc);
//    }

    @Override
    public int getContentLength() {
//        return this.request.getContentLength();
        return this.newBufferSize;
    }

    @Override
    public long getContentLengthLong() {
//        return this.request.getContentLengthLong();
        return (long)this.newBufferSize;
    }

//    @Override
//    public String getContentType() {
//        return this.request.getContentType();
//    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.newBuffer);
        return new ServletInputStreamImpl(byteArrayInputStream);
    }

    class ServletInputStreamImpl extends ServletInputStream {
        private InputStream inputStream;

        public ServletInputStreamImpl(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }

        @Override
        public int read() throws IOException {
            int read = this.inputStream.read();
            return read;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int read = this.inputStream.read(b);
            return read;
        }
    };

    @Override
    public String getParameter(String name) {
        if ("size".equals(name)) {
            return Integer.toString(this.newBufferSize);
        } else {
            return this._getHttpServletRequest().getParameter(name);
        }
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = new HashMap<>();
        map.putAll(this._getHttpServletRequest().getParameterMap());
        for (String key : map.keySet()) {
            if ("size".equals(key)) {
                String[] newSize = new String[1];
                newSize[0] = Integer.toString(this.newBufferSize);
                map.replace(key, newSize);
            }
        }
        return map;
    }

//    @Override
//    public Enumeration<String> getParameterNames() {
//        return this.request.getParameterNames();
//    }

//    @Override
//    public String[] getParameterValues(String name) {
//        return this.request.getParameterValues(name);
//    }

//    @Override
//    public String getProtocol() {
//        return this.request.getProtocol();
//    }

//    @Override
//    public String getScheme() {
//        return this.request.getScheme();
//    }

//    @Override
//    public String getServerName() {
//        return this.request.getServerName();
//    }

//    @Override
//    public int getServerPort() {
//        return this.request.getServerPort();
//    }

//    @Override
//    public BufferedReader getReader() throws IOException {
//        return this.request.getReader();
//    }

//    @Override
//    public String getRemoteAddr() {
//        return this.request.getRemoteAddr();
//    }

//    @Override
//    public String getRemoteHost() {
//        return this.request.getRemoteHost();
//    }

//    @Override
//    public void setAttribute(String name, Object o) {
//        this.request.setAttribute(name, o);
//    }

//    @Override
//    public void removeAttribute(String name) {
//        this.request.removeAttribute(name);
//    }

//    @Override
//    public Locale getLocale() {
//        return this.request.getLocale();
//    }

//    @Override
//    public Enumeration<Locale> getLocales() {
//        return this.request.getLocales();
//    }

//    @Override
//    public boolean isSecure() {
//        return this.request.isSecure();
//    }

//    @Override
//    public RequestDispatcher getRequestDispatcher(String path) {
//        return this.request.getRequestDispatcher(path);
//    }

//    @Override
//    public String getRealPath(String path) {
//        return this.request.getRealPath(path);
//    }

//    @Override
//    public int getRemotePort() {
//        return this.request.getRemotePort();
//    }

//    @Override
//    public String getLocalName() {
//        return this.request.getLocalName();
//    }

//    @Override
//    public String getLocalAddr() {
//        return this.request.getLocalAddr();
//    }

//    @Override
//    public int getLocalPort() {
//        return this.request.getLocalPort();
//    }

//    @Override
//    public ServletContext getServletContext() {
//        return request.getServletContext();
//    }

//    @Override
//    public AsyncContext startAsync() throws IllegalStateException {
//        return request.startAsync();
//    }

//    @Override
//    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
//        return request.startAsync(servletRequest, servletResponse);
//    }

//    @Override
//    public boolean isAsyncStarted() {
//        return request.isAsyncStarted();
//    }

//    @Override
//    public boolean isAsyncSupported() {
//        return request.isAsyncSupported();
//    }

//    @Override
//    public AsyncContext getAsyncContext() {
//        return request.getAsyncContext();
//    }

//    @Override
//    public boolean isWrapperFor(ServletRequest wrapped) {
//        if (request == wrapped) {
//            return true;
//        } else if (request instanceof ServletRequestWrapper) {
//            return ((ServletRequestWrapper) request).isWrapperFor(wrapped);
//        } else {
//            return false;
//        }
//    }

//    @Override
//    public boolean isWrapperFor(Class<?> wrappedType) {
//        if (!ServletRequest.class.isAssignableFrom(wrappedType)) {
//            throw new IllegalArgumentException("Given class " + wrappedType.getName() + " not a subinterface of " + ServletRequest.class.getName());
//        }
//        if (wrappedType.isAssignableFrom(request.getClass())) {
//            return true;
//        } else if (request instanceof ServletRequestWrapper) {
//            return ((ServletRequestWrapper) request).isWrapperFor(wrappedType);
//        } else {
//            return false;
//        }
//    }

//    @Override
//    public DispatcherType getDispatcherType() {
//        return request.getDispatcherType();
//    }

}
