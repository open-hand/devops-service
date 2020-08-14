package io.choerodon.devops.app.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CiPipelineImageVO;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.CiPipelineImageService;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.CiPipelineImageDTO;
import io.choerodon.devops.infra.exception.DevopsCiInvalidException;
import io.choerodon.devops.infra.mapper.CiPipelineImageMapper;
import io.choerodon.devops.infra.util.ExceptionUtil;

/**
 * @author scp
 * @date 2020/7/21
 * @description
 */
@Service
public class CiPipelineImageServiceImpl implements CiPipelineImageService {
    @Autowired
    private CiPipelineImageMapper ciPipelineImageMapper;
    @Autowired
    private AppServiceService appServiceService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createOrUpdate(CiPipelineImageVO ciPipelineImageVO) {
        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(ciPipelineImageVO.getToken());
        if (appServiceDTO == null) {
            throw new DevopsCiInvalidException("error.token.invalid");
        }

        // 异常包装
        ExceptionUtil.wrapExWithCiEx(() -> {
            CiPipelineImageDTO oldCiPipelineImageDTO = queryByGitlabPipelineId(ciPipelineImageVO.getGitlabPipelineId(), ciPipelineImageVO.getJobName());
            if (oldCiPipelineImageDTO == null || oldCiPipelineImageDTO.getId() == null) {
                CiPipelineImageDTO ciPipelineImageDTO = new CiPipelineImageDTO();
                BeanUtils.copyProperties(ciPipelineImageVO, ciPipelineImageDTO);
                if (ciPipelineImageMapper.insertSelective(ciPipelineImageDTO) != 1) {
                    throw new CommonException("error.create.image.record");
                }
            } else {
                BeanUtils.copyProperties(ciPipelineImageVO, oldCiPipelineImageDTO);
                if (ciPipelineImageMapper.updateByPrimaryKey(oldCiPipelineImageDTO) != 1) {
                    throw new CommonException("error.update.image.record");
                }
            }
        });
    }

    @Override
    public CiPipelineImageDTO queryByGitlabPipelineId(Long gitlabPipelineId, String jobName) {
        CiPipelineImageDTO ciPipelineImageDTO = new CiPipelineImageDTO();
        ciPipelineImageDTO.setGitlabPipelineId(gitlabPipelineId);
        ciPipelineImageDTO.setJobName(jobName);
        return ciPipelineImageMapper.selectOne(ciPipelineImageDTO);
    }
}
