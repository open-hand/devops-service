package io.choerodon.devops.api.vo.host;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/27 20:43
 */
public class DockerComposeUpdatePayload {
    private List<DockerProcessUpdatePayload> updateProcessInfos;

    public List<DockerProcessUpdatePayload> getUpdateProcessInfos() {
        return updateProcessInfos;
    }

    public void setUpdateProcessInfos(List<DockerProcessUpdatePayload> updateProcessInfos) {
        this.updateProcessInfos = updateProcessInfos;
    }
}
