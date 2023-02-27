package kr.dmove.woori.drm.filters;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import kr.dmove.woori.drm.config.Config;
import kr.dmove.woori.drm.config.Constants;
import kr.dmove.woori.drm.service.SocamService;
import kr.dmove.woori.drm.service.ZipDownloadService;
import kr.dmove.woori.drm.servlet.DoAttachFileActionHttpServletRequestWrapper;
import kr.dmove.woori.drm.servlet.DownloadActionHttpServletResponseWrapper;
import kr.dmove.woori.drm.servlet.UploadActionHttpServletRequestWrapper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Named
public class FileFilterForDRM implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(FileFilterForDRM.class);
    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    private final SocamService socamService;

    private FilterConfig config;

    @Inject
    public FileFilterForDRM(SocamService socamService, PluginSettingsFactory pluginSettingsFactory) {
        logger.info("==== Constructor ================================================");
        this.socamService = socamService;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("==== init ================================================");
        this.config = filterConfig;
    }

    @Override
    public void destroy() {
        logger.info("==== destroy ================================================");
    }

    /**
     * 1. 파일 업로드 - 파일 업로드 시, DRM이 걸려 있으면(check, method 필요) - DRM 해제 (DRM 해제 method 필요)
     * 2. 파일 다운로드 - DRM이 걸려 있지 않으면 - DRM 적용 (DRM 적용 method 필요)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        logger.debug("==== doFilter ================================================" + ((HttpServletRequest)request));
        try {

            /*
             * init
             */
            PluginSettings pluginSettings = this.pluginSettingsFactory.createGlobalSettings();
            Config config = new Config();
            Object isUploadEnable = pluginSettings.get("dmove-confl-drm.isUploadEnable");
            config.setIsUploadEnable(isUploadEnable == null ? "" : isUploadEnable.toString());
            Object isDownloadEnable = pluginSettings.get("dmove-confl-drm.isDownloadEnable");
            config.setIsDownloadEnable(isDownloadEnable == null ? "" : isDownloadEnable.toString());
            Object extension = pluginSettings.get("dmove-confl-drm.extension");
            config.setExtension(extension == null ? "" : extension.toString());
            Object properties = pluginSettings.get("dmove-confl-drm.properties");
            config.setProperties(properties == null ? "" : properties.toString());
            Object system = pluginSettings.get("dmove-confl-drm.system");
            config.setSystem(system == null ? "" : system.toString());
            Object allowExtension = pluginSettings.get("dmove-confl-drm.allowExtension");
            config.setAllowExtension(allowExtension == null ? "" : allowExtension.toString().replace(" ", ""));
            Object prohibitExtension = pluginSettings.get("dmove-confl-drm.prohibitExtension");
            config.setProhibitExtension(prohibitExtension == null ? "" : prohibitExtension.toString().replace(" ", ""));
            Object keyfile = pluginSettings.get("dmove-confl-drm.keyfile");
            config.setKeyfile(keyfile == null ? "" : keyfile.toString());
            Object userId = pluginSettings.get("dmove-confl-drm.userId");
            config.setUserId(userId == null ? "" : userId.toString());
            Object auth = pluginSettings.get("dmove-confl-drm.auth");
            config.setAuth(auth == null ? "" : auth.toString());

            final String[] applyExtensionArray = config.getExtension().split(",");
            final String[] allowExtensionArray = config.getAllowExtension().split(",");
            final String[] prohibitExtensionArray = config.getProhibitExtension().split(",");
            logger.debug("==== doFilter prohibitExtension ================================================" + prohibitExtension.toString());

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // 서블릿 패스 : 사용하지 않음 getRequestURI() 사용
            String requestURI = httpRequest.getRequestURI();

            /*
             * doFilter
             */
            // TODO : 아래 url 확인
            // /pages/attachfile.action ???
            // /pages/doeditscaffold.action ???
            if (
                    ("POST".equals(httpRequest.getMethod()) && requestURI.contains("/pages/doattachfile.action"))
                            || ("POST".equals(httpRequest.getMethod()) && requestURI.contains("/plugins/drag-and-drop/upload.action"))
                            || ("POST".equals(httpRequest.getMethod()) && requestURI.contains("/pages/plugins/attachments/doattachfile.action"))
            ) {
                logger.debug("==== upload 1 ================================================");
                if (isUploadEnable != null && isUploadEnable.equals("true")) {
                    this.doFilterUpload(httpRequest, httpResponse, chain, applyExtensionArray, allowExtensionArray, prohibitExtensionArray, config);
                } else {
                    chain.doFilter(request, response);
                }
            } else if (
                    ("GET".equals(httpRequest.getMethod()) && requestURI.contains("/download/attachments"))
                            || ("GET".equals(httpRequest.getMethod()) && requestURI.contains("/pages/downloadallattachments.action"))
            ) {
                logger.debug("==== download 1 ================================================");

                if (isDownloadEnable != null && isDownloadEnable.equals("true")) {
                    this.doFilterDownload(httpRequest, httpResponse, chain, applyExtensionArray, config);
                } else {
                    chain.doFilter(request, response);
                }
            } else {
                logger.debug("==== else ================================================");
                chain.doFilter(request, response);
            }
        }
        // TODO : exception 발생 시 처리
        catch (Exception e) {
            e.printStackTrace();
            logger.debug("======== [stackTrace] : " + e.toString());
            for (int i = 0; i < 10; i++) {
                logger.debug("======== [stackTrace] : " + e.getStackTrace()[i].toString());
            }
            chain.doFilter(request, response);
        }
    }

    /**
     *
     */
    private void doFilterUpload(HttpServletRequest req, HttpServletResponse res, FilterChain chain, String[] applyExtensionArray, String[] allowExtensionArray, String[] prohibitExtensionArray, Config config) throws Exception {
        logger.info("==== doFilterUpload ================================================");

        String requestURI = req.getRequestURI();

        if ("POST".equals(req.getMethod()) && requestURI.contains("/pages/doattachfile.action")) {
            this.doFilterUpload_doattachfile(req, res, chain, applyExtensionArray, allowExtensionArray, prohibitExtensionArray, config);
        } else if ("POST".equals(req.getMethod()) && requestURI.contains("/plugins/drag-and-drop/upload.action")) {
            this.doFilterUpload_upload(req, res, chain, applyExtensionArray, allowExtensionArray, prohibitExtensionArray, config);
        } else if ("POST".equals(req.getMethod()) && requestURI.contains("/pages/plugins/attachments/doattachfile.action")) {
            this.doFilterUpload_attachments_doattachfile(req, res, chain, applyExtensionArray, allowExtensionArray, prohibitExtensionArray, config);
        } else {
            // unreachable
        }
    }

    /**
     *
     */
    private void doFilterDownload(HttpServletRequest req, HttpServletResponse res, FilterChain chain, String[] applyExtensionArray, Config config) throws Exception {
        logger.info("==== doFilterDownload ================================================");

        String requestURI = req.getRequestURI();

        if ("GET".equals(req.getMethod()) && requestURI.contains("/download/attachments")) {
            this.doFilterDownload_download(req, res, chain, applyExtensionArray, config);
        } else if ("GET".equals(req.getMethod()) && requestURI.contains("/pages/downloadallattachment.action")) {
            this.doFilterDownload_downloadallattachments(req, res, chain, applyExtensionArray, config);
        } else {
            // unreachable
        }
    }

    /**
     *
     */
    private void doFilterUpload_attachfile(HttpServletRequest req, HttpServletResponse res, FilterChain chain, String[] applyExtensionArray, String[] allowExtensionArray, String[] prohibitExtensionArray, Config config) throws Exception {
        logger.info("==== attachfile.action ================================================");

        this.doFilterUpload_attachments_doattachfile(req, res, chain, applyExtensionArray, allowExtensionArray, prohibitExtensionArray, config);
    }

    /**
     *
     */
    private void doFilterUpload_doattachfile(HttpServletRequest req, HttpServletResponse res, FilterChain chain, String[] applyExtensionArray, String[] allowExtensionArray, String[] prohibitExtensionArray, Config config) throws Exception {
        logger.info("==== doattachfile.action ================================================");

        this.doFilterUpload_attachments_doattachfile(req, res, chain, applyExtensionArray, allowExtensionArray, prohibitExtensionArray, config);
    }

    /**
     * drag and drop, 파일 업로드를 이용한 파일 업로드
     */
    private void doFilterUpload_upload(HttpServletRequest req, HttpServletResponse res, FilterChain chain, String[] applyExtensionArray, String[] allowExtensionArray, String[] prohibitExtensionArray, Config config) throws Exception {
        logger.info("======== [upload.action] " + req.getParameter("pageId") + "/" + req.getParameter("draftId") + "/" + req.getParameter("filename"));

        String filename_org = req.getParameter("filename");
        int idx = filename_org.lastIndexOf(".");
        String extension = filename_org.substring(idx + 1);

        // 허용된 확장자 아닌 경우 : return err
        if (!this.isAllowExtension(filename_org, allowExtensionArray)) {
            logger.debug("======== [doFilterUpload_upload] can not upload : " + extension.toLowerCase() + " file");
            res.setStatus(500);
            res.setContentType("application/json;charset=utf-8");
            res.getWriter().write("{\"actionErrors\":[\"can not upload : " + extension.toLowerCase() + " file\"]}");
            return;
        }

        // 금지된 확장자 : return err
        if (this.isProhibitExtension(filename_org, prohibitExtensionArray)) {
            logger.debug("======== [doFilterUpload_upload] can not upload : " + extension.toLowerCase() + " file");
            res.setStatus(500);
            res.setContentType("application/json;charset=utf-8");
            res.getWriter().write("{\"actionErrors\":[\"can not upload : " + extension.toLowerCase() + " file\"]}");
            return;
        }

        // 미대상 확장자 : bypass
        if (!this.isApplyExtension(filename_org, applyExtensionArray)) {
            logger.debug("======== [doFilterUpload_upload] " + extension.toLowerCase() + " file is not targer... for Decryption");
            chain.doFilter(req, res);
            return;
        }

        // get attach cipher file from req
        InputStream attachFileInputStream = req.getInputStream();
        logger.debug("======== [get attach cipher file from req] ");

        // get temp file path
        String tempFileLocation = this.getTempFileLocation(req, Constants.DMOVE_DRM_CRYPTO_TYPE_DEC);
        String hashCode = req.hashCode() + "";
        String tempFileName_cipher = this.getTempFileNameSourceCipher(filename_org, hashCode);
        String tempFileName_plain = this.getTempFileNameTargetPlain(filename_org, hashCode);
        logger.debug("======== [get temp file path] tempFileName_cipher : " + tempFileName_cipher);
        logger.debug("======== [get temp file path] tempFileName_plain : " + tempFileName_plain);

        // save attach to temp
        this.saveAttachFileToTemp(attachFileInputStream, this.getTempFileOutputStream(tempFileLocation, tempFileName_cipher));
        logger.debug("======== [save attach to temp] ");

        // plain = dec(temp)
        this.socamService.Dec(tempFileLocation + "/" + tempFileName_cipher, tempFileLocation + "/" + tempFileName_plain, config);
        logger.debug("======== [plain = dec(temp)] ");

        // request body를 가공할 HttpServletRequestWrapper를 생성한다.
        // 가공 (가급적 생성자에서 처리)
        UploadActionHttpServletRequestWrapper reqWrapper = new UploadActionHttpServletRequestWrapper(req, tempFileLocation + "/" + tempFileName_plain);

        // 가공된 HttpServletRequestWrapper를 chain에 전달
        logger.debug("======== [reqWrapper] " + reqWrapper.toString());
        chain.doFilter(reqWrapper, res);
    }

    /**
     * 댓글 작성 중 연결삽입 이용한 파일업로드, 생성 중 연결삽입 이용한 파일업로드
     */
    private void doFilterUpload_attachments_doattachfile(HttpServletRequest req, HttpServletResponse res, FilterChain chain, String[] applyExtensionArray, String[] allowExtensionArray, String[] prohibitExtensionArray, Config config) throws Exception {
        logger.info("======== [doattachfile.action] " + req.getParameter("pageId") + "/" + req.getParameter("draftId") + "/" + req.getParameter("filename"));

        // org file name list to be decrypted
        List<String> orgFileNameList = new ArrayList<String>();
        // plain(decrypted) file path list
        List<String> plainFilePathList = new ArrayList<String>();

        // TODO 여기서 encoding 필요한지 확인
        req.setCharacterEncoding("UTF-8");
        List<FileItem> fileItems = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
        logger.debug("======== [fileItems] " + fileItems);
        for (FileItem fileItem : fileItems) {

            // file이 아닌 경우 : bypass
            if (fileItem.isFormField() || fileItem.getSize() == 0) {
                continue;
            }

            String filename_org = fileItem.getName();

            // 허용된  확장자 아닌 경우 : return err
            if (!this.isAllowExtension(filename_org, allowExtensionArray)) {
                logger.debug("======== [doFilterUpload_upload] can not upload : " + filename_org + " file");
                res.setStatus(500);
                res.setContentType("application/json;charset=utf-8");
                res.getWriter().write("{\"actionErrors\":[\"can not upload : " + filename_org + " file\"]}");
                return;
            }

            // 미대상 확장자 : bypass
            if (!this.isApplyExtension(filename_org, applyExtensionArray)) {
                logger.debug("======== [doFilterUpload_upload] " + filename_org + " file is not targer... for Decryption");
                continue;
            }

            // get attach cipher file from req
            InputStream attachFileInputStream = fileItem.getInputStream();
            logger.debug("======== [get attach cipher file from req] ");

            // get temp file path
            String tempFileLocation = this.getTempFileLocation(req, Constants.DMOVE_DRM_CRYPTO_TYPE_DEC);
            String hashCode = req.hashCode() + "";
            String tempFileName_cipher = this.getTempFileNameSourceCipher(fileItem.getName(), hashCode);
            String tempFileName_plain = this.getTempFileNameTargetPlain(fileItem.getName(), hashCode);
            logger.debug("======== [get temp file path] tempFileName_cipher : " + tempFileName_cipher);
            logger.debug("======== [get temp file path] tempFileName_plain : " + tempFileName_plain);

            // save attach to temp
            this.saveAttachFileToTemp(attachFileInputStream, this.getTempFileOutputStream(tempFileLocation, tempFileName_cipher));
            logger.debug("======== [save attach to temp] ");
            logger.debug("======== [save tempFileName_cipher to temp] " + tempFileLocation + "/" + tempFileName_cipher);
            logger.debug("======== [save tempFileName_plain to temp] " + tempFileLocation + "/" + tempFileName_plain);

            // plain = dec(temp)
            this.socamService.Dec(tempFileLocation + "/" + tempFileName_cipher, tempFileLocation + "/" + tempFileName_plain, config);
            logger.debug("======== [plain = dec(temp)] ");

            orgFileNameList.add(fileItem.getName());
            plainFilePathList.add(tempFileLocation + "/" + tempFileName_plain);
        }

        DoAttachFileActionHttpServletRequestWrapper reqWrapper = new DoAttachFileActionHttpServletRequestWrapper(req, fileItems, orgFileNameList, plainFilePathList);

        chain.doFilter(reqWrapper, res);
    }

    /**
     *
     */
    private void doFilterDownload_download(HttpServletRequest req, HttpServletResponse res, FilterChain chain, String[] applyExtensionArray, Config config) throws Exception {
        logger.info("======== [download.action] " + req.getRequestURI());

        String filename_org = req.getRequestURI().split("/")[4];

        // 미대상 확장자 : bypass
        if (!this.isApplyExtension(filename_org, applyExtensionArray)) {
            int idx = filename_org.lastIndexOf(".");
            String extension = filename_org.substring(filename_org.lastIndexOf(".") + 1);
            logger.debug("======== [doFilterUpload_upload] " + extension.toLowerCase() + " file is not targer... for Decryption");
            return;
        }

        // servlet에 사용할 response 생성
        DownloadActionHttpServletResponseWrapper resWrapper = new DownloadActionHttpServletResponseWrapper(res, "");

        chain.doFilter(req, resWrapper);

        // servlet에 작성한 file을 임시 파일(plain)로 저장
        // get temp file path
        String tempFileLocation = this.getTempFileLocation(req, Constants.DMOVE_DRM_CRYPTO_TYPE_ENC);
        String hashCode = req.hashCode() + "";
        String tempFileName_plain = this.getTempFileNameSourcePlain(req);
        String tempFileName_cipher = this.getTempFileNameTargetCipher(req);
        logger.debug("======== [get temp file path] tempFileName_plain : " + tempFileName_plain);
        logger.debug("======== [get temp file path] tempFileName_cipher : " + tempFileName_cipher);

        // save response attach file to temp
        this.saveAttachFileToTemp(resWrapper.getDataStream(), this.getTempFileOutputStream(tempFileLocation, tempFileName_plain));
        logger.debug("======== [save response attach file to temp] ====[{}]====", resWrapper.getDataStream());

        // cipher = enc(temp)
        this.socamService.Enc(tempFileLocation + "/" + tempFileName_plain, tempFileLocation + "/" + tempFileName_cipher, config);
        logger.debug("======== [plain = dec(temp)] ");

        // 실제 response에는 cipher length setting
        FileInputStream fis = new FileInputStream(new File(tempFileLocation + "/" + tempFileName_cipher));
        BufferedInputStream reader = new BufferedInputStream(fis);
        res.setContentLengthLong(fis.getChannel().size());

        // 실제 response에 cipher data setting
        ServletOutputStream sos = res.getOutputStream();
        byte[] bytes = new byte[1024];
        int read;
        while ((read = reader.read(bytes)) != -1) {
            sos.write(bytes, 0, read);
        }
        sos.close();
        fis.close();
    }

    /**
     *
     */
    private void doFilterDownload_downloadallattachments(HttpServletRequest req, HttpServletResponse res, FilterChain chain, String[] applyExtensionArray, Config config) throws Exception {
        logger.debug("======== [downloadallattachments.action] ");

        // servlet에 사용할 response 생성
        DownloadActionHttpServletResponseWrapper resWrapper = new DownloadActionHttpServletResponseWrapper(res, "");

        chain.doFilter(req, resWrapper);

        // 경로 설정
        String location = resWrapper.getLocation();
        logger.debug("======== [location] " + location);
        int hashStartIndex = location.indexOf("export/download");
        String hashSubString = location.substring(hashStartIndex + "export/download".length(), location.length() - 1);
        int hashEndIndex = hashSubString.indexOf(".zip");
        String hashCode = hashSubString.substring(0, hashEndIndex);
        logger.debug("======== [hashCode] " + hashCode);
        String tempFileLocation = this.getTempFileLocationForDownloadAll(req, hashCode);
        logger.debug("======== [tempFileLocation] " + tempFileLocation);

        // 압축 처리할 ZipDownloadService 객체 생성
        ZipDownloadService zipService = new ZipDownloadService(req, tempFileLocation, hashCode);

        // resWrapper로부터 압축파일을 얻는다.
        zipService.getZipFileFromLocation(req, location);

        // temp에 압축을 푼다.
        List<String> fileNameList = zipService.doUnzip();

        // 필요한 file을 encrypt하고 원본은 삭제
        for (int i = 0; i < fileNameList.size(); i++) {

            String filePath = tempFileLocation + "/" + fileNameList.get(i);

            // 미대상 확장자 : bypass
            if (!this.isApplyExtension(filePath, applyExtensionArray)) {
                continue;
            }

            // cipher = enc(temp)
            this.socamService.Enc(filePath, filePath + "_enc", config);
            logger.debug("======== [plain = dec(temp)] ");

            // 원본 삭제
            File file = new File(filePath);
            boolean delete = file.delete();
            logger.debug("======== [delete] " + delete);

            // rename
            boolean rename = new File(filePath + "_enc").renameTo(new File(filePath));
            logger.debug("======== [rename] " + rename);
        }

        // temp 폴더의 파일들을 압축
        zipService.doZip(fileNameList);

        // 실제 response에 cipher length setting
        FileInputStream fis = new FileInputStream(new File(zipService.getZipFilePath()));
        BufferedInputStream reader = new BufferedInputStream(fis);
        res.setContentLengthLong(fis.getChannel().size());
        res.setContentType("application/zip; charset=UTF-8");
        res.setHeader("Content-Disposition", "attachment; filename=\"" + zipService.getZipFileName() + "\"");
        logger.debug("======== [zip file size] " + fis.getChannel().size());

        // 실제 response에 cipher data setting
        ServletOutputStream sos = res.getOutputStream();
        byte[] bytes = new byte[1024];
        int read;
        while ((read = reader.read(bytes)) != -1) {
            sos.write(bytes, 0, read);
        }
        sos.flush();
        sos.close();

        reader.close();
        fis.close();

        logger.debug("======== [close] ");
        zipService.close();

        logger.debug("======== [downloadallattachments.action] " + req.getParameter("pageId") + "/" + req.getParameter("draftId") + "/" + zipService.getZipFilePath());
    }

    /**
     *
     */
    private void saveAttachFileToTemp(InputStream is, OutputStream os) throws Exception {
        byte[] bytes = new byte[63];
        int read;

        while ((read = is.read(bytes)) != -1) {
            os.write(bytes, 0, read);
        }
    }

    /**
     *
     */
    private void saveAttachFileToTemp(byte[] b, OutputStream os) throws Exception {
        os.write(b);
    }

    /**
     *
     */
    private OutputStream getTempFileOutputStream(String tempFileLocation, String tempFileName) throws Exception {
        File temporaryLocation = new File(tempFileLocation);

        if (!temporaryLocation.exists()) {
            logger.debug("======== [DRMPLUGIN] Directory not not not exist");
            temporaryLocation.mkdirs();
        }

        File temporaryFile = new File(tempFileLocation + "/" + tempFileName);
        temporaryFile.deleteOnExit();

        return new FileOutputStream(temporaryFile);
    }

    /**
     *
     */
    private String getTempFileLocation(HttpServletRequest req, String cryptoType) throws Exception {
        if (Constants.DMOVE_DRM_CRYPTO_TYPE_ENC.equals(cryptoType)) {
            return System.getProperty("java.io.tmpdir") + "/enc/" + this.getPageId(req, cryptoType);
        } else {
            String spaceKey = req.getParameter("spaceKey");
            if (spaceKey == null) {
                return System.getProperty("java.io.tmpdir") + "/dec/" + "Temp" + "/" + this.getPageId(req, cryptoType);
            } else {
                return System.getProperty("java.io.tmpdir") + "/dec/" + spaceKey + "/" + this.getPageId(req, cryptoType);
            }
        }
    }

    /**
     *
     */
    private String getTempFileLocationForDownloadAll(HttpServletRequest req, String hashCode) throws Exception {
        String path = System.getProperty("java.io.tmpdir") + "/enc/" + req.getParameter("pageId") + "/" + hashCode;
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return path;
    }

    /**
     *
     */
    private String getTempFileNameSourceCipher(String filename_org, String hash) throws Exception {
        // encrypt
        String extension = filename_org.substring(filename_org.lastIndexOf(".") + 1);
        return "Je_" + hash + "_rry." + extension;
    }

    /**
     *
     */
    private String getTempFileNameTargetPlain(String filename_org, String hash) throws Exception {
        String extension = filename_org.substring(filename_org.lastIndexOf(".") + 1);
        return "Je_" + hash + "_rry" + "_DEC." + extension;
    }

    /**
     *
     */
    private String getTempFileNameSourcePlain(HttpServletRequest req) throws Exception {
        String uri = req.getRequestURI();
        String[] splitdata = uri.split("/");
        String filename_org = URLDecoder.decode(splitdata[4], "UTF-8");
        return filename_org;
    }

    /**
     *
     */
    private String getTempFileNameTargetCipher(HttpServletRequest req) throws Exception {
        String uri = req.getRequestURI();
        String[] splitdata = uri.split("/");
        String filename_org = URLDecoder.decode(splitdata[4], "UTF-8");
        int idx = filename_org.lastIndexOf(".");
        String filename_ex_extension = filename_org.substring(0, idx);
        String extension = filename_org.substring(filename_org.lastIndexOf(".") + 1);
        return filename_ex_extension + "_enc." + extension;
    }

    /**
     *
     */
    private String getPageId(HttpServletRequest req, String cryptoType) throws Exception {
        String strPageId;

        // encrypt
        if (Constants.DMOVE_DRM_CRYPTO_TYPE_ENC.equals(cryptoType)) {
            // sample
            // /download/attachments/11239455/user_account%20%281%29.csv?version=1&amp;modificationDate=1668557835255&amp;api=v2
            String uri = req.getRequestURI();

            String[] splitUri = uri.split("/");

            for (int i = 0; i < splitUri.length; i++) {
                logger.debug("======== [DRMPLUGIN] " + splitUri[i]);
            }

            strPageId = splitUri[3];
        }
        // decrypt
        else {
            strPageId = req.getParameter("pageId");

            // pageId == null, 0 일 경우에는 신규 작성 시 DRAFT ID로 구분
            if (strPageId == null || strPageId.equals("0")) {
                String draftId = req.getParameter("draftId");
                strPageId = draftId == null ? "123456" : draftId;
                logger.debug("======== [DRMPLUGIN] DRAFT ID : " + draftId);
            }
        }

        return strPageId;
    }

    /**
     * Upload 적용 확장자 확인
     */
    private boolean isAllowExtension(String filename, String[] allowExtensionArray) throws Exception {
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        if (Arrays.asList(allowExtensionArray).contains(extension.toLowerCase())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Upload 금지된 확장자 확인
     */
    private boolean isProhibitExtension(String filename, String[] prohibitExtensionArray) throws Exception {
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        if (Arrays.asList(prohibitExtensionArray).contains(extension.toLowerCase())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * DRM 적용대상 확장자 확인
     */
    private boolean isApplyExtension(String filename, String[] applyExtensionArray) throws Exception {
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        if (Arrays.asList(applyExtensionArray).contains(extension.toLowerCase())) {
            return true;
        } else {
            return false;
        }
    }

}
