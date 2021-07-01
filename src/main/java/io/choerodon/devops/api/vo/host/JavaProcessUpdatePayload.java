package io.choerodon.devops.api.vo.host;

import java.util.List;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2021/6/27 19:27
 */
public class JavaProcessUpdatePayload {
    private List<JavaProcessInfoVO> updateProcessInfos;

    public List<JavaProcessInfoVO> getUpdateProcessInfos() {
        return updateProcessInfos;
    }

    public void setUpdateProcessInfos(List<JavaProcessInfoVO> updateProcessInfos) {
        this.updateProcessInfos = updateProcessInfos;
    }

}
