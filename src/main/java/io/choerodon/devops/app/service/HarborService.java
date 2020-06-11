package io.choerodon.devops.app.service;

import java.util.List;

import io.choerodon.devops.api.vo.harbor.HarborCustomRepoVO;
import io.choerodon.devops.app.eventhandler.payload.HarborPayload;
import io.choerodon.devops.infra.dto.harbor.User;
import io.choerodon.devops.infra.dto.iam.ProjectDTO;
import io.choerodon.devops.infra.feign.HarborClient;

/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/8
 * Time: 10:30
 * Description:
 */
public interface HarborService {

    //todo
    void createHarborForProject(HarborPayload harborPayload);

    void createHarbor(HarborClient harborClient, Long projectId, String projectCode, Boolean createUser, Boolean harborPrivate);

    void createHarborUserByClient(HarborPayload harborPayload, User user, ProjectDTO projectDTO, List<Integer> roles);

    User convertHarborUser(ProjectDTO projectDTO, Boolean isPush, String username);

    List<HarborCustomRepoVO> listAllCustomRepoByProject(Long projectId);
}
