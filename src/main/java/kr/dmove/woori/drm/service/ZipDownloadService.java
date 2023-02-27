package kr.dmove.woori.drm.service;

import org.apache.log4j.Logger;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipDownloadService {

    private final Logger logger = Logger.getLogger(ZipDownloadService.class);

    private HttpServletRequest req;
    private String location;
    private String fileName;

    public ZipDownloadService(HttpServletRequest req, String location, String hashCode) throws Exception {
        this.req = req;
        this.location = location; // System.getProperty("java.io.tmpdir") + "/enc/" + pageId + "/" + hashCode;
        this.fileName = "download" + hashCode + ".zip";
    }

    public void getZipFileFromLocation(HttpServletRequest req, String location) throws Exception {
        String zipUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + location;
        logger.debug("========== [zipUrl] ==========" + zipUrl);
        URL url = new URL(zipUrl);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        String cookie = "";
        Cookie[] cookies = req.getCookies();
        for (int i = 0; i < cookies.length; i++) {
            if (i > 0) {
                cookie += "; ";
            }
            cookie += (cookies[i].getName() + "=" + cookies[i].getValue());
        }
        logger.debug("========== [cookie] ==========" + cookie);
        conn.setRequestProperty("Cookie", cookie);

        logger.debug("========== [conn.getResponseCode()] : " + conn.getResponseCode());

        FileOutputStream fos = new FileOutputStream(new File(this.location + "/" + this.fileName));
        FileCopyUtils.copy(conn.getInputStream(), fos);
        fos.close();
    }

    public List<String> doUnzip() throws Exception {
        List<String> fileList = new ArrayList<String>();

        File zipFile = new File(this.location + "/" + this.fileName);
        FileInputStream fileInputStream = new FileInputStream(zipFile);
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
        ZipEntry zipEntry = null;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            String entryFileName = zipEntry.getName();
            logger.debug("========== [entryFileName] ==========" + entryFileName);
            if (zipEntry.isDirectory()) {
            } else {
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(this.location + "/" + entryFileName));
                byte[] bytes = new byte[1024];
                int read = 0;
                while ((read = zipInputStream.read(bytes)) != -1) {
                    bufferedOutputStream.write(bytes, 0, read);
                }
                bufferedOutputStream.flush();
                bufferedOutputStream.close();

                fileList.add(entryFileName);
            }
            logger.debug("========== [entryFileName] ==========" + entryFileName);
            zipInputStream.closeEntry();
        }
        zipInputStream.close();
        fileInputStream.close();

        // 원본 파일을 삭제한다.
        boolean delete = zipFile.delete();
        logger.debug("========== [delete] " + delete);

        return fileList;
    }

    public void doZip(List<String> fileNameList) throws Exception {
        logger.debug("========== [fileName] " + this.location + "/" + this.fileName);
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(this.location + "/" + this.fileName));
        ZipEntry zipEntry = null;
        for (int i = 0; i < fileNameList.size(); i++) {
            logger.debug("========== [i] " + i);
            String filePath = this.location + "/" + fileNameList.get(i);
            logger.debug("========== [filePath] " + filePath);

            zipOutputStream.putNextEntry(new ZipEntry(fileNameList.get(i)));
            logger.debug("========== [putNextEntry] ");

            FileInputStream fileInputStream = new FileInputStream(filePath);
            byte[] bytes = new byte[1024];
            int read = 0;
            while ((read = fileInputStream.read(bytes)) != -1) {
                zipOutputStream.write(bytes, 0, read);
                if (read < 1024) {
                    logger.debug("========== [last read] " + read);
                }
            }
            fileInputStream.close();

            zipOutputStream.flush();
            zipOutputStream.closeEntry();
        }
        logger.debug("========== [close] ");
        zipOutputStream.close();
    }

    public String getZipFileName() throws Exception {
        return this.fileName;
    }

    public String getZipFilePath() throws Exception {
        return this.location + "/" + this.fileName;
    }

    public void close() throws Exception {
    }

}
