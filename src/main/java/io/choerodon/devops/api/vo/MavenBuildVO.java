package io.choerodon.devops.api.vo;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/7 14:04
 */
public class MavenBuildVO {
    private List<MavenbuildTemplateVO> mavenbuildTemplateVOList;

    public List<MavenbuildTemplateVO> getMavenbuildTemplateVOList() {
        return mavenbuildTemplateVOList;
    }

    public void setMavenbuildTemplateVOList(List<MavenbuildTemplateVO> mavenbuildTemplateVOList) {
        this.mavenbuildTemplateVOList = mavenbuildTemplateVOList;
    }
}