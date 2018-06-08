package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.dto.GitlabCommitDTO;

/**
 * Created by Zenger on 2018/5/3.
 */
public interface GitlabCommitService {

    /**
     * 查询gitlab下的Commit信息
     *
     * @param gitlabProjectId gitlab项目id
     * @param shas            关联pipeline的值
     * @return GitlabCommitDTO
     */
    List<GitlabCommitDTO> getGitlabCommit(Integer gitlabProjectId, List<String> shas);
}
