package io.choerodon.devops.api.vo.host;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/27 19:27
 */
public class InstanceProcessUpdatePayload {
    private List<InstanceProcessInfoVO> updateProcessInfos;

    public List<InstanceProcessInfoVO> getUpdateProcessInfos() {
        return updateProcessInfos;
    }

    public void setUpdateProcessInfos(List<InstanceProcessInfoVO> updateProcessInfos) {
        this.updateProcessInfos = updateProcessInfos;
    }

}
