package io.choerodon.devops.api.controller.v1

import com.github.pagehelper.PageInfo
import io.choerodon.core.exception.CommonException
import io.choerodon.core.exception.ExceptionResponse
import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.DevopsBranchVO
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO
import io.choerodon.devops.api.vo.iam.RoleVO
import io.choerodon.devops.app.service.AppServiceService
import io.choerodon.devops.app.service.DevopsBranchService
import io.choerodon.devops.app.service.DevopsGitService
import io.choerodon.devops.app.service.IamService
import io.choerodon.devops.app.service.UserAttrService
import io.choerodon.devops.infra.dto.AppServiceDTO
import io.choerodon.devops.infra.dto.DevopsBranchDTO
import io.choerodon.devops.infra.dto.DevopsMergeRequestDTO
import io.choerodon.devops.infra.dto.UserAttrDTO
import io.choerodon.devops.infra.dto.agile.IssueDTO
import io.choerodon.devops.infra.dto.gitlab.BranchDTO
import io.choerodon.devops.infra.dto.gitlab.CommitDTO
import io.choerodon.devops.infra.dto.gitlab.MemberDTO
import io.choerodon.devops.infra.dto.gitlab.TagDTO
import io.choerodon.devops.infra.dto.iam.IamUserDTO
import io.choerodon.devops.infra.dto.iam.OrganizationDTO
import io.choerodon.devops.infra.dto.iam.ProjectDTO
import io.choerodon.devops.infra.enums.AccessLevel
import io.choerodon.devops.infra.feign.AgileServiceClient
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.GitlabServiceClient
import io.choerodon.devops.infra.feign.operator.AgileServiceClientOperator
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator
import io.choerodon.devops.infra.mapper.AppServiceMapper
import io.choerodon.devops.infra.mapper.DevopsBranchMapper
import io.choerodon.devops.infra.mapper.DevopsMergeRequestMapper
import io.choerodon.devops.infra.mapper.UserAttrMapper
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
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

import static org.mockito.ArgumentMatchers.*
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsGitController)
@Stepwise
class DevopsGitControllerSpec extends Specification {

    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    private AppServiceMapper applicationMapper
    @Autowired
    private DevopsGitService devopsGitService
    @Autowired
    private AgileServiceClientOperator agileServiceClientOperator
    @Autowired
    private DevopsMergeRequestMapper devopsMergeRequestMapper
    @Autowired
    private UserAttrMapper userAttrMapper
    @Autowired
    private DevopsBranchService devopsBranchRepository

    @Autowired
    private IamService iamService
    @Autowired
    private UserAttrService userAttrService
    @Autowired
    private BaseServiceClientOperator baseServiceClientOperator

    @Autowired
    private AppServiceService applicationService
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator
    @Autowired
    private DevopsBranchService devopsBranchService;

    @Autowired
    private DevopsBranchMapper devopsBranchMapper
    GitlabServiceClient gitlabServiceClient = Mockito.mock(GitlabServiceClient.class)
    AgileServiceClient agileServiceClient = Mockito.mock(AgileServiceClient.class)
    BaseServiceClient baseServiceClient = Mockito.mock(BaseServiceClient)

    @Shared
    AppServiceDTO applicationDO = new AppServiceDTO()



    def cleanup(){
        applicationMapper.delete(null)
    }
    def setup() {
        applicationDO.setId(1L)
        applicationDO.setAppId(1L)
        applicationDO.setCode("test")
        applicationDO.setName("test")
        applicationDO.setGitlabProjectId(1)
        applicationMapper.insert(applicationDO)
        DependencyInjectUtil.setAttribute(iamService, "baseServiceClient", baseServiceClient)
        DependencyInjectUtil.setAttribute(gitlabServiceClientOperator, "gitlabServiceClient", gitlabServiceClient)
        DependencyInjectUtil.setAttribute(applicationService, "baseServiceClientOperator", baseServiceClientOperator)
        DependencyInjectUtil.setAttribute(baseServiceClientOperator, "baseServiceClient", baseServiceClient)
        DependencyInjectUtil.setAttribute(agileServiceClientOperator, "agileServiceClient", agileServiceClient)


        ProjectDTO projectDO = new ProjectDTO()
        projectDO.setName("pro")
        projectDO.setOrganizationId(1L)
        ResponseEntity<ProjectDTO> responseEntity = new ResponseEntity<>(projectDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(baseServiceClient).queryIamProject(1L)
        OrganizationDTO organizationDO = new OrganizationDTO()
        organizationDO.setId(1L)
        organizationDO.setCode("testOrganization")
        ResponseEntity<OrganizationDTO> responseEntity1 = new ResponseEntity<>(organizationDO, HttpStatus.OK)
        Mockito.doReturn(responseEntity1).when(baseServiceClient).queryOrganizationById(1L)

        IamUserDTO userDO = new IamUserDTO()
        userDO.setLoginName("test")
        userDO.setId(1L)
        userDO.setImageUrl("imageURL")
        ResponseEntity<IamUserDTO> responseEntity2 = new ResponseEntity<>(userDO, HttpStatus.OK)
        Mockito.when(baseServiceClient.queryByLoginName(anyString())).thenReturn(responseEntity2)

        List<IamUserDTO> userDOList = new ArrayList<>()
        userDOList.add(userDO)
        ResponseEntity<List<IamUserDTO>> responseEntity3 = new ResponseEntity<>(userDOList, HttpStatus.OK)
        Mockito.when(baseServiceClient.listUsersByIds(any(Long[].class))).thenReturn(responseEntity3)

        PageInfo<IamUserDTO> page = new PageInfo<>(userDOList)
        ResponseEntity<PageInfo<IamUserDTO>> responseEntityPage = new ResponseEntity<>(page, HttpStatus.OK)
        Mockito.when(baseServiceClient.listUsersByEmail(anyLong(), anyInt(), anyInt(), anyString())).thenReturn(responseEntityPage)

        TagDTO tagDO = new TagDTO()
        tagDO.setName("testTag")
        CommitDTO commitDO = new CommitDTO()
        commitDO.setId("DOcommitId")
        commitDO.setMessage("message")
        commitDO.setCommittedDate(new Date(2018, 11, 9, 0, 0, 0))
        commitDO.setAuthorName("testAuthorName")
        tagDO.setCommit(commitDO)
        ResponseEntity<TagDTO> responseEntity4 = new ResponseEntity<>(tagDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.updateTag(anyInt(), anyString(), anyString(), anyInt())).thenReturn(responseEntity4)

        List<TagDTO> tagDOList = new ArrayList<>()
        tagDOList.add(tagDO)
        ResponseEntity<List<TagDTO>> responseEntity5 = new ResponseEntity<>(tagDOList, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.getTags(anyInt(), anyInt())).thenReturn(responseEntity5)

        BranchDTO branchDTO = new BranchDTO()
        CommitDTO commitDTO = new CommitDTO()
        commitDTO.setMessage("message")
        commitDTO.setId("EcommitId")
        commitDTO.setCommittedDate(new Date(2018, 11, 9, 0, 0, 0))
        commitDTO.setAuthorName("testAuthorName")
        branchDTO.setCommit(commitDTO)
        ResponseEntity<BranchDTO> responseEntity6 = new ResponseEntity<>(branchDTO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.createBranch(anyInt(), anyString(), anyString(), anyInt())).thenReturn(responseEntity6)

        List<TagDTO> list = new ArrayList<>()
        TagDTO tagDTO = new TagDTO()
        CommitDTO commitDTO1 = new CommitDTO()
        commitDTO1.setId("test")
        commitDTO1.setAuthorName("test")
        tagDTO.setCommit(commitDO)
        tagDTO.setName("hello")
        tagDTO.setMessage("hello")
        list.add(tagDO)

        PageInfo<RoleVO> dTOPageInfo = new PageInfo<>(list)
        ResponseEntity<PageInfo<TagDTO>> responseEntity7 = new ResponseEntity<>(dTOPageInfo, HttpStatus.OK)
        PowerMockito.when(gitlabServiceClient.getPageTags(anyInt(), anyInt(), anyInt(), anyInt())).thenReturn(responseEntity7)

        MemberDTO memberDO = new MemberDTO()
        memberDO.setAccessLevel(AccessLevel.OWNER.toValue())
        ResponseEntity<MemberDTO> responseEntity8 = new ResponseEntity<>(memberDO, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.getProjectMember(anyInt(), anyInt())).thenReturn(responseEntity8)


    }

    def "GetUrl"() {
        when: '获取工程下地址'
        def url = restTemplate.getForObject("/v1/projects/1/app_service/1/git/url", String.class)

        then: '校验返回结果'
        url != ""
    }

    def "CreateTag"() {
        given: 'mock gitlab创建tag'
        UserAttrDTO userAttrE = new UserAttrDTO()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        Mockito.doReturn(null).when(gitlabServiceClient).createTag(null, null, null, null, null, null)

        when: '创建标签'
        restTemplate.postForEntity("/v1/projects/1/app_service/1/git/tags?tag=test&ref=test&message=test", "test", Object.class)

        then: '校验'
        userAttrService.baseQueryById(_ as Long) >> userAttrE
    }

    def "UpdateTagRelease"() {
        given: 'mock 更新tag'
        UserAttrDTO userAttrE = new UserAttrDTO()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        ResponseEntity<TagDTO> responseEntity = new ResponseEntity<>(new TagDTO(), HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(gitlabServiceClient).updateTag(1, "test", "test", 1)

        when: '更新标签'
        restTemplate.put("/v1/projects/1/app_service/1/git/tags?tag=test", "test", Object.class)

        then: '校验'
        userAttrService.baseQueryById(_ as Long) >> userAttrE
    }

    def "GetTagByPage"() {
        given:
        UserAttrDTO userAttrE = new UserAttrDTO()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)

        def rootUrl = "/v1/projects/{project_id}/app_service/{app_service_id}/git"
        def url = rootUrl + "page_tags_by_options?page={page}&size={size}"
        Map<String, Object> map = new HashMap<>()
        map.put("page", 1)
        map.put("size", 10)
        map.put("project_id", 1)
        map.put("app_service_id", 1)
        String params = "{\"searchParam\": {},\"params\": []}"
        when: '获取标签分页列表'
        def page = restTemplate.postForObject(url, params, PageInfo.class, map)

        then: '校验返回值'
        page != null
        //toDO
    }

    def "GetTagList"() {
        given:
        UserAttrDTO userAttrE = new UserAttrDTO()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        List<TagDTO> tagDOS = new ArrayList<>()
        TagDTO tagDO = new TagDTO()
        CommitDTO commitDO = new CommitDTO()
        commitDO.setId("test")
        commitDO.setAuthorName("test")
        tagDO.setCommit(commitDO)
        tagDOS.add(tagDO)
        ResponseEntity<List<TagDTO>> tagResponseEntity = new ResponseEntity<>(tagDOS, HttpStatus.OK)
        PowerMockito.when(gitlabServiceClientOperator.listTag(1, 1)).thenReturn(tagResponseEntity)
        userAttrService.baseQueryById(_ as Long) >> userAttrE
        when: '获取标签列表'
        def tags = restTemplate.getForObject("/v1/projects/1/app_service/1/git/list_tags", List.class)

        then: '校验返回值'
        tags.size() == 1
    }

    def "CheckTag"() {
        given: 'mock gitlab获取tag'
        UserAttrDTO userAttrE = new UserAttrDTO()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        List<TagDTO> tagDOS = new ArrayList<>()
        TagDTO tagDO = new TagDTO()
        CommitDTO commitDO = new CommitDTO()
        commitDO.setId("test")
        commitDO.setAuthorName("test")
        tagDO.setCommit(commitDO)
        tagDOS.add(tagDO)
        ResponseEntity<List<TagDTO>> tagResponseEntity = new ResponseEntity<>(tagDOS, HttpStatus.OK)
        PowerMockito.when(gitlabServiceClientOperator.listTag(1, 1)).thenReturn(tagResponseEntity)
        userAttrService.baseQueryById(_ as Long) >> userAttrE

        when: '获取标签列表'
        def exist = restTemplate.getForObject("/v1/projects/1/app_service/1/git/check_tag?tag_name=test", Boolean.class)

        then:
        exist
    }

    def "DeleteTag"() {
        given: 'mock gitlab删除tag'
        UserAttrDTO userAttrE = new UserAttrDTO()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        ResponseEntity responseEntity=new ResponseEntity(HttpStatus.OK)
        Mockito.doReturn(responseEntity).when(gitlabServiceClient).deleteTag(anyInt(), anyString(),anyInt())

        when: '检查标签'
        restTemplate.delete("/v1/projects/1/app_service/1/git/tags?tag=test")

        then: '返回值'
        userAttrService.baseQueryById(_ as Long) >> userAttrE
    }

    def "CreateBranch"() {
        given: 'mock gitlab创建分支'
        List<BranchDTO> branchDTOS = new ArrayList<>();
        BranchDTO branchDTO = new BranchDTO();
        CommitDTO commitDO = new CommitDTO()
        commitDO.setId("test")
        commitDO.setAuthorName("test")
        branchDTO.setName("test")
        branchDTO.setCommit(commitDO)
        branchDTO.setDevelopersCanMerge(false)
        UserAttrDTO userAttrE = new UserAttrDTO()
        branchDTOS.add(branchDTO)
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        DevopsBranchVO devopsBranchDTO = new DevopsBranchVO()
        devopsBranchDTO.setAppServiceName("test")
        devopsBranchDTO.setBranchName("test1")
        devopsBranchDTO.setIssueId(1L)
        devopsBranchDTO.setAppServiceId(1L)
        devopsBranchDTO.setOriginBranch("test")
        List<BranchDTO> branchDOList = new ArrayList<>()
        BranchDTO branchDO = new BranchDTO()
        branchDO.setName("test")
        branchDO.setCommit(commitDO)
        branchDO.setDevelopersCanMerge(true)
        branchDOList.add(branchDO)
        ResponseEntity branchResponse = new ResponseEntity(branchDOList, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.listBranch(anyInt(), anyInt())).thenReturn(branchResponse)
        userAttrService.baseQueryById(_ as Long) >> userAttrE

        when: '创建分支'
        def entry = restTemplate.postForObject("/v1/projects/1/app_service/1/git/branch", devopsBranchDTO, Object.class)

        then: '校验返回值'
        devopsBranchMapper.selectAll().size() > 0
    }

    def "ListByAppId"() {
        given: 'mock gitlab查询issue'
        UserAttrDTO userAttrE = new UserAttrDTO()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        IssueDTO issue = new IssueDTO()
        ResponseEntity<IssueDTO> issueResponseEntity = new ResponseEntity<>(issue, HttpStatus.OK)
        IamUserDTO userE = new IamUserDTO()
        userE.setLoginName("test")
        userE.setId(1L)
        userE.setRealName("test")
        userE.setImageUrl("test")
        Mockito.when(agileServiceClient.queryIssue(anyLong(), anyLong(), anyLong())).thenReturn(issueResponseEntity)

        and: '准备数据'
        List<RoleVO> roleDTOList = new ArrayList<>()
        RoleVO roleDTO = new RoleVO()
        roleDTO.setCode("role/project/default/project-owner")
        roleDTOList.add(roleDTO)
        List<ProjectWithRoleVO> projectWithRoleDTOList = new ArrayList<>()
        ProjectWithRoleVO projectWithRoleDTO = new ProjectWithRoleVO()
        projectWithRoleDTO.setName("pro")
        projectWithRoleDTO.setRoles(roleDTOList)
        projectWithRoleDTOList.add(projectWithRoleDTO)
        PageInfo<ProjectWithRoleVO> projectWithRoleDTOPage = new PageInfo<>(projectWithRoleDTOList)
        ResponseEntity<PageInfo<ProjectWithRoleVO>> pageResponseEntity = new ResponseEntity<>(projectWithRoleDTOPage, HttpStatus.OK)
        Mockito.doReturn(pageResponseEntity).when(baseServiceClient).listProjectWithRole(anyLong(), anyInt(), anyInt())

        when: '获取工程下所有分支名'
        def branches = restTemplate.postForObject("/v1/projects/1/app_service/1/git/page_branch_by_options?page=0&size=10", null, PageInfo.class)


        then: '校验返回值'
        branches.getTotal() == 1
    }

    def "QueryByAppId"() {
        when: '查询单个分支'
        def devopsBranch = restTemplate.getForObject("/v1/projects/1/app_service/1/git/branch?branch_name=test", DevopsBranchVO.class)

        then: '校验返回值'
        devopsBranch != null
    }

    def "Update"() {
        given: '初始化branchDTO类'
        DevopsBranchVO devopsBranchDTO = new DevopsBranchVO()
        devopsBranchDTO.setBranchName("test")
        devopsBranchDTO.setIssueId(1L)

        when: '更新分支关联的问题'
        restTemplate.put("/v1/projects/1/app_service/1/git/update_branch_issue", devopsBranchDTO)

        then: '校验返回值'
        devopsBranchMapper.selectByPrimaryKey(devopsBranchMapper.selectAll().get(0).getId()).getIssueId() == 1L
    }

    def "Delete"() {
        given: 'mock gitlab删除分支'
        UserAttrDTO userAttrE = new UserAttrDTO()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        ResponseEntity responseEntity = new ResponseEntity(new Object(), HttpStatus.OK)
        Mockito.when(gitlabServiceClient.deleteBranch(anyInt(), anyString(), anyInt())).thenReturn(responseEntity)

        and: 'mock gitlab查询分支'
        List<BranchDTO> branchDOList = new ArrayList<>()
        BranchDTO branchDO = new BranchDTO()
        branchDO.setName("test")
        branchDOList.add(branchDO)
        ResponseEntity branchResponse = new ResponseEntity(branchDOList, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.listBranch(anyInt(), anyInt())).thenReturn(branchResponse)

        when: '删除分支'
        restTemplate.delete("/v1/projects/{project_id}/app_service/{application_id}/git/branch?branch_name=test", 1L, 1L)

        then: '校验返回值'
        devopsBranchMapper.selectByPrimaryKey(devopsBranchMapper.selectAll().get(0).getId())
    }

    def "GetMergeRequestList"() {
        given: 'mock 查询commits'
        IamUserDTO userE = new IamUserDTO()
        userE.setLoginName("test")
        userE.setId(1L)
        userE.setRealName("test")
        userE.setImageUrl("test")
        UserAttrDTO userAttrE = new UserAttrDTO()
        userAttrE.setIamUserId(1L)
        userAttrE.setGitlabUserId(1L)
        DevopsMergeRequestDTO devopsMergeRequestDO = new DevopsMergeRequestDTO()
        devopsMergeRequestDO.setId(3L)
        devopsMergeRequestDO.setState("merged")
        devopsMergeRequestDO.setGitlabProjectId(1L)
        devopsMergeRequestDO.setGitlabMergeRequestId(1L)
        devopsMergeRequestDO.setAuthorId(1L)
        devopsMergeRequestDO.setAssigneeId(1L)
        devopsMergeRequestMapper.insert(devopsMergeRequestDO)
        DevopsMergeRequestDTO devopsMergeRequestDO1 = new DevopsMergeRequestDTO()
        devopsMergeRequestDO1.setId(2L)
        devopsMergeRequestDO1.setState("closed")
        devopsMergeRequestDO1.setGitlabProjectId(2L)
        devopsMergeRequestDO1.setGitlabMergeRequestId(2L)
        devopsMergeRequestDO1.setAuthorId(2L)
        devopsMergeRequestDO1.setAssigneeId(2L)
        devopsMergeRequestMapper.insert(devopsMergeRequestDO1)
        List<CommitDTO> commitDOList = new ArrayList<>()
        CommitDTO commitDO = new CommitDTO()
        commitDOList.add(commitDO)
        ResponseEntity<List<CommitDTO>> responseEntity = new ResponseEntity<>(commitDOList, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.listCommits(anyInt(), anyInt(), anyInt())).thenReturn(responseEntity).thenReturn(responseEntity).thenReturn(responseEntity)

        when: '查看所有合并请求'
        def mergeRequest = restTemplate.getForObject("/v1/projects/1/app_service/1/git/list_merge_request?page=0&size=10", Map.class)

        then: '校验返回值'
        !mergeRequest.isEmpty()
    }

    def "CheckName"() {
        given: 'mock gitlab查询分支'
        List<BranchDTO> branchDOList = new ArrayList<>()
        BranchDTO branchDO = new BranchDTO()
        branchDO.setName("test")
        branchDOList.add(branchDO)
        ResponseEntity branchResponse = new ResponseEntity(branchDOList, HttpStatus.OK)
        Mockito.when(gitlabServiceClient.listBranch(anyInt(), anyInt())).thenReturn(branchResponse)

        when: '校验实例名唯一性'
        def exception = restTemplate.getForEntity("/v1/projects/{project_id}/app_service/{application_id}/git/check_branch_name?branch_name=uniqueName", ExceptionResponse.class, 1L, 1L)

        then: '名字不存在不抛出异常'
        exception.statusCode.is2xxSuccessful()
        notThrown(CommonException)

        // 删除app
        List<AppServiceDTO> list = applicationMapper.selectAll()
        if (list != null && !list.isEmpty()) {
            for (AppServiceDTO e : list) {
                applicationMapper.delete(e)
            }
        }
        // 删除branch
        List<DevopsBranchDTO> list1 = devopsBranchMapper.selectAll()
        if (list1 != null && !list1.isEmpty()) {
            for (DevopsBranchDTO e : list1) {
                devopsBranchMapper.delete(e)
            }
        }
        // 删除mergeRequest
        List<DevopsMergeRequestDTO> list2 = devopsMergeRequestMapper.selectAll()
        if (list2 != null && !list2.isEmpty()) {
            for (DevopsMergeRequestDTO e : list2) {
                devopsMergeRequestMapper.delete(e)
            }
        }
    }
}
