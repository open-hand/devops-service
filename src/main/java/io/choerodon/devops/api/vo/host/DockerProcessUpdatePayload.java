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
    private List<DockerProcessInfoVO> updateProcessInfos;

    public List<DockerProcessInfoVO> getDeleteProcessInfos() {
        return deleteProcessInfos;
    }

    public void setDeleteProcessInfos(List<DockerProcessInfoVO> deleteProcessInfos) {
        this.deleteProcessInfos = deleteProcessInfos;
    }

    public List<DockerProcessInfoVO> getUpdateProcessInfos() {
        return updateProcessInfos;
    }

    public void setUpdateProcessInfos(List<DockerProcessInfoVO> updateProcessInfos) {
        this.updateProcessInfos = updateProcessInfos;
    }
}
