package io.choerodon.devops.api.controller.v1


import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.IssueDTO
import io.choerodon.devops.app.service.IssueService
import io.choerodon.devops.domain.application.entity.ApplicationE
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabProjectE
import io.choerodon.devops.domain.application.entity.iam.UserE
import io.choerodon.devops.domain.application.repository.ApplicationRepository
import io.choerodon.devops.domain.application.repository.DevopsGitRepository
import io.choerodon.devops.domain.application.repository.DevopsMergeRequestRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.dataobject.DevopsBranchDO
import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO
import io.choerodon.devops.infra.dataobject.gitlab.CommitDO
import io.choerodon.devops.infra.mapper.DevopsBranchMapper
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Specification
import spock.lang.Stepwise

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by n!Ck
 * Date: 2018/9/6
 * Time: 20:51
 * Description: 
 */

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class IssueControllerSpec extends Specification {

    private static flag = 0

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private IssueService issueService
    @Autowired
    private ApplicationRepository applicationRepository

    @Autowired
    private DevopsBranchMapper devopsBranchMapper
    @Autowired
    private DevopsMergeRequestMapper devopsMergeRequestMapper
    @Autowired
    private DevopsMergeRequestRepository devopsMergeRequestRepository

    @Autowired
    @Qualifier("mockDevopsGitRepository")
    private DevopsGitRepository devopsGitRepository

    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository

    private List<CommitDO> commitDOS

    def setup() {
        if (flag == 0) {
            Organization organization = new Organization()
            organization.setId(1L)
            organization.setCode("ocode")

            ProjectE projectE = new ProjectE()
            projectE.setId(1L)
            projectE.setCode("pcode")
            projectE.setOrganization(organization)

            GitlabProjectE gitlabProjectE = new GitlabProjectE(1)

            ApplicationE applicationE = new ApplicationE()
            applicationE.setName("name")
            applicationE.setProjectE(projectE)
            applicationE.setGitlabProjectE(gitlabProjectE)
            applicationRepository.create(applicationE)

            Date date = new Date(2018, 9, 7, 9, 18, 0)
            Date date1 = new Date(2018, 9, 7, 9, 25, 0)
            DevopsBranchDO devopsBranchDO = new DevopsBranchDO()
            devopsBranchDO.setIssueId(1L)
            devopsBranchDO.setDeleted(false)
            devopsBranchDO.setCheckoutDate(date)
            devopsBranchDO.setBranchName("branch")
            devopsBranchDO.setAppId(1L)
            DevopsBranchDO devopsBranchDO1 = new DevopsBranchDO()
            devopsBranchDO1.setIssueId(1L)
            devopsBranchDO1.setDeleted(false)
            devopsBranchDO1.setCheckoutDate(date1)
            devopsBranchDO1.setBranchName("branch1")
            devopsBranchDO1.setAppId(1L)
            devopsBranchMapper.insert(devopsBranchDO)
            devopsBranchMapper.insert(devopsBranchDO1)

            DevopsMergeRequestDO devopsMergeRequestDO = new DevopsMergeRequestDO()
            devopsMergeRequestDO.setProjectId(1L)
            devopsMergeRequestDO.setAuthorId(1L)
            devopsMergeRequestDO.setAssigneeId(1L)
            devopsMergeRequestDO.setSourceBranch("branch")
            devopsMergeRequestDO.setTargetBranch("branch111")
            devopsMergeRequestDO.setUpdatedAt(date)
            devopsMergeRequestDO.setState("opened")
            DevopsMergeRequestDO devopsMergeRequestDO1 = new DevopsMergeRequestDO()
            devopsMergeRequestDO1.setProjectId(1L)
            devopsMergeRequestDO1.setAuthorId(1L)
            devopsMergeRequestDO1.setAssigneeId(1L)
            devopsMergeRequestDO1.setSourceBranch("branch1")
            devopsMergeRequestDO1.setTargetBranch("branch111")
            devopsMergeRequestDO1.setUpdatedAt(date1)
            devopsMergeRequestDO1.setState("opened")
            devopsMergeRequestMapper.insert(devopsMergeRequestDO)
            devopsMergeRequestMapper.insert(devopsMergeRequestDO1)

            commitDOS = new ArrayList<>()
            CommitDO commitDO = new CommitDO()
            commitDO.setId("commitNot")
            commitDO.setCreatedAt(date)
            CommitDO commitDO1 = new CommitDO()
            commitDO1.setId("commitNot1")
            commitDO1.setCreatedAt(date1)
            commitDOS.add(commitDO)
            commitDOS.add(commitDO1)

            flag = 1
        }
    }

    def "GetCommitsByIssueId"() {
        given:
        UserE userE = new UserE()
        userE.setLoginName("login")
        userE.setRealName("realName")
        userE.setImageUrl("imageUrl")

        when:
        def list = restTemplate.getForObject("/v1/project/1/issue/1/commit/list", List.class)

        then:
        devopsGitRepository.getGitLabId(_ as Long) >> Integer.valueOf(1)
        devopsGitRepository.getCommits(_ as Integer, _ as String, _ as String) >> commitDOS
        iamRepository.queryUserByUserId(1L) >> userE
        devopsGitRepository.getUserIdByGitlabUserId(_ as Long) >> Long.valueOf(1)
        !list.isEmpty()
    }

    def "GetMergeRequestsByIssueId"() {
        given:
        UserE userE = new UserE()
        userE.setLoginName("login")
        userE.setRealName("realName")
        userE.setImageUrl("imageUrl")

        when:
        def list = restTemplate.getForObject("/v1/project/1/issue/1/merge_request/list", List.class)

        then:
        devopsGitRepository.getGitLabId(_ as Long) >> Integer.valueOf(1)
        iamRepository.queryUserByUserId(1L) >> userE
        devopsGitRepository.getUserIdByGitlabUserId(_ as Long) >> Long.valueOf(1)
        !list.isEmpty()
    }

    def "CountCommitAndMergeRequest"() {
        given:
        UserE userE = new UserE()
        userE.setLoginName("login")
        userE.setRealName("realName")
        userE.setImageUrl("imageUrl")

        Date date = new Date(2018, 9, 7, 14, 14, 14)
        Date date1 = new Date(2018, 9, 8, 14, 30, 30)
        List<CommitDO> commitDOSCount = new ArrayList<>()
        CommitDO commitDO = new CommitDO()
        commitDO.setId("commitNot")
        commitDO.setCreatedAt(date)
        CommitDO commitDO1 = new CommitDO()
        commitDO1.setId("commitNot1")
        commitDO1.setCreatedAt(date1)
        commitDOSCount.add(commitDO)
        commitDOSCount.add(commitDO1)

        when:
        def issueDTO = restTemplate.getForObject("/v1/project/1/issue/1/commit_and_merge_request/count", IssueDTO.class)

        then:
        devopsGitRepository.getGitLabId(_ as Long) >> Integer.valueOf(1)
        devopsGitRepository.getCommits(_ as Integer, _ as String, _ as String) >> commitDOSCount
        iamRepository.queryUserByUserId(1L) >> userE
        devopsGitRepository.getUserIdByGitlabUserId(_ as Long) >> Long.valueOf(1)
        issueDTO != null
    }
}
