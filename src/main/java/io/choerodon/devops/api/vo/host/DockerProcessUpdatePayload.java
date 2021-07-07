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
    private List<DockerProcessInfoVO> updateProcessInfos;

    public List<DockerProcessInfoVO> getUpdateProcessInfos() {
        return updateProcessInfos;
    }

    public void setUpdateProcessInfos(List<DockerProcessInfoVO> updateProcessInfos) {
        this.updateProcessInfos = updateProcessInfos;
    }
}
