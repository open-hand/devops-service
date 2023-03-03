package io.choerodon.devops.app.service;

import java.util.List;
import java.util.Map;

import io.choerodon.devops.api.vo.jenkins.JenkinsJobVO;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2023/3/3 9:24
 */
public interface JenkinsJobService {
    List<JenkinsJobVO> listAll(Long projectId);

    void build(Long projectId, Long serverId, String folder, String name, Map<String, String> params);

}
