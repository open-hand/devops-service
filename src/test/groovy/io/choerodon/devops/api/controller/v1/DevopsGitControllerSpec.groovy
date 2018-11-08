package io.choerodon.devops.api.controller.v1

import io.choerodon.core.domain.Page
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.dto.DevopsBranchDTO
import io.choerodon.devops.domain.application.entity.ProjectE
import io.choerodon.devops.domain.application.entity.UserAttrE
import io.choerodon.devops.domain.application.entity.gitlab.CommitE
import io.choerodon.devops.domain.application.entity.iam.UserE
import io.choerodon.devops.domain.application.repository.*
import io.choerodon.devops.domain.application.valueobject.Issue
import io.choerodon.devops.domain.application.valueobject.Organization
import io.choerodon.devops.infra.dataobject.ApplicationDO
import io.choerodon.devops.infra.dataobject.DevopsMergeRequestDO
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO
import io.choerodon.devops.infra.dataobject.gitlab.CommitDO
import io.choerodon.devops.infra.dataobject.gitlab.TagDO
import io.choerodon.devops.infra.dataobject.iam.OrganizationDO
import io.choerodon.devops.infra.dataobject.iam.ProjectDO
import io.choerodon.devops.infra.dataobject.iam.UserDO
import io.choerodon.devops.infra.feign.AgileServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.IamServiceClient
import io.choerodon.devops.infra.mapper.ApplicationMapper
import io.choerodon.devops.infra.mapper.DevopsBranchMapper
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper
import io.choerodon.devops.infra.mapper.UserAttrMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyString
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsGitController)
@Stepwise
class DevopsGitControllerSpec extends Specification {

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
    private DevopsBranchRepository devopsBranchRepository

    @Autowired
    private IamRepository iamRepository
    @Autowired
    private UserAttrRepository userAttrRepository

    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)
    AgileServiceClient agileServiceClient = Mockito.mock(AgileServiceClient.class)
    IamServiceClient iamServiceClient = Mockito.mock(IamServiceClient.class)

    @Shared
    ApplicationDO applicationDO = new ApplicationDO()

    def setupSpec() {
        applicationDO.setId(1L)
        applicationDO.setProjectId(1L)
        applicationDO.setCode("test")
        applicationDO.setName("test")
        applicationDO.setGitlabProjectId(1)
    }

    def setup() {
        iamRepository.initMockIamService(iamServiceClient)
        devopsGitRepository.initGitlabServiceClient(gitlabServiceClient)

        ProjectDO projectDO = new ProjectDO()
        projectDO.setName("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(iamServiceClient).queryIamProject(1L)
        OrganizationDO organizationDO = new OrganizationDO()
        organizationDO.setId(1L)
        organizationDO.setCode("testOrganization")
        ResponseEntity<OrganizationDO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(iamServiceClient).queryOrganizationById(1L)

        UserDO userDO = new UserDO()
        userDO.setLoginName("test")
        userDO.setId(1L)
        ResponseEntity<UserDO> responseEntity2 = new ResponseEntity<>(userDO, HttpStatus.OK)
        Mockito.when(iamServiceClient.queryByLoginName(anyString())).thenReturn(responseEntity2)

        List<UserDO> userDOList = new ArrayList<>()
        userDOList.add(userDO)
        ResponseEntity<List<UserDO>> responseEntity3 = new ResponseEntity<>(userDOList, HttpStatus.OK)
        Mockito.when(iamServiceClient.listUsersByIds(any(Long[].class))).thenReturn(responseEntity3)
    }

    def "GetUrl"() {
        given: '初始化变量'
        applicationMapper.insert(applicationDO)

        when: '获取工程下地址'
        def url = restTemplate.getForObject("/v1/projects/1/apps/1/git/url", String.class)

        then: '校验返回结果'
        !url.equals("")
    }

    def "CreateTag"() {
        given: 'mock gitlab创建tag'
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        Mockito.doReturn(null).when(gitlabServiceClient).createTag(null, null, null, null, null, null)

        when: '创建标签'
        restTemplate.postForEntity("/v1/projects/1/apps/1/git/tags?tag=test&ref=test&message=test", "test", Object.class)

        then: '校验'
        userAttrRepository.queryById(_ as Long) >> userAttrE
    }

    def "UpdateTagRelease"() {
        given: 'mock 更新tag'
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        ResponseEntity<TagDO> responseEntity = new ResponseEntity<>(new TagDO(), HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(gitlabServiceClient).updateTagRelease(1, "test", "test", 1)

        when: '更新标签'
        restTemplate.put("/v1/projects/1/apps/1/git/tags?tag=test", "test", Object.class)

        then: '校验'
        userAttrRepository.queryById(_ as Long) >> userAttrE
    }

    def "GetTagByPage"() {
        given:
        Organization organization = initOrg(1L, "testOrganization")
        ProjectE projectE = initProj(1L, "testProject", organization)
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
        Mockito.doReturn(tagResponseEntity).when(gitlabServiceClient).getTags(1, 1)
        userAttrRepository.queryById(_ as Long) >> userAttrE

        when: '获取标签分页列表'
        def page = restTemplate.postForObject("/v1/projects/1/apps/1/git/tags_list_options?page=0&size=10", null, Page.class)

        then: '校验返回值'
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
        Mockito.doReturn(tagResponseEntity).when(gitlabServiceClient).getTags(1, 1)
        userAttrRepository.queryById(_ as Long) >> userAttrE

        when: '获取标签分页列表'
        def tags = restTemplate.getForObject("/v1/projects/1/apps/1/git/tag_list", List.class)

        then: '校验返回值'
        tags.size() == 1
    }

    def "CheckTag"() {
        given: 'mock gitlab获取tag'
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
        Mockito.doReturn(tagResponseEntity).when(gitlabServiceClient).getTags(1, 1)
        userAttrRepository.queryById(_ as Long) >> userAttrE

        when: '获取标签列表'
        def exist = restTemplate.getForObject("/v1/projects/1/apps/1/git/tags_check?tag_name=test", Boolean.class)

        then:
        exist
    }

    def "DeleteTag"() {
        given: 'mock gitlab删除tag'
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        Mockito.doReturn(null).when(gitlabServiceClient).deleteTag(null, null, null)

        when: '检查标签'
        restTemplate.delete("/v1/projects/1/apps/1/git/tags?tag=test")

        then: '返回值'
        userAttrRepository.queryById(_ as Long) >> userAttrE
    }

    def "CreateBranch"() {
        given: 'mock gitlab创建分支'
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
        Mockito.doReturn(branchDOResponseEntity).when(gitlabServiceClient).createBranch(1, "test", "test", 1)
        userAttrRepository.queryById(_ as Long) >> userAttrE

        when: '创建分支'
        restTemplate.postForObject("/v1/projects/1/apps/1/git/branch", devopsBranchDTO, Object.class)

        then: '校验返回值'
        devopsBranchMapper.selectAll().get(0)["branchName"] == "test"
    }

    def "ListByAppId"() {
        given: 'mock gitlab查询issue'
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
        userAttrRepository.queryById(_ as Long) >> userAttrE

        when: '获取工程下所有分支名'
        def branches = restTemplate.postForObject("/v1/projects/1/apps/1/git/branches?page=0&size=10", null, Page.class)


        then: '校验返回值'
        branches.size() == 1
    }

    def "QueryByAppId"() {
        when: '查询单个分支'
        def devopsBranch = restTemplate.getForObject("/v1/projects/1/apps/1/git/branch?branchName=test", DevopsBranchDTO.class)

        then: '校验返回值'
        devopsBranch["branchName"] == "test"
    }

    def "Update"() {
        given: '初始化branchDTO类'
        DevopsBranchDTO devopsBranchDTO = new DevopsBranchDTO()
        devopsBranchDTO.setBranchName("test")
        devopsBranchDTO.setIssueId(2L)

        when: '更新分支关联的问题'
        restTemplate.put("/v1/projects/1/apps/1/git/branch", devopsBranchDTO)

        then: '校验返回值'
        devopsBranchMapper.selectByPrimaryKey(devopsBranchMapper.selectAll().get(0).getId()).getIssueId() == 2L
    }

    def "Delete"() {
        given: 'mock gitlab删除分支'
        UserAttrE userAttrE = new UserAttrE()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        Mockito.doReturn(null).when(gitlabServiceClient).deleteBranch(1, "test", 1)
        userAttrRepository.queryById(_ as Long) >> userAttrE

        when: '删除分支'
        restTemplate.delete("/v1/projects/1/apps/1/git/branch?branchName=test")

        then: '校验返回值'
        devopsBranchMapper.selectByPrimaryKey(devopsBranchMapper.selectAll().get(0).getId()).getDeleted()
    }

    def "GetMergeRequestList"() {
        given: 'mock 查询commits'
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

        when: '查看所有合并请求'
        def mergeRequest = restTemplate.getForObject("/v1/projects/1/apps/1/git//merge_request/list?page=0&size=10", Map.class)

        then: '校验返回值'
        !mergeRequest.isEmpty()
        applicationMapper.deleteByPrimaryKey(1L)
        devopsBranchMapper.deleteByPrimaryKey(devopsBranchMapper.selectAll().get(0).getId())
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
