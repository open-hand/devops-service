package io.choerodon.devops.api.vo.pipeline;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/16 11:03
 */
public class PipelineImageInfoVO {
    private String imageTag;
    private String downloadUrl;

    public PipelineImageInfoVO(String imageTag, String downloadUrl) {
        this.imageTag = imageTag;
        this.downloadUrl = downloadUrl;
    }

    public String getImageTag() {
        return imageTag;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
