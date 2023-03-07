package io.choerodon.devops.app.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.devops.app.service.PipelinePersonalTokenService;
import io.choerodon.devops.infra.dto.PipelinePersonalTokenDTO;
import io.choerodon.devops.infra.mapper.PipelinePersonalTokenMapper;
import io.choerodon.devops.infra.util.GenerateUUID;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 流水线个人token表(PipelinePersonalToken)应用服务
 *
 * @author hao.wang@zknow.com
 * @since 2022-12-05 11:20:48
 */
@Service
public class PipelinePersonalTokenServiceImpl implements PipelinePersonalTokenService {


    private static final String DEVOPS_SAVE_PERSONAL_TOKEN_FAILED = "devops.save.personal.token.failed";
    private static final String DEVOPS_PERSONAL_TOKEN_INVALID = "devops.personal.token.invalid";

    @Autowired
    private PipelinePersonalTokenMapper pipelinePersonalTokenMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String queryOrCreatePersonalToken(Long projectId) {
        Long userId = DetailsHelper.getUserDetails().getUserId();
        PipelinePersonalTokenDTO pipelinePersonalTokenDTO = new PipelinePersonalTokenDTO();
        pipelinePersonalTokenDTO.setUserId(userId);
        PipelinePersonalTokenDTO pipelinePersonalTokenDTO1 = pipelinePersonalTokenMapper.selectOne(pipelinePersonalTokenDTO);
        if (pipelinePersonalTokenDTO1 == null) {
            pipelinePersonalTokenDTO1 = new PipelinePersonalTokenDTO();
            pipelinePersonalTokenDTO1.setUserId(userId);
            pipelinePersonalTokenDTO1.setToken(GenerateUUID.generateUUID());
            MapperUtil.resultJudgedInsertSelective(pipelinePersonalTokenMapper, pipelinePersonalTokenDTO1, DEVOPS_SAVE_PERSONAL_TOKEN_FAILED);
        }

        return pipelinePersonalTokenDTO1.getToken();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String resetPersonalToken(Long projectId) {
        Long userId = DetailsHelper.getUserDetails().getUserId();
        PipelinePersonalTokenDTO pipelinePersonalTokenDTO = new PipelinePersonalTokenDTO();
        pipelinePersonalTokenDTO.setUserId(userId);
        PipelinePersonalTokenDTO pipelinePersonalTokenDTO1 = pipelinePersonalTokenMapper.selectOne(pipelinePersonalTokenDTO);
        if (pipelinePersonalTokenDTO1 == null) {
            pipelinePersonalTokenDTO1 = new PipelinePersonalTokenDTO();
            pipelinePersonalTokenDTO1.setUserId(userId);
            pipelinePersonalTokenDTO1.setToken(GenerateUUID.generateUUID());
            MapperUtil.resultJudgedInsertSelective(pipelinePersonalTokenMapper, pipelinePersonalTokenDTO1, DEVOPS_SAVE_PERSONAL_TOKEN_FAILED);
        } else {
            pipelinePersonalTokenDTO1.setToken(GenerateUUID.generateUUID());
            MapperUtil.resultJudgedUpdateByPrimaryKeySelective(pipelinePersonalTokenMapper, pipelinePersonalTokenDTO1, DEVOPS_SAVE_PERSONAL_TOKEN_FAILED);
        }

        return pipelinePersonalTokenDTO1.getToken();
    }

    @Override
    public PipelinePersonalTokenDTO queryByToken(String token) {
        PipelinePersonalTokenDTO pipelinePersonalTokenDTO = new PipelinePersonalTokenDTO();
        pipelinePersonalTokenDTO.setToken(token);
        return pipelinePersonalTokenMapper.selectOne(pipelinePersonalTokenDTO);
    }

    @Override
    public PipelinePersonalTokenDTO queryByTokenOrThrowE(String token) {
        PipelinePersonalTokenDTO pipelinePersonalTokenDTO = queryByToken(token);
        if (pipelinePersonalTokenDTO == null) {
            throw new CommonException(DEVOPS_PERSONAL_TOKEN_INVALID);
        }
        return pipelinePersonalTokenDTO;
    }

}

