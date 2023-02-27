package kr.dmove.woori.drm.filters;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileServletOutputStream extends ServletOutputStream {

    private final DataOutputStream outputStream;

    public FileServletOutputStream(OutputStream outputStream) {
        this.outputStream = new DataOutputStream(outputStream);
    }

    public void write(int b) throws IOException {
        this.outputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.outputStream.write(b);
    }

    public boolean isReady() {
        return true;
    }

    public void setWriteListener(WriteListener listener) {
    }

}
