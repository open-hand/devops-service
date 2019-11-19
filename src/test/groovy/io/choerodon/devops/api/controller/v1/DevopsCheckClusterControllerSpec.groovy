package io.choerodon.devops.api.controller.v1

import io.choerodon.devops.DependencyInjectUtil
import io.choerodon.devops.IntegrationTestConfiguration
import io.choerodon.devops.api.vo.iam.ProjectWithRoleVO
import io.choerodon.devops.api.vo.iam.RoleVO
import io.choerodon.devops.app.service.DevopsClusterService
import io.choerodon.devops.infra.dto.DevopsClusterDTO
import io.choerodon.devops.infra.feign.BaseServiceClient
import io.choerodon.devops.infra.feign.operator.BaseServiceClientOperator
import io.choerodon.devops.infra.mapper.DevopsClusterMapper
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Subject

/**
 * @author zhaotianxin* @since 2019/10/28
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@Subject(DevopsCheckClusterController)
@Stepwise
class DevopsCheckClusterControllerSpec extends Specification {
    @Autowired
    private DevopsClusterService devopsClusterService;
    @Autowired
    private TestRestTemplate restTemplate
    @Autowired
    DevopsClusterMapper devopsClusterMapper
    @Shared
    List<ProjectWithRoleVO> projectWithRoleVOS = new ArrayList<>();

    BaseServiceClientOperator baseServiceClientOperator = Mockito.mock(BaseServiceClientOperator.class)
    def setup(){
        DependencyInjectUtil.setAttribute(devopsClusterService,"baseServiceClientOperator",baseServiceClientOperator)
        // 初始化集群
        DevopsClusterDTO devopsClusterDTO =new DevopsClusterDTO();
        devopsClusterDTO.setName("集群A")
        devopsClusterDTO.setProjectId(1L)
        devopsClusterMapper.insertSelective(devopsClusterDTO)
        ProjectWithRoleVO projectWithRoleVO = new ProjectWithRoleVO();
        projectWithRoleVO.setId(1L)
        projectWithRoleVO.setName("项目A")
        RoleVO roleVO = new RoleVO()
        roleVO.setId(1L)
        roleVO.setName("项目所有者")
        roleVO.setCode("role/project/default/project-owner")
        List<RoleVO> roleVOList = new ArrayList<>();
        roleVOList.add(roleVO)
        projectWithRoleVO.setRoles(roleVOList)
        List<ProjectWithRoleVO> projectWithRoleVOS = new ArrayList<>();
        projectWithRoleVOS.add(projectWithRoleVO)
        Mockito.doReturn(projectWithRoleVOS).when(baseServiceClientOperator).listProjectWithRole(1L,0,0)
    }
    def "CheckUserClusterPermission"() {
        when: '用户对集群的权限校验'
        def entity = restTemplate.getForEntity("/v1/checks/clusterCheck?cluster_id=1&user_id=1",Boolean.class)
        then: '校验返回结果'
        entity.statusCode.is2xxSuccessful()
        entity.getBody() == true
    }
}
