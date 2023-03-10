//package io.choerodon.devops.app.service;
//
//import java.util.List;
//
//import io.choerodon.devops.infra.dto.deploy.DevopsHzeroDeployDetailsDTO;
//import io.choerodon.devops.infra.enums.HzeroDeployDetailsStatusEnum;
//
///**
// * 〈功能简述〉
// * 〈〉
// *
// * @author wanghao
// * @since 2021/7/28 10:04
// */
//public interface DevopsHzeroDeployDetailsService {
//
//    DevopsHzeroDeployDetailsDTO baseSave(DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO);
//
//    DevopsHzeroDeployDetailsDTO baseQueryById(Long detailsRecordId);
//
//    void updateStatusById(Long id, HzeroDeployDetailsStatusEnum status);
//
//    DevopsHzeroDeployDetailsDTO baseQueryByAppId(Long appId);
//
//    List<DevopsHzeroDeployDetailsDTO> listNotSuccessRecordId(Long recordId);
//
//    List<DevopsHzeroDeployDetailsDTO> listByDeployRecordId(Long deployRecordId);
//
//    void baseUpdate(DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO);
//
//    /**
//     * 判断部署任务是否执行完成
//     * @param deployRecordId
//     * @return true：所有部署任务执行完成，false:还有未完成的部署任务
//     */
//    Boolean completed(Long deployRecordId);
//
//    List<DevopsHzeroDeployDetailsDTO> listDeployingByDate(String date);
//
//    void updateStatusToFailed(DevopsHzeroDeployDetailsDTO devopsHzeroDeployDetailsDTO);
//}
