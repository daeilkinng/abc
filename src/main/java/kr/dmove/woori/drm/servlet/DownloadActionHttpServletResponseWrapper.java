package kr.dmove.woori.drm.servlet;

import kr.dmove.woori.drm.filters.FileServletOutputStream;
import org.apache.log4j.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DownloadActionHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private static final Logger logger = Logger.getLogger(DownloadActionHttpServletResponseWrapper.class);

    private ByteArrayOutputStream byteArrayOutputStream;
    private FileServletOutputStream filterServletOutputStream;

    private String location;

    /**
     * 생성자
     */
    public DownloadActionHttpServletResponseWrapper(HttpServletResponse res, String cipherFilePath) throws Exception {
        super(res);

        logger.debug("DownloadActionHttpServletResponseWrapper constructor : ");

        this.byteArrayOutputStream = new ByteArrayOutputStream();
    }

    /**
     * servlet이 저장한 file data(plain)을 확인할 때 사용
     */
    public byte[] getDataStream() {
        return this.byteArrayOutputStream.toByteArray();
    }

    /**
     * servlet이 저장한 send redirect location을 확인할 때 사용
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * HttpServletResponseWrapper Override
     */
//    public HttpServletResponseWrapper(HttpServletResponse response) {
//        super(response);
//    }
    private HttpServletResponse _getHttpServletResponse() {
        return (HttpServletResponse) super.getResponse();
    }

//    @Override
//    public void addCookie(Cookie cookie) {
//        logger.debug("========== [addCookie] " + cookie);
//        this._getHttpServletResponse().addCookie(cookie);
//    }

//    @Override
//    public boolean containsHeader(String name) {
//        logger.debug("========== [containsHeader] " + name);
//        return this._getHttpServletResponse().containsHeader(name);
//    }

//    @Override
//    public String encodeURL(String url) {
//        logger.debug("========== [encodeURL] " + url);
//        return this._getHttpServletResponse().encodeURL(url);
//    }

//    @Override
//    public String encodeRedirectURL(String url) {
//        logger.debug("========== [encodeRedirectURL] " + url);
//        return this._getHttpServletResponse().encodeRedirectURL(url);
//    }

//    @Override
//    public String encodeUrl(String url) {
//        logger.debug("========== [encodeUrl] " + url);
//        return this._getHttpServletResponse().encodeUrl(url);
//    }

//    @Override
//    public String encodeRedirectUrl(String url) {
//        logger.debug("========== [encodeRedirectUrl] " + url);
//        return this._getHttpServletResponse().encodeRedirectUrl(url);
//    }

//    @Override
//    public void sendError(int sc, String msg) throws java.io.IOException {
//        logger.debug("========== [sendError] " + sc + ", " + msg);
//        this._getHttpServletResponse().sendError(sc, msg);
//    }

//    @Override
//    public void sendError(int sc) throws java.io.IOException {
//        logger.debug("========== [sendError] " + sc);
//        this._getHttpServletResponse().sendError(sc);
//    }

    @Override
    public void sendRedirect(String location) throws IOException {
        logger.debug("========== [sendRedirect] " + location);
        if (location.contains("/download/export/download")) {
            this.location = location;
        } else {
            this._getHttpServletResponse().sendRedirect(location);
        }
    }

//    @Override
//    public void setDateHeader(String name, long date) {
//        logger.debug("========== [setDateHeader] " + name + ", " + date);
//        this._getHttpServletResponse().setDateHeader(name, date);
//    }

//    @Override
//    public void addDateHeader(String name, long date) {
//        logger.debug("========== [addDateHeader] " + name + ", " + date);
//        this._getHttpServletResponse().addDateHeader(name, date);
//    }

//    @Override
//    public void setHeader(String name, String value) {
//        logger.debug("========== [setHeader] " + name);
//        this._getHttpServletResponse().setHeader(name, value);
//    }

//    @Override
//    public void addHeader(String name, String value) {
//        logger.debug("========== [addHeader] " + name);
//        this._getHttpServletResponse().addHeader(name, value);
//    }

//    @Override
//    public void setIntHeader(String name, int value) {
//        logger.debug("========== [setIntHeader] " + name);
//        this._getHttpServletResponse().setIntHeader(name, value);
//    }

//    @Override
//    public void addIntHeader(String name, int value) {
//        logger.debug("========== [addIntHeader] " + name);
//        this._getHttpServletResponse().addIntHeader(name, value);
//    }

//    @Override
//    public void setStatus(int sc) {
//        logger.debug("========== [setStatus] " + sc);
//        this._getHttpServletResponse().setStatus(sc);
//    }

//    @Override
//    public void setStatus(int sc, String sm) {
//        logger.debug("========== [setStatus] " + sc);
//        this._getHttpServletResponse().setStatus(sc, sm);
//    }

//    @Override
//    public int getStatus() {
//        logger.debug("========== [getStatus] " + _getHttpServletResponse().getStatus());
//        return this._getHttpServletResponse().getStatus();
//    }

//    @Override
//    public String getHeader(String name) {
//        logger.debug("========== [getHeader] " + name);
//        return this._getHttpServletResponse().getHeader(name);
//    }

//    @Override
//    public Collection<String> getHeaders(String name) {
//        logger.debug("========== [getHeaders] " + name);
//        return this._getHttpServletResponse().getHeaders(name);
//    }

//    @Override
//    public Collection<String> getHeaderNames() {
//        logger.debug("========== [getHeaderNames] ");
//        return this._getHttpServletResponse().getHeaderNames();
//    }

//    private ServletResponse response;

    /**
     * ServletResponseWrapper Override
     */
//    @Override
//    public ServletResponse getResponse() {
//        logger.debug("========== [getResponse] ");
//        return this.response;
//    }

//    @Override
//    public void setResponse(ServletResponse response) {
//        logger.debug("========== [setResponse] ");
//        if (response == null) {
//            throw new IllegalArgumentException("Response cannot be null");
//        }
//        this.response = response;
//    }

//    @Override
//    public void setCharacterEncoding(String charset) {
//        logger.debug("========== [setCharacterEncoding] ");
//        this.response.setCharacterEncoding(charset);
//    }

//    @Override
//    public String getCharacterEncoding() {
//        logger.debug("========== [getCharacterEncoding] ");
//        return this.response.getCharacterEncoding();
//    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        logger.debug("========== [getOutputStream] ");
//        return this.response.getOutputStream();
        if (this.filterServletOutputStream == null) {
            this.filterServletOutputStream = new FileServletOutputStream(this.byteArrayOutputStream);
        }
        return this.filterServletOutputStream;
    }

//    @Override
//    public PrintWriter getWriter() throws IOException {
//        logger.debug("========== [getWriter] ");
//        return this.response.getWriter();
//    }

//    @Override
//    public void setContentLength(int len) {
//        logger.debug("========== [setContentLength] " + len);
//        _getHttpServletResponse().setContentLength(len);
//    }

//    @Override
//    public void setContentType(String type) {
//        logger.debug("========== [setContentType] " + type);
//        this.response.setContentType(type);
//    }

//    @Override
//    public String getContentType() {
//        logger.debug("========== [getContentType] ");
//        return this.response.getContentType();
//    }

//    @Override
//    public void setBufferSize(int size) {
//        logger.debug("========== [setBufferSize] " + size);
//        this.response.setBufferSize(size);
//    }

//    @Override
//    public int getBufferSize() {
//        logger.debug("========== [getBufferSize] ");
//        return this.response.getBufferSize();
//    }

//    @Override
//    public boolean isCommitted() {
//        logger.debug("========== [isCommitted] ");
//        return this.response.isCommitted();
//    }

//    @Override
//    public void reset() {
//        logger.debug("========== [reset] ");
//        this.response.reset();
//    }

//    @Override
//    public void resetBuffer() {
//        logger.debug("========== [resetBuffer] ");
//        this.response.resetBuffer();
//    }

//    @Override
//    public void setLocale(Locale loc) {
//        logger.debug("========== [setLocale] ");
//        this.response.setLocale(loc);
//    }

//    @Override
//    public boolean isWrapperFor(ServletResponse wrapped) {
//        logger.debug("========== [isWrapperFor] ");
//        if (response == wrapped) {
//            return true;
//        } else if (response instanceof ServletResponseWrapper) {
//            return ((ServletResponseWrapper) response).isWrapperFor(wrapped);
//        } else {
//            return false;
//        }
//    }

//    @Override
//    public boolean isWrapperFor(Class<?> wrappedType) {
//        logger.debug("========== [isWrapperFor] ");
//        if (!ServletResponse.class.isAssignableFrom(wrappedType)) {
//            throw new IllegalArgumentException("Given class " + wrappedType.getName() + " not a subinterface of " + ServletResponse.class.getName());
//        }
//        if (wrappedType.isAssignableFrom(response.getClass())) {
//            return true;
//        } else if (response instanceof ServletResponseWrapper) {
//            return ((ServletResponseWrapper) response).isWrapperFor(wrappedType);
//        } else {
//            return false;
//        }
//    }

}
