package io.choerodon.devops.infra.feign;

import java.util.List;
import java.util.Set;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import org.hzero.common.HZeroService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.ExternalTenantVO;
import io.choerodon.devops.api.vo.OrgAdministratorVO;
import io.choerodon.devops.infra.dto.iam.ClientVO;
import io.choerodon.devops.infra.dto.iam.IamUserDTO;
import io.choerodon.devops.infra.dto.iam.Tenant;
import io.choerodon.devops.infra.dto.iam.UserCountVO;
import io.choerodon.devops.infra.feign.fallback.IamServiceClientFallback;

/**
 * Created by younger on 2018/3/29.
 */

@FeignClient(value =  HZeroService.Iam.NAME, fallback = IamServiceClientFallback.class)
public interface IamServiceClient {
    /**
     * @param organizationId 组织id
     * @param withMoreInfo   获取更详细的组织配置信息以及用户信息
     * @return
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organizationId}")
    ResponseEntity<Tenant> queryOrganizationById(@PathVariable("organizationId") Long organizationId,
                                                 @RequestParam(value = "with_more_info") Boolean withMoreInfo);


    /**
     * 根据id集合查询组织
     *
     * @param ids id集合，去重
     * @return 组织集合
     */
    @PostMapping(value = "/choerodon/v1/organizations/ids")
    ResponseEntity<List<Tenant>> queryOrgByIds(@RequestBody Set<Long> ids);

    @GetMapping(value = "/choerodon/v1/users")
    ResponseEntity<IamUserDTO> queryByLoginName(@RequestParam("login_name") String loginName);

    @GetMapping(value = "/choerodon/v1/users/{id}/info")
    ResponseEntity<IamUserDTO> queryById(@PathVariable("id") Long id);


    @PostMapping(value = "/choerodon/v1/users/ids")
    ResponseEntity<String> listUsersByIds(@RequestBody Long[] ids, @RequestParam(value = "only_enabled") Boolean onlyEnabled);


    /**
     * 组织下创client
     *
     * @param clientVO clientVO
     * @return 分配结果
     */
    @PostMapping(value = "/choerodon/v1/organizations/{organization_id}/clients")
    ResponseEntity<ClientVO> createClient(@PathVariable("organization_id") Long organizationId,
                                          @RequestBody @Valid ClientVO clientVO);


    /**
     * 组织下删除client
     *
     * @param organizationId 组织id
     * @param clientId       clientId
     * @return 分配结果
     */
    @DeleteMapping(value = "/choerodon/v1/organizations/{organization_id}/clients/{client_id}")
    ResponseEntity deleteClient(@PathVariable("organization_id") Long organizationId, @PathVariable("client_id") Long clientId);

    /**
     * 根据集群Id和组织Id查询client
     *
     * @param organizationId
     * @param clientId
     * @return
     */
    @GetMapping(value = "/v1/{organization_id}/clients/{client_id}")
    ResponseEntity<ClientVO> queryClientBySourceId(@PathVariable("organization_id") Long organizationId, @PathVariable("client_id") Long clientId);


    /**
     * 判断用户是否是平台root用户
     *
     * @param id
     * @return
     */
    @GetMapping(value = "/choerodon/v1/users/{id}/check_is_root")
    ResponseEntity<Boolean> checkIsRoot(@PathVariable("id") Long id);


    /**
     * 判段用户是否是组织root用户
     *
     * @param organizationId
     * @param userId
     * @return
     */
    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/users/{user_id}/check_is_root")
    ResponseEntity<Boolean> checkIsOrgRoot(@PathVariable(name = "organization_id") Long organizationId,
                                           @PathVariable(name = "user_id") Long userId);

    @GetMapping(value = "/choerodon/v1/organizations/{organization_id}/org_administrator")
    ResponseEntity<Page<OrgAdministratorVO>> listOrgAdministrator(
            @PathVariable("organization_id") Long organizationId,
            @RequestParam("size") Integer size);

    /**
     * 根据名称查询客户端
     */
    @GetMapping("/choerodon/v1/organizations/{organization_id}/clients/query_by_name")
    ResponseEntity<ClientVO> queryClientByName(@PathVariable("organization_id") Long organizationId,
                                               @RequestParam(value = "client_name") String clientName);

    /**
     * 查询所有root
     */
    @GetMapping("/choerodon/v1/users/root")
    ResponseEntity<List<IamUserDTO>> queryRoot();

    /**
     * @return {@link UserCountVO}
     */
    @ApiOperation(value = "查询平台中所有用户的数量")
    @GetMapping(value = "/choerodon/v1/users/all_user_count")
    ResponseEntity<String> countAllUsers();

    @GetMapping("/choerodon/v1/organizations/external/tenants")
    ResponseEntity<ExternalTenantVO> queryTenantByIdWithExternalInfo(@RequestParam("organization_id") Long organizationId);

    @ApiOperation(value = "查询用户是不是平台管理员(供市场使用，包含平台管理员，平台维护者，root)")
    @GetMapping(value = "/choerodon/v1/users/self/is_site_administrator")
    ResponseEntity<Boolean> platformAdministratorOrAuditor(@RequestParam(value = "user_id") Long userId);


    @PostMapping(value = "/choerodon/v1/users/emails")
    ResponseEntity<String> listUsersByEmails(@RequestBody List<String> emails);

}
