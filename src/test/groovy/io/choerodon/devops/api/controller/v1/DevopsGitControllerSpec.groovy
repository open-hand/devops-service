package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsBranchDTO
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.entity.gitlab.CommitE
import io.choerodon.devops.domain.application.entity.iam.UserE
import io.choerodon.devops.domain.application.repository.AgileRepository
import io.choerodon.devops.domain.application.repository.DevopsGitRepository
import io.choerodon.devops.domain.application.repository.IamRepository
import io.choerodon.devops.domain.application.repository.UserAttrRepository
import io.choerodon.devops.domain.application.valueobject.Issue
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.dataobject.ApplicationDO
import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO
import io.choerodon.devops.infra.dataobject.gitlab.CommitDO
import io.choerodon.devops.infra.dataobject.gitlab.TagDO
import io.choerodon.devops.infra.feign.AgileServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.mapper.ApplicationMapper
import io.choerodon.devops.infra.mapper.DevopsBranchMapper
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper
import io.choerodon.devops.infra.mapper.UserAttrMapper
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

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Stepwise
class DevopsGitControllerSpec extends Specification {


    private static flag = 0


    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private ApplicationMapper applicationMapper
    @Autowired
    private DevopsGitRepository devopsGitRepository
    @Autowired
    private DevopsBranchMapper devopsBranchMapper
    @Autowired
    private AgileRepository agileRepository
    @Autowired
    private DevopsMergeRequestMapper devopsMergeRequestMapper
    @Autowired
    private UserAttrMapper userAttrMapper

    @Autowired
    @Qualifier("mockIamRepository")
    private IamRepository iamRepository

    @Autowired
    @Qualifier("mockUserAttrRepository")
    private UserAttrRepository userAttrRepository

    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)
    AgileServiceClient agileServiceClient = Mockito.mock(AgileServiceClient.class)

    void setup() {
        if (flag == 0) {

            ApplicationDO applicationDO = new ApplicationDO()
            applicationDO.setId(1L)
            applicationDO.setProjectId(1L)
            applicationDO.setCode("test")
            applicationDO.setName("test")
            applicationDO.setGitlabProjectId(1)
            applicationMapper.insert(applicationDO)

            UserAttrE userAttrE = new UserAttrE()
            userAttrE.setIamUserId(1L)
            userAttrE.setGitlabUserId(1L)

            flag = 1
        }
    }

    def "GetUrl"() {
        given:
        Organization organization = initOrg(1L, "testOrganization")
        ProjectE projectE = initProj(1L, "testProject", organization)
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization


        when:
        def url = restTemplate.getForObject("/v1/projects/1/apps/1/git/url", String.class)


        then:
        !url.equals("")


    }

    def "CreateTag"() {
        given:
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        devopsGitRepository.initGitlabServiceClient(gitlabServiceClient)
        Mockito.doReturn(null).when(gitlabServiceClient).createTag(null, null, null, null, null, null)


        when:
        restTemplate.postForEntity("/v1/projects/1/apps/1/git/tags?tag=test&ref=test&message=test", "test", Object.class)

        then:
        userAttrRepository.queryById(_ as Long) >> userAttrE


    }

    def "UpdateTagRelease"() {
        given:
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        ResponseEntity<TagDO> responseEntity = new ResponseEntity<>(new TagDO(), HttpStatus.OK)
        devopsGitRepository.initGitlabServiceClient(gitlabServiceClient)
        Mockito.doReturn(responseEntity).when(gitlabServiceClient).updateTagRelease(1, "test", "test", 1)

        when:
        restTemplate.put("/v1/projects/1/apps/1/git/tags?tag=test", "test", Object.class)

        then:
        userAttrRepository.queryById(_ as Long) >> userAttrE

    }

    def "GetTagByPage"() {
        given:
        Organization organization = initOrg(1L, "testOrganization")
        ProjectE projectE = initProj(1L, "testProject", organization)
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        UserE userE = new UserE();
        userE.setLoginName("test")
        userE.setId(1L)
        List<TagDO> tagDOS = new ArrayList<>();
        TagDO tagDO = new TagDO()
        CommitDO commitDO = new CommitDO();
        commitDO.setId("test")
        commitDO.setAuthorName("test")
        tagDO.setCommit(commitDO)
        tagDOS.add(tagDO)
        ResponseEntity<List<TagDO>> tagResponseEntity = new ResponseEntity<>(tagDOS, HttpStatus.OK)
        devopsGitRepository.initGitlabServiceClient(gitlabServiceClient)
        Mockito.doReturn(tagResponseEntity).when(gitlabServiceClient).getTags(1, 1)
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
        iamRepository.queryByLoginName(_ as String) >> userE
        userAttrRepository.queryById(_ as Long) >> userAttrE

        when:
        def page = restTemplate.postForObject("/v1/projects/1/apps/1/git/tags_list_options?page=0&size=10", null, Page.class)


        then:
        page.size() == 1

    }

    def "GetTagList"() {
        given:
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        List<TagDO> tagDOS = new ArrayList<>();
        TagDO tagDO = new TagDO()
        CommitDO commitDO = new CommitDO();
        commitDO.setId("test")
        commitDO.setAuthorName("test")
        tagDO.setCommit(commitDO)
        tagDOS.add(tagDO)
        ResponseEntity<List<TagDO>> tagResponseEntity = new ResponseEntity<>(tagDOS, HttpStatus.OK)
        devopsGitRepository.initGitlabServiceClient(gitlabServiceClient)
        Mockito.doReturn(tagResponseEntity).when(gitlabServiceClient).getTags(1, 1)
        userAttrRepository.queryById(_ as Long) >> userAttrE


        when:
        def tags = restTemplate.getForObject("/v1/projects/1/apps/1/git/tag_list", List.class)


        then:
        tags.size() == 1


    }

    def "CheckTag"() {
        given:
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        List<TagDO> tagDOS = new ArrayList<>();
        TagDO tagDO = new TagDO()
        CommitDO commitDO = new CommitDO();
        commitDO.setId("test")
        commitDO.setAuthorName("test")
        tagDO.setCommit(commitDO)
        tagDOS.add(tagDO)
        ResponseEntity<List<TagDO>> tagResponseEntity = new ResponseEntity<>(tagDOS, HttpStatus.OK)
        devopsGitRepository.initGitlabServiceClient(gitlabServiceClient)
        Mockito.doReturn(tagResponseEntity).when(gitlabServiceClient).getTags(1, 1)
        userAttrRepository.queryById(_ as Long) >> userAttrE


        when:
        def exist = restTemplate.getForObject("/v1/projects/1/apps/1/git/tags_check?tag_name=test", Boolean.class)

        then:
        exist == true

    }

    def "DeleteTag"() {
        given:
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        devopsGitRepository.initGitlabServiceClient(gitlabServiceClient)
        Mockito.doReturn(null).when(gitlabServiceClient).deleteTag(null, null, null)

        when:
        restTemplate.delete("/v1/projects/1/apps/1/git/tags?tag=test")

        then:
        userAttrRepository.queryById(_ as Long) >> userAttrE

    }

    def "CreateBranch"() {
        given:
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO()
        devopsBranchDTO.setAppName("test")
        devopsBranchDTO.setBranchName("test")
        devopsBranchDTO.setIssueId(1L)
        devopsBranchDTO.setAppId(1L)
        devopsBranchDTO.setOriginBranch("test")
        BranchDO branchDO = new BranchDO()
        CommitE commitE = new CommitE();
        commitE.setId("test")
        commitE.setCommittedDate(new Date())
        commitE.setMessage("test")
        branchDO.setCommit(commitE)
        ResponseEntity<BranchDO> branchDOResponseEntity = new ResponseEntity<>(branchDO, HttpStatus.OK)
        devopsGitRepository.initGitlabServiceClient(gitlabServiceClient)
        Mockito.doReturn(branchDOResponseEntity).when(gitlabServiceClient).createBranch(1, "test", "test", 1)
        userAttrRepository.queryById(_ as Long) >> userAttrE

        when:
        restTemplate.postForObject("/v1/projects/1/apps/1/git/branch", devopsBranchDTO, Object.class)

        then:
        devopsBranchMapper.selectByPrimaryKey(1L).getBranchName().equals("test")

    }

    def "ListByAppId"() {
        given:
        Organization organization = initOrg(1L, "testOrganization")
        ProjectE projectE = initProj(1L, "testProject", organization)
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        Issue issue = new Issue()
        ResponseEntity<Issue> issueResponseEntity = new ResponseEntity<>(issue, HttpStatus.OK)
        UserE userE = new UserE()
        userE.setLoginName("test")
        userE.setId(1L)
        userE.setRealName("test")
        userE.setImageUrl("test")
        agileRepository.initAgileServiceClient(agileServiceClient)
        Mockito.doReturn(issueResponseEntity).when(agileServiceClient).queryIssue(1, 1)
        iamRepository.queryIamProject(_ as Long) >> projectE
        iamRepository.queryOrganizationById(_ as Long) >> organization
        2 * iamRepository.queryUserByUserId(_) >> userE
        userAttrRepository.queryById(_ as Long) >> userAttrE

        when:
        def branches = restTemplate.postForObject("/v1/projects/1/apps/1/git/branches?page=0&size=10", null, Page.class)


        then:
        branches.size() == 1

    }

    def "QueryByAppId"() {
        given:

        when:
        def devopsBranch = restTemplate.getForObject("/v1/projects/1/apps/1/git/branch?branchName=test", DevopsBranchDTO.class)


        then:
        devopsBranch.getBranchName() == "test"

    }

    def "Update"() {
        given:
        DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO()
        devopsBranchDTO.setBranchName("test")
        devopsBranchDTO.setIssueId(2L)

        when:
        restTemplate.put("/v1/projects/1/apps/1/git/branch", devopsBranchDTO)


        then:
        devopsBranchMapper.selectByPrimaryKey(1L).getIssueId() == 2L

    }

    def "Delete"() {
        given:
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        devopsGitRepository.initGitlabServiceClient(gitlabServiceClient)
        Mockito.doReturn(null).when(gitlabServiceClient).deleteBranch(1, "test", 1)
        userAttrRepository.queryById(_ as Long) >> userAttrE

        when:
        restTemplate.delete("/v1/projects/1/apps/1/git/branch?branchName=test")

        then:
        devopsBranchMapper.selectByPrimaryKey(1L).getDeleted() == true

    }

    def "GetMergeRequestList"() {
        given:
        UserE userE = new UserE()
        userE.setLoginName("test")
        userE.setId(1L)
        userE.setRealName("test")
        userE.setImageUrl("test")
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        DevopsMergeRequestDO devopsMergeRequestDO = new DevopsMergeRequestDO()
        devopsMergeRequestDO.setId(1L)
        devopsMergeRequestDO.setState("merged")
        devopsMergeRequestDO.setProjectId(1L)
        devopsMergeRequestDO.setGitlabMergeRequestId(1L)
        devopsMergeRequestDO.setAuthorId(1L)
        devopsMergeRequestDO.setAssigneeId(1L)
        devopsMergeRequestMapper.insert(devopsMergeRequestDO)
        DevopsMergeRequestDO devopsMergeRequestDO1 = new DevopsMergeRequestDO()
        devopsMergeRequestDO1.setId(2L)
        devopsMergeRequestDO1.setState("closed")
        devopsMergeRequestDO1.setProjectId(1L)
        devopsMergeRequestDO1.setGitlabMergeRequestId(2L)
        devopsMergeRequestDO1.setAuthorId(1L)
        devopsMergeRequestDO1.setAssigneeId(1L)
        devopsMergeRequestMapper.insert(devopsMergeRequestDO1)
        DevopsMergeRequestDO devopsMergeRequestDO2 = new DevopsMergeRequestDO()
        devopsMergeRequestDO2.setId(3L)
        devopsMergeRequestDO2.setState("opened")
        devopsMergeRequestDO2.setProjectId(1L)
        devopsMergeRequestDO2.setGitlabMergeRequestId(3L)
        devopsMergeRequestDO2.setAuthorId(1L)
        devopsMergeRequestDO2.setAssigneeId(1L)
        devopsMergeRequestMapper.insert(devopsMergeRequestDO2)
        List<CommitDO> commitDOList = new ArrayList<>();
        CommitDO commitDO = new CommitDO()
        commitDOList.add(commitDO)
        ResponseEntity<List<CommitDO>> commitDOS = new ResponseEntity<>(commitDOList, HttpStatus.OK)
        devopsGitRepository.initGitlabServiceClient(gitlabServiceClient)
        Mockito.doReturn(commitDOS).when(gitlabServiceClient).listCommits(1, 1, 1)
        Mockito.doReturn(commitDOS).when(gitlabServiceClient).listCommits(1, 2, 1)
        Mockito.doReturn(commitDOS).when(gitlabServiceClient).listCommits(1, 3, 1)
        userAttrRepository.queryById(_ as Long) >> userAttrE
        6 * userAttrRepository.queryUserIdByGitlabUserId(_) >> 1L
        6 * iamRepository.queryUserByUserId(_) >> userE

        when:
        def mergeRequest = restTemplate.getForObject("/v1/projects/1/apps/1/git//merge_request/list?page=0&size=10", Map.class)

        then:
        !mergeRequest.isEmpty()
        applicationMapper.deleteByPrimaryKey(1L)
        devopsBranchMapper.deleteByPrimaryKey(1L)
        devopsMergeRequestMapper.deleteByPrimaryKey(1L)
        devopsMergeRequestMapper.deleteByPrimaryKey(2L)
        devopsMergeRequestMapper.deleteByPrimaryKey(3L)


    }


    private static Organization initOrg(Long id, String code) {
        Organization organization = new Organization()
        organization.setId(id)
        organization.setCode(code)
        organization
    }

    private static ProjectE initProj(Long id, String code, Organization organization) {
        ProjectE projectE = new ProjectE()
        projectE.setId(id)
        projectE.setCode(code)
        projectE.setOrganization(organization)
        projectE
    }
}
