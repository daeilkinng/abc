package kr.dmove.woori.drm.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class Config {

    @XmlElement private String isUploadEnable;
    @XmlElement private String isDownloadEnable;

    @XmlElement private String extension;
    @XmlElement private String properties;
    @XmlElement private String system;

    @XmlElement private String allowExtension;
    @XmlElement private String prohibitExtension;

    @XmlElement private String keyfile;
    @XmlElement private String userId;
    @XmlElement private String auth;

    public Config() {}

    private Config(String isUploadEnable, String isDownloadEnable, String extension, String properties, String system, String allowExtension, String prohibitExtension, String keyfile, String userId, String auth) {
        this.isUploadEnable = isUploadEnable;
        this.isDownloadEnable = isDownloadEnable;

        this.extension = extension;
        this.properties = properties;
        this.system = system;

        this.allowExtension = allowExtension;
        this.prohibitExtension = prohibitExtension;

        this.keyfile = keyfile;
        this.userId = userId;
        this.auth = auth;
    }

    public String getIsUploadEnable() {
        return isUploadEnable;
    }

    public void setIsUploadEnable(String isUploadEnable) {
        this.isUploadEnable = isUploadEnable;
    }

    public String getIsDownloadEnable() {
        return isDownloadEnable;
    }

    public void setIsDownloadEnable(String isDownloadEnable) {
        this.isDownloadEnable = isDownloadEnable;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getAllowExtension() {
        return allowExtension;
    }

    public void setAllowExtension(String allowExtension) {
        this.allowExtension = allowExtension;
    }

    public String getProhibitExtension() {
        return prohibitExtension;
    }

    public void setProhibitExtension(String prohibitExtension) {
        this.prohibitExtension = prohibitExtension;
    }

    public String getKeyfile() {
        return keyfile;
    }

    public void setKeyfile(String keyfile) {
        this.keyfile = keyfile;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    @Override
    public String toString() {
        return "Config [isUploadEnable=" + isUploadEnable + ", isDownloadEnable=" + isDownloadEnable + ", extension="
                + extension + ", properties=" + properties + ", system=" + system + ", allowExtension=" + allowExtension
                + ", prohibitExtension=" + prohibitExtension + ", keyfile=" + keyfile + ", userId=" + userId + ", auth=" + auth + "]";
    }

}
