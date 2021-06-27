package io.choerodon.devops.api.vo.host;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/27 20:43
 */
public class DockerProcessUpdatePayload {
    private List<DockerProcessInfoVO> deleteProcessInfos;
    private List<DockerProcessInfoVO> addProcessInfos;

    public List<DockerProcessInfoVO> getDeleteProcessInfos() {
        return deleteProcessInfos;
    }

    public void setDeleteProcessInfos(List<DockerProcessInfoVO> deleteProcessInfos) {
        this.deleteProcessInfos = deleteProcessInfos;
    }

    public List<DockerProcessInfoVO> getAddProcessInfos() {
        return addProcessInfos;
    }

    public void setAddProcessInfos(List<DockerProcessInfoVO> addProcessInfos) {
        this.addProcessInfos = addProcessInfos;
    }
}
