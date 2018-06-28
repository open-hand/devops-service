package io.choerodon.devops.api.controller.v1;

import java.util.List;
import java.util.Optional;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.InitRoleCode;
import io.choerodon.devops.api.dto.GitlabCommitDTO;
import io.choerodon.devops.app.service.GitlabCommitService;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by Zenger on 2018/4/2.
 */
@RestController
@RequestMapping(value = "/v1/projects/{project_id}/gitlab_projects")
public class GitlabCommitController {

    private GitlabCommitService gitlabCommitService;

    public GitlabCommitController(GitlabCommitService gitlabCommitService) {
        this.gitlabCommitService = gitlabCommitService;
    }

    /**
     * 查询gitlab下的Commit信息
     *
     * @param projectId       项目id
     * @param gitlabProjectId gitlab项目id
     * @param shas            关联pipeline的值
     * @return GitlabCommitDTO
     */
    @Permission(roles = {InitRoleCode.PROJECT_OWNER, InitRoleCode.PROJECT_MEMBER, InitRoleCode.DEPLOY_ADMINISTRATOR})
    @ApiOperation(value = "查询gitlab下的Commit信息")
    @CustomPageRequest
    @PostMapping(value = "/{gitlabProjectId}/commit_sha")
    public ResponseEntity<List<GitlabCommitDTO>> list(
            @ApiParam(value = "项目ID", required = true)
            @PathVariable(value = "project_id") Long projectId,
            @ApiParam(value = "gitlab项目ID", required = true)
            @PathVariable Integer gitlabProjectId,
            @ApiParam(value = "关联pipeline的值", required = true)
            @RequestBody List<String> shas) {
        return Optional.ofNullable(gitlabCommitService.getGitlabCommit(gitlabProjectId, shas))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.gitlab.commit.query"));
    }
}
