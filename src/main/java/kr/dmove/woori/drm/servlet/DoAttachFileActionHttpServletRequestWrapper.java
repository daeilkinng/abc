package kr.dmove.woori.drm.servlet;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DoAttachFileActionHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final Logger logger = Logger.getLogger(DoAttachFileActionHttpServletRequestWrapper.class);

    private byte[] newBuffer;
    private int bytesTotalWrite = 0;
    private int newBufferSize;

    private byte[] newBufferTemp;
    private int newBufferTempSize;

    /**
     * 생성자 : request body 재구성
     */
    public DoAttachFileActionHttpServletRequestWrapper(HttpServletRequest req, List<FileItem> fileItems, List<String> orgFileNameList, List<String> plainFilePathList) throws IOException {
        super(req);

        logger.debug("DoAttachFileActionHttpServletRequestWrapper constructor : ");

        try {

            this.newBufferSize = req.getContentLength();
            logger.debug("newBufferSize : " + this.newBufferSize);
            this.newBuffer = new byte[this.newBufferSize];

            String boundary = this.getBoundary(req);
            logger.debug("========== boundary : " + boundary);

            for (FileItem fileItem : fileItems) {
                System.arraycopy("--".getBytes(), 0, this.newBuffer, this.bytesTotalWrite, 2);
                this.bytesTotalWrite += 2;
                System.arraycopy(boundary.getBytes(), 0, this.newBuffer, this.bytesTotalWrite, boundary.length());
                this.bytesTotalWrite += boundary.length();
                Iterator<String> headerNames = fileItem.getHeaders().getHeaderNames();
                while (headerNames.hasNext()) {
                    String name = headerNames.next();
                    String value = fileItem.getHeaders().getHeader(name);
                    String header = name + ": " + value;
                    System.arraycopy("\r\n".getBytes(), 0, this.newBuffer, this.bytesTotalWrite, 2);
                    this.bytesTotalWrite += 2;
                    System.arraycopy(header.getBytes(), 0, this.newBuffer, this.bytesTotalWrite, header.length());
                    this.bytesTotalWrite += header.getBytes().length;
                }
                System.arraycopy("\r\n".getBytes(), 0, this.newBuffer, this.bytesTotalWrite, 2);
                this.bytesTotalWrite += 2;
                System.arraycopy("\r\n".getBytes(), 0, this.newBuffer, this.bytesTotalWrite, 2);
                this.bytesTotalWrite += 2;
                // 복호화된 파일인 경우 : plain file로 body작성
                if (orgFileNameList.contains(fileItem.getName())) {
                    logger.debug("========== body : 복호화 파일" + fileItem.getName());
                    BufferedInputStream reader = new BufferedInputStream(new FileInputStream(new File(plainFilePathList.get(orgFileNameList.indexOf(fileItem.getName())))));
                    byte[] tempBuffer = new byte[1024];
                    int bytesRead = 0;
                    while ((bytesRead = reader.read(tempBuffer)) != -1) {
                        System.arraycopy(tempBuffer, 0, this.newBuffer, this.bytesTotalWrite, bytesRead);
                        this.bytesTotalWrite += bytesRead;
                    }
                    reader.close();
                }
                // 복호화된 파일이 아닌 경우 : 기존 내용 copy
                else {
                    logger.debug("========== body : 기존 파일" + fileItem.getName());
                    System.arraycopy(fileItem.get(), 0, this.newBuffer, this.bytesTotalWrite, (int)(fileItem.getSize()));
                    this.bytesTotalWrite += (int)(fileItem.getSize());
                }
                System.arraycopy("\r\n".getBytes(), 0, this.newBuffer, this.bytesTotalWrite, 2);
                this.bytesTotalWrite += 2;
            }
            System.arraycopy("--".getBytes(), 0, this.newBuffer, this.bytesTotalWrite, 2);
            this.bytesTotalWrite += 2;
            System.arraycopy(boundary.getBytes(), 0, this.newBuffer, this.bytesTotalWrite, boundary.length());
            this.bytesTotalWrite += boundary.length();
            System.arraycopy("--".getBytes(), 0, this.newBuffer, this.bytesTotalWrite, 2);
            this.bytesTotalWrite += 2;
            System.arraycopy("\r\n".getBytes(), 0, this.newBuffer, this.bytesTotalWrite, 2);
            this.bytesTotalWrite += 2;

            logger.debug("[[[" + new String(this.newBuffer) + "]]]");
            this.newBufferSize = this.bytesTotalWrite;

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

    /**
     * ServletRequestWrapper Override
     */
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
        public void setReadListener(ReadListener listener) {
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

    private String getBoundary(HttpServletRequest req) throws IOException {
        String boundary;
        try {
            String contentType = req.getContentType();
            int index = contentType.indexOf("boundary=");
            boundary = contentType.substring(index + "boundary=".length());
        } catch (Exception e) {
            throw new IOException("Cannot get boundary from request : " + e.toString());
        }
        return boundary;
    }

}
