package io.choerodon.devops.api.vo.deploy;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/8/30 20:19
 */
public class FileInfoVO {
    private String fileName;
    private String jarFileUrl;
    private String uploadUrl;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getJarFileUrl() {
        return jarFileUrl;
    }

    public void setJarFileUrl(String jarFileUrl) {
        this.jarFileUrl = jarFileUrl;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }
}
