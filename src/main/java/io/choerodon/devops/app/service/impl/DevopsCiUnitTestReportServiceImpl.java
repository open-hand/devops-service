package io.choerodon.devops.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.pipeline.DevopsCiUnitTestResultVO;
import io.choerodon.devops.app.eventhandler.pipeline.test.CiUnitTestReportHandler;
import io.choerodon.devops.app.eventhandler.pipeline.test.CiUnitTestReportOperator;
import io.choerodon.devops.app.service.AppServiceService;
import io.choerodon.devops.app.service.DevopsCiUnitTestReportService;
import io.choerodon.devops.infra.constant.ResourceCheckConstant;
import io.choerodon.devops.infra.dto.AppServiceDTO;
import io.choerodon.devops.infra.dto.DevopsCiUnitTestReportDTO;
import io.choerodon.devops.infra.mapper.DevopsCiUnitTestReportMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 单元测试报告(DevopsCiUnitTestReport)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2021-12-24 10:30:48
 */
@Service
public class DevopsCiUnitTestReportServiceImpl implements DevopsCiUnitTestReportService {
    @Autowired
    private DevopsCiUnitTestReportMapper devopsCiUnitTestReportMapper;
    @Autowired
    private AppServiceService appServiceService;
    @Autowired
    private CiUnitTestReportOperator ciUnitTestReportOperator;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadUnitTest(Long gitlabPipelineId, String jobName, String token, String type, MultipartFile file) {
        AppServiceDTO appServiceDTO = appServiceService.baseQueryByToken(token);
        if (appServiceDTO == null) {
            throw new CommonException("error.app.svc.not.found");
        }
        Long appServiceId = appServiceDTO.getId();
        // 解析测试报告
        CiUnitTestReportHandler handlerByType = ciUnitTestReportOperator.getHandlerByType(type);
        DevopsCiUnitTestResultVO devopsCiUnitTestResultVO = handlerByType.analyseReport(file);

        // 上传测试报告
        String reportUrl = handlerByType.uploadReport(appServiceId, gitlabPipelineId, jobName, type, file);

        DevopsCiUnitTestReportDTO devopsCiUnitTestReportDTO = queryByUniqueIndex(appServiceId, gitlabPipelineId, jobName, type);
        if (devopsCiUnitTestReportDTO == null) {

            DevopsCiUnitTestReportDTO devopsCiUnitTestReportDTO1 = ConvertUtils.convertObject(devopsCiUnitTestResultVO, DevopsCiUnitTestReportDTO.class);
            devopsCiUnitTestReportDTO1.setAppServiceId(appServiceId);
            devopsCiUnitTestReportDTO1.setGitlabPipelineId(gitlabPipelineId);
            devopsCiUnitTestReportDTO1.setJobName(jobName);
            devopsCiUnitTestReportDTO1.setType(type);
            devopsCiUnitTestReportDTO1.setReportUrl(reportUrl);
            baseCreate(devopsCiUnitTestReportDTO1);

        } else {
            devopsCiUnitTestReportDTO.setReportUrl(reportUrl);
            devopsCiUnitTestReportDTO.setTests(devopsCiUnitTestResultVO.getTests());
            devopsCiUnitTestReportDTO.setFailures(devopsCiUnitTestResultVO.getFailures());
            devopsCiUnitTestReportDTO.setSkipped(devopsCiUnitTestResultVO.getSkipped());
        }

    }

    @Override
    public DevopsCiUnitTestReportDTO queryByUniqueIndex(Long appServiceId, Long gitlabPipelineId, String jobName, String type) {
        Assert.notNull(appServiceId, ResourceCheckConstant.ERROR_APP_SERVICE_ID_IS_NULL);
        Assert.notNull(gitlabPipelineId, ResourceCheckConstant.ERROR_GITLAB_PIPELINE_ID_IS_NULL);
        Assert.notNull(jobName, ResourceCheckConstant.ERROR_JOB_NAME_ID_IS_NULL);

        DevopsCiUnitTestReportDTO devopsCiUnitTestReportDTO = new DevopsCiUnitTestReportDTO();
        devopsCiUnitTestReportDTO.setAppServiceId(appServiceId);
        devopsCiUnitTestReportDTO.setGitlabPipelineId(gitlabPipelineId);
        devopsCiUnitTestReportDTO.setJobName(jobName);
        devopsCiUnitTestReportDTO.setType(type);

        return devopsCiUnitTestReportMapper.selectOne(devopsCiUnitTestReportDTO);
    }

    @Override
    public List<DevopsCiUnitTestReportDTO> listByJobName(Long appServiceId, Long gitlabPipelineId, String jobName) {
        Assert.notNull(appServiceId, ResourceCheckConstant.ERROR_APP_SERVICE_ID_IS_NULL);
        Assert.notNull(gitlabPipelineId, ResourceCheckConstant.ERROR_GITLAB_PIPELINE_ID_IS_NULL);
        Assert.notNull(jobName, ResourceCheckConstant.ERROR_JOB_NAME_ID_IS_NULL);

        DevopsCiUnitTestReportDTO devopsCiUnitTestReportDTO = new DevopsCiUnitTestReportDTO();
        devopsCiUnitTestReportDTO.setAppServiceId(appServiceId);
        devopsCiUnitTestReportDTO.setGitlabPipelineId(gitlabPipelineId);
        devopsCiUnitTestReportDTO.setJobName(jobName);

        return devopsCiUnitTestReportMapper.select(devopsCiUnitTestReportDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(DevopsCiUnitTestReportDTO devopsCiUnitTestReportDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiUnitTestReportMapper,
                devopsCiUnitTestReportDTO,
                "error.save.unit.test.report.failed");
    }

    @Override
    public void baseUpdate(DevopsCiUnitTestReportDTO devopsCiUnitTestReportDTO) {
        devopsCiUnitTestReportMapper.updateByPrimaryKeySelective(devopsCiUnitTestReportDTO);
    }


}

