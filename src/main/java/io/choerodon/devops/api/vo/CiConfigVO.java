package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/7 14:04
 */
public class CiConfigVO {
    private List<CiConfigTemplateVO> config;

    public CiConfigVO() {
    }

    public CiConfigVO(List<CiConfigTemplateVO> config) {
        this.config = config;
    }

    public List<CiConfigTemplateVO> getConfig() {
        return config;
    }

    public void setConfig(List<CiConfigTemplateVO> config) {
        this.config = config;
    }
}