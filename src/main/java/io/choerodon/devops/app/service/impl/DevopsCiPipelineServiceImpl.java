package io.choerodon.devops.app.service.impl;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsCiPipelineVO;
import io.choerodon.devops.api.vo.DevopsCiStageVO;
import io.choerodon.devops.app.service.DevopsCiJobService;
import io.choerodon.devops.app.service.DevopsCiPipelineService;
import io.choerodon.devops.app.service.DevopsCiStageService;
import io.choerodon.devops.infra.dto.DevopsCiJobDTO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineDTO;
import io.choerodon.devops.infra.dto.DevopsCiStageDTO;
import io.choerodon.devops.infra.mapper.DevopsCiPipelineMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/2 18:00
 */
@Service
public class DevopsCiPipelineServiceImpl implements DevopsCiPipelineService{

    private static final String CREATE_PIPELINE_FAILED = "create.pipeline.failed";
    private static final String UPDATE_PIPELINE_FAILED = "update.pipeline.failed";

    private DevopsCiPipelineMapper devopsCiPipelineMapper;

    private DevopsCiStageService devopsCiStageService;

    private DevopsCiJobService devopsCiJobService;

    public DevopsCiPipelineServiceImpl(DevopsCiPipelineMapper devopsCiPipelineMapper, DevopsCiStageService devopsCiStageService, DevopsCiJobService devopsCiJobService) {
        this.devopsCiPipelineMapper = devopsCiPipelineMapper;
        this.devopsCiStageService = devopsCiStageService;
        this.devopsCiJobService = devopsCiJobService;
    }

    @Override
    @Transactional
    public DevopsCiPipelineDTO create(Long projectId, DevopsCiPipelineVO devopsCiPipelineVO) {
        devopsCiPipelineVO.setProjectId(projectId);
        DevopsCiPipelineDTO devopsCiPipelineDTO = ConvertUtils.convertObject(devopsCiPipelineVO, DevopsCiPipelineDTO.class);
        if (devopsCiPipelineMapper.insertSelective(devopsCiPipelineDTO) != 1) {
            throw new CommonException(CREATE_PIPELINE_FAILED);
        }
        // 保存stage信息
        devopsCiPipelineVO.getStageList().forEach(devopsCiStageVO -> {
            DevopsCiStageDTO devopsCiStageDTO = ConvertUtils.convertObject(devopsCiStageVO, DevopsCiStageDTO.class);
            devopsCiStageDTO.setCiPipelineId(devopsCiPipelineDTO.getId());
            DevopsCiStageDTO savedDevopsCiStageDTO = devopsCiStageService.create(devopsCiStageDTO);
            // 保存job信息
            devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                devopsCiJobDTO.setCiPipelineId(devopsCiPipelineDTO.getId());
                devopsCiJobDTO.setStageId(savedDevopsCiStageDTO.getId());
                devopsCiJobService.create(devopsCiJobDTO);
            });
        });
        // TODO 保存ci配置文件

        // TODO 更新代码库.gitlab-ci.yaml文件，添加inclue指令
        return devopsCiPipelineMapper.selectByPrimaryKey(devopsCiPipelineDTO.getId());
    }

    @Override
    @Transactional
    public DevopsCiPipelineDTO update(Long projectId, Long ciPipelineId, DevopsCiPipelineVO devopsCiPipelineVO) {
        DevopsCiPipelineDTO devopsCiPipelineDTO = ConvertUtils.convertObject(devopsCiPipelineVO, DevopsCiPipelineDTO.class);
        devopsCiPipelineDTO.setId(ciPipelineId);
        if (devopsCiPipelineMapper.updateByPrimaryKeySelective(devopsCiPipelineDTO) != 1) {
            throw new CommonException(UPDATE_PIPELINE_FAILED);
        }
        // 更新stage
        // 查询数据库中原有stage列表,并和新的stage列表作比较。
        // 差集：要删除的记录
        // 交集：要更新的记录
        List<DevopsCiStageDTO> devopsCiStageDTOList = devopsCiStageService.listByPipelineId(ciPipelineId);
        Set<Long> oldStageIds = devopsCiStageDTOList.stream().map(DevopsCiStageDTO::getId).collect(Collectors.toSet());

        Set<Long> updateIds = devopsCiPipelineVO.getStageList().stream()
                .filter(devopsCiStageVO -> devopsCiStageVO.getId() != null)
                .map(DevopsCiStageVO::getId)
                .collect(Collectors.toSet());
        // 去掉要更新的记录，剩下的为要删除的记录
        oldStageIds.removeAll(updateIds);
        oldStageIds.forEach(stageId -> {
            devopsCiStageService.deleteById(stageId);
            devopsCiJobService.deleteByStageId(stageId);
        });

        devopsCiPipelineVO.getStageList().forEach(devopsCiStageVO -> {
            if (devopsCiStageVO.getId() != null) {
                // 更新
                devopsCiStageService.update(devopsCiStageVO);
                devopsCiJobService.deleteByStageId(devopsCiStageVO.getId());
                // 保存job信息
                devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                    DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                    devopsCiJobService.create(devopsCiJobDTO);
                });
            } else {
                // 新增
                DevopsCiStageDTO devopsCiStageDTO = ConvertUtils.convertObject(devopsCiStageVO, DevopsCiStageDTO.class);
                DevopsCiStageDTO savedDevopsCiStageDTO = devopsCiStageService.create(devopsCiStageDTO);
                // 保存job信息
                devopsCiStageVO.getJobList().forEach(devopsCiJobVO -> {
                    DevopsCiJobDTO devopsCiJobDTO = ConvertUtils.convertObject(devopsCiJobVO, DevopsCiJobDTO.class);
                    devopsCiJobDTO.setStageId(savedDevopsCiStageDTO.getId());
                    devopsCiJobService.create(devopsCiJobDTO);
                });
            }
        });
        // TODO 更新ci配置

        return devopsCiPipelineMapper.selectByPrimaryKey(ciPipelineId);
    }
}
