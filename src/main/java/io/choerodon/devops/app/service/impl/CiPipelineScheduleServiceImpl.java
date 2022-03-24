package io.choerodon.devops.app.service.impl;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.CiPipelineScheduleVO;
import io.choerodon.devops.app.service.CiPipelineScheduleService;
import io.choerodon.devops.infra.dto.CiPipelineScheduleDTO;
import io.choerodon.devops.infra.enums.CiPipelineScheduleTriggerTypeEnum;
import io.choerodon.devops.infra.mapper.CiPipelineScheduleMapper;
import io.choerodon.devops.infra.util.ConvertUtils;

/**
 * devops_ci_pipeline_schedule(CiPipelineSchedule)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-03-24 17:00:27
 */
@Service
public class CiPipelineScheduleServiceImpl implements CiPipelineScheduleService {
    @Autowired
    private CiPipelineScheduleMapper ciPipelineScheduleMapper;


    @Override
    @Transactional
    public CiPipelineScheduleDTO create(CiPipelineScheduleVO ciPipelineScheduleVO) {
        validate(ciPipelineScheduleVO);

        CiPipelineScheduleDTO ciPipelineScheduleDTO = ConvertUtils.convertObject(ciPipelineScheduleVO, CiPipelineScheduleDTO.class);
        String cron = calculateCron(ciPipelineScheduleVO);


        return null;
    }

    /**
     * 计算cron表达式
     * @param ciPipelineScheduleVO
     * @return
     */
    private String calculateCron(CiPipelineScheduleVO ciPipelineScheduleVO) {
        String cronTemplate = "s% s% * * s%";
        String minute = "";
        String hour = "";
        if (CiPipelineScheduleTriggerTypeEnum.PERIOD.value().equals(ciPipelineScheduleVO.getTriggerType())) {
            if (ciPipelineScheduleVO.getPeriod() >= 60) {
                minute = "0";
                hour = ciPipelineScheduleVO.getStartHourOfDay() + "-" + ciPipelineScheduleVO.getEndHourOfDay() + "/" + (ciPipelineScheduleVO.getPeriod() / 60);
            } else {
                minute = "0-59/" + ciPipelineScheduleVO.getPeriod();
                hour = ciPipelineScheduleVO.getStartHourOfDay() + "-" + ciPipelineScheduleVO.getEndHourOfDay();
            }
        } else if (CiPipelineScheduleTriggerTypeEnum.SINGLE.value().equals(ciPipelineScheduleVO.getTriggerType())) {
            Calendar instance = Calendar.getInstance();
            instance.setTime(ciPipelineScheduleVO.getExecuteTime());
            minute = String.valueOf(instance.get(Calendar.MINUTE));
            hour = String.valueOf(instance.get(Calendar.HOUR));
        } else {
            throw new CommonException("error.trigger.type.invalid");
        }


        String week = ciPipelineScheduleVO.getWeekNumber();
        return String.format(cronTemplate, minute, hour, week);
    }

    private void validate(CiPipelineScheduleVO ciPipelineScheduleVO) {
        if (CiPipelineScheduleTriggerTypeEnum.PERIOD.value().equals(ciPipelineScheduleVO.getTriggerType())) {
            if (ciPipelineScheduleVO.getStartHourOfDay() == null) {
                throw new CommonException("error.start.hour.of.day.is.null");
            }
            if (ciPipelineScheduleVO.getEndHourOfDay() == null) {
                throw new CommonException("error.end.hour.of.day.is.null");
            }
            if (ciPipelineScheduleVO.getPeriod() == null) {
                throw new CommonException("error.period.is.null");
            }
        } else if (CiPipelineScheduleTriggerTypeEnum.SINGLE.value().equals(ciPipelineScheduleVO.getTriggerType())) {
            if (ciPipelineScheduleVO.getExecuteTime() == null) {
                throw new CommonException("error.executeTime.is.null");
            }
        } else {
            throw new CommonException("error.trigger.type.invalid");
        }
    }
}

