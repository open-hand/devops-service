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
    private List<CiConfigTemplateVO> ciConfigTemplateVOList;

    public List<CiConfigTemplateVO> getCiConfigTemplateVOList() {
        return ciConfigTemplateVOList;
    }

    public void setCiConfigTemplateVOList(List<CiConfigTemplateVO> ciConfigTemplateVOList) {
        this.ciConfigTemplateVOList = ciConfigTemplateVOList;
    }
}