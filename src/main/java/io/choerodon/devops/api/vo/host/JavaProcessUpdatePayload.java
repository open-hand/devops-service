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
    private List<JavaProcessInfoVO> deleteProcessInfos;

    public List<JavaProcessInfoVO> getDeleteProcessInfos() {
        return deleteProcessInfos;
    }

    public void setDeleteProcessInfos(List<JavaProcessInfoVO> deleteProcessInfos) {
        this.deleteProcessInfos = deleteProcessInfos;
    }

}