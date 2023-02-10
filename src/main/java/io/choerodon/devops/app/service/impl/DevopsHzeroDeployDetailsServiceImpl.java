//package io.choerodon.devops.app.service.impl;
//
//import java.util.Date;
//import java.util.List;
//import java.util.Objects;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.Assert;
//
//import io.choerodon.devops.app.service.DevopsDeployRecordService;
//import io.choerodon.devops.app.service.DevopsHzeroDeployDetailsService;
//import io.choerodon.devops.infra.constant.ResourceCheckConstant;
//import io.choerodon.devops.infra.dto.DevopsDeployRecordDTO;
//import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployDetailsDTO;
//import io.choerodon.devops.infra.enums.HzeroDeployDetailsStatusEnum;
//import io.choerodon.devops.infra.enums.deploy.DeployResultEnum;
//import io.choerodon.devops.infra.feign.operator.WorkFlowServiceOperator;
//import io.choerodon.devops.infra.mapper.DevopsHzeroDeployDetailsMapper;
//import io.choerodon.devops.infra.util.MapperUtil;
//
///**
// * 〈功能简述〉
// * 〈〉
// *
// * @author wanghao
// * @since 2021/7/28 10:04
// */
//@Service
//public class DevopsHzeroDeployDetailsServiceImpl implements DevopsHzeroDeployDetailsService {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(DevopsHzeroDeployDetailsServiceImpl.class);
//
//    private static final String ERROR_SAVE_DEPLOY_DETAILS_FAILED = "devops.save.deploy.details.failed";
//    private static final String ERROR_UPDATE_DEPLOY_DETAILS_FAILED = "devops.update.deploy.details.failed";
//
//    @Autowired
//    private DevopsHzeroDeployDetailsMapper devopsHzeroDeployDetailsMapper;
//    @Autowired
//    @Lazy
//    private DevopsDeployRecordService devopsDeployRecordService;
//    @Autowired
//    private WorkFlowServiceOperator workFlowServiceOperator;
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public DevopsHzeroDeployDetailsDTO baseSave(DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO) {
//        MapperUtil.resultJudgedInsert(devopsHzeroDeployDetailsMapper, devopsHzeroDeployDetailsDTO, ERROR_SAVE_DEPLOY_DETAILS_FAILED);
//        return devopsHzeroDeployDetailsMapper.selectByPrimaryKey(devopsHzeroDeployDetailsDTO.getId());
//    }
//
//    @Override
//    public DevopsHzeroDeployDetailsDTO baseQueryById(Long detailsRecordId) {
//        return devopsHzeroDeployDetailsMapper.selectByPrimaryKey(detailsRecordId);
//    }
//
//    @Override
//    @Transactional
//    public void updateStatusById(Long id, HzeroDeployDetailsStatusEnum status) {
//        DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO = devopsHzeroDeployDetailsMapper.selectByPrimaryKey(id);
//        devopsHzeroDeployDetailsDTO.setStatus(status.value());
//        if ((Objects.equals(status.value(), HzeroDeployDetailsStatusEnum.SUCCESS.value()) || Objects.equals(status.value(), HzeroDeployDetailsStatusEnum.FAILED.value())
//                || Objects.equals(status.value(), HzeroDeployDetailsStatusEnum.CANCELED.value())) && devopsHzeroDeployDetailsDTO.getStartTime() != null) {
//            if (devopsHzeroDeployDetailsDTO.getEndTime() == null) {
//                devopsHzeroDeployDetailsDTO.setEndTime(new Date());
//            }
//        }
//        MapperUtil.resultJudgedUpdateByPrimaryKeySelective(devopsHzeroDeployDetailsMapper, devopsHzeroDeployDetailsDTO, ERROR_UPDATE_DEPLOY_DETAILS_FAILED);
//    }
//
//    @Override
//    public DevopsHzeroDeployDetailsDTO baseQueryByAppId(Long appId) {
//        Assert.notNull(appId, ResourceCheckConstant.DEVOPS_APP_ID_IS_NULL);
//        DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO = new DevopsHzeroDeployDetailsDTO();
//        devopsHzeroDeployDetailsDTO.setAppId(appId);
//        return devopsHzeroDeployDetailsMapper.selectOne(devopsHzeroDeployDetailsDTO);
//    }
//
//    @Override
//    public List<DevopsHzeroDeployDetailsDTO> listNotSuccessRecordId(Long recordId) {
//        return devopsHzeroDeployDetailsMapper.listNotSuccessRecordId(recordId);
//    }
//
//    @Override
//    public List<DevopsHzeroDeployDetailsDTO> listByDeployRecordId(Long deployRecordId) {
//        Assert.notNull(deployRecordId, ResourceCheckConstant.DEVOPS_DEPLOY_RECORD_ID_IS_NULL);
//        DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO = new DevopsHzeroDeployDetailsDTO();
//        devopsHzeroDeployDetailsDTO.setDeployRecordId(deployRecordId);
//        return devopsHzeroDeployDetailsMapper.select(devopsHzeroDeployDetailsDTO);
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void baseUpdate(DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO) {
//        MapperUtil.resultJudgedUpdateByPrimaryKey(devopsHzeroDeployDetailsMapper, devopsHzeroDeployDetailsDTO, ERROR_UPDATE_DEPLOY_DETAILS_FAILED);
//    }
//
//    @Override
//    public Boolean completed(Long deployRecordId) {
//        DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO = new DevopsHzeroDeployDetailsDTO();
//        devopsHzeroDeployDetailsDTO.setDeployRecordId(deployRecordId);
//
//        List<DevopsHzeroDeployDetailsDTO> devopsHzeroDeployDetailsDTOS = devopsHzeroDeployDetailsMapper.select(devopsHzeroDeployDetailsDTO);
//        return devopsHzeroDeployDetailsDTOS.stream().allMatch(v -> HzeroDeployDetailsStatusEnum.SUCCESS.value().equals(v.getStatus()));
//    }
//
//    @Override
//    public List<DevopsHzeroDeployDetailsDTO> listDeployingByDate(String date) {
//        return devopsHzeroDeployDetailsMapper.listDeployingByDate(date);
//    }
//
//    @Override
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void updateStatusToFailed(DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO) {
//        DevopsDeployRecordDTO devopsDeployRecordDTO = devopsDeployRecordService.baseQueryById(devopsHzeroDeployDetailsDTO.getDeployRecordId());
//
//        // 1. 更新记录状态为失败
//        updateStatusById(devopsHzeroDeployDetailsDTO.getId(), HzeroDeployDetailsStatusEnum.FAILED);
//        // 2. 更新部署记录状态为失败
//        devopsDeployRecordService.updateResultById(devopsDeployRecordDTO.getId(), DeployResultEnum.FAILED);
//
//        // 3. 停止工作流
//        try {
//            workFlowServiceOperator.stopInstance(devopsDeployRecordDTO.getProjectId(), devopsDeployRecordDTO.getBusinessKey());
//        } catch (Exception e) {
//            LOGGER.error(">>>>>>>>>>>>>>>Stop workflow instance failed, deployRecordId: {}<<<<<<<<<<<<<", devopsHzeroDeployDetailsDTO.getDeployRecordId());
//        }
//    }
//
//}
