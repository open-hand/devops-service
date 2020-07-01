package io.choerodon.devops.app.service.impl;

import static io.choerodon.devops.app.eventhandler.constants.SagaTopicCodeConstants.IAM_CREATE_USER;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.devops.api.vo.GitlabUserVO;
import io.choerodon.devops.api.vo.iam.UserVO;
import io.choerodon.devops.app.service.SyncService;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.feign.BaseServiceClient;
import io.choerodon.devops.infra.mapper.UserAttrMapper;
import io.choerodon.devops.infra.util.CustomContextUtil;

/**
 * @author scp
 * @date 2020/7/1
 * @description
 */
@Component
public class SyncServiceImpl implements SyncService {

    @Autowired
    private BaseServiceClient baseServiceClient;
    @Autowired
    private UserAttrMapper userAttrMapper;
    @Autowired
    private TransactionalProducer producer;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void userWithOutGitlabUser() {
        ResponseEntity<List<UserVO>> roleResponseEntity = baseServiceClient.listUserByCreationDate();
        List<UserVO> userVOList = roleResponseEntity.getBody();
        CustomContextUtil.setUserContext(0L);
        if (roleResponseEntity.getStatusCode().is2xxSuccessful() && userVOList != null && userVOList.size() != 0) {
            userVOList.forEach(t -> {
                UserAttrDTO queryDTO = new UserAttrDTO();
                queryDTO.setIamUserId(t.getId());
                UserAttrDTO userAttrDTO = userAttrMapper.selectOne(queryDTO);
                if (ObjectUtils.isEmpty(userAttrDTO) || userAttrDTO.getIamUserId() == null) {
                    List<GitlabUserVO> list = new ArrayList<>();
                    GitlabUserVO gitlabUserVO = new GitlabUserVO();
                    gitlabUserVO.setEmail(t.getEmail());
                    gitlabUserVO.setName(t.getRealName());
                    gitlabUserVO.setUsername(t.getLoginName());
                    gitlabUserVO.setId(t.getId() + "");
                    list.add(gitlabUserVO);
                    String input = null;
                    try {
                        input = mapper.writeValueAsString(list);
                        producer.apply(StartSagaBuilder.newBuilder()
                                        .withSagaCode(IAM_CREATE_USER)
                                        .withJson(input)
                                        .withRefType("user")
                                        .withRefId(t.getId() + "")
                                        .withLevel(ResourceLevel.ORGANIZATION)
                                        .withSourceId(t.getOrganizationId()),
                                build -> {
                                });
                    } catch (Exception e) {
                        throw new CommonException("error.sync.user.gitlab");
                    }
                }
            });
        } else {
            throw new CommonException("error.user.list");
        }
    }
}
