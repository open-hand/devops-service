package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.IssueDTO
import io.choerodon.devops.app.service.IssueService
import io.choerodon.devops.domain.application.entity.ApplicationE
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.entity.gitlab.GitlabProjectE
import io.choerodon.devops.domain.application.entity.iam.UserE
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.common.util.FileUtil
import io.choerodon.devops.infra.dataobject.DevopsBranchDO
import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO
import io.choerodon.devops.infra.dataobject.gitlab.CommitDO
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.mapper.DevopsBranchMapper
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    private DevopsBranchMapper devopsBranchMapper
    @Autowired
    private ApplicationRepository applicationRepository
    @Autowired
    private DevopsMergeRequestMapper devopsMergeRequestMapper
    @Autowired
    private DevopsMergeRequestRepository devopsMergeRequestRepository
    @Autowired
    private UserAttrRepository userAttrRepository

    @Autowired
    private DevopsGitRepository devopsGitRepository
    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository

    private List<CommitDO> commitDOS

    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)

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
            applicationE.setId(1L)
            applicationE.setName("name")
            applicationE.setProjectE(projectE)
            applicationE.setGitlabProjectE(gitlabProjectE)
            applicationRepository.create(applicationE)

            Date date = new Date(2018, 9, 7, 9, 18, 0)
            Date date1 = new Date(2018, 9, 7, 9, 18, 0)
            DevopsBranchDO devopsBranchDO = new DevopsBranchDO()
            devopsBranchDO.setId(1L)
            devopsBranchDO.setIssueId(1L)
            devopsBranchDO.setDeleted(false)
            devopsBranchDO.setCheckoutDate(date)
            devopsBranchDO.setBranchName("branch")
            devopsBranchDO.setAppId(1L)
            DevopsBranchDO devopsBranchDO1 = new DevopsBranchDO()
            devopsBranchDO1.setId(2L)
            devopsBranchDO1.setIssueId(1L)
            devopsBranchDO1.setDeleted(false)
            devopsBranchDO1.setCheckoutDate(date1)
            devopsBranchDO1.setBranchName("branch1")
            devopsBranchDO1.setAppId(1L)
            devopsBranchMapper.insert(devopsBranchDO)


            DevopsMergeRequestDO devopsMergeRequestDO = new DevopsMergeRequestDO()
            devopsMergeRequestDO.setId(1L)
            devopsMergeRequestDO.setProjectId(1L)
            devopsMergeRequestDO.setAuthorId(1L)
            devopsMergeRequestDO.setAssigneeId(1L)
            devopsMergeRequestDO.setSourceBranch("branch")
            devopsMergeRequestDO.setTargetBranch("branch111")
            devopsMergeRequestDO.setUpdatedAt(date)
            devopsMergeRequestDO.setState("opened")
            DevopsMergeRequestDO devopsMergeRequestDO1 = new DevopsMergeRequestDO()
            devopsMergeRequestDO1.setId(2L)
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
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        List<CommitDO> commitDOS1 = new ArrayList<>();
        CommitDO commitDO = new CommitDO();
        commitDO.setId("test")
        commitDOS1.add(commitDO)

        ResponseEntity<List<CommitDO>> responseEntity = new ResponseEntity<>(commitDOS1, HttpStatus.OK)
        devopsGitRepository.initGitlabServiceClient(gitlabServiceClient)
        Mockito.doReturn(responseEntity).when(gitlabServiceClient).getCommits(1, "branch", "3918-10-07 09:18:00 CST")
        userAttrRepository.queryById(_ as Long) >> userAttrE
        iamRepository.queryUserByUserId(1L) >> userE
        userAttrRepository.queryUserIdByGitlabUserId(_) >> 1L

        when:
        def list = restTemplate.getForObject("/v1/project/1/issue/1/commit/list", List.class)

        then:
        !list.isEmpty()
    }

    def "GetMergeRequestsByIssueId"() {
        given:
        UserE userE = new UserE()
        userE.setLoginName("login")
        userE.setRealName("realName")
        userE.setImageUrl("imageUrl")
        iamRepository.queryUserByUserId(1L) >> userE
        userAttrRepository.queryUserIdByGitlabUserId(_) >> 1L

        when:
        def list = restTemplate.getForObject("/v1/project/1/issue/1/merge_request/list", List.class)

        then:
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
        CommitDO commitDO = new CommitDO()
        commitDO.setId("commitNot")
        commitDO.setCreatedAt(date)
        CommitDO commitDO1 = new CommitDO()
        commitDO1.setId("commitNot1")
        commitDO1.setCreatedAt(date1)
        List<CommitDO> commitDOS1 = new ArrayList<>()
        commitDOS1.add(commitDO)

        ResponseEntity<List<CommitDO>> responseEntity = new ResponseEntity<>(commitDOS1, HttpStatus.OK)
        devopsGitRepository.initGitlabServiceClient(gitlabServiceClient)
        Mockito.doReturn(responseEntity).when(gitlabServiceClient).getCommits(1, "branch", "3918-10-07 09:18:00 CST")
        iamRepository.queryUserByUserId(1L) >> userE
        userAttrRepository.queryUserIdByGitlabUserId(_) >> 1L

        when:
        def issueDTO = restTemplate.getForObject("/v1/project/1/issue/1/commit_and_merge_request/count", IssueDTO.class)

        then:
        issueDTO != null
        applicationRepository.delete(1L)
        devopsBranchMapper.deleteByPrimaryKey(1L)
        devopsMergeRequestMapper.deleteByPrimaryKey(1L)
        devopsMergeRequestMapper.deleteByPrimaryKey(2L)
    }

    /**
     * 清理中间目录
     */
    def cleanupSpec() {
        FileUtil.deleteDirectory(new File("gitops"))
        FileUtil.deleteDirectory(new File("Charts"))
        FileUtil.deleteDirectory(new File("devopsversion"))
    }
}
