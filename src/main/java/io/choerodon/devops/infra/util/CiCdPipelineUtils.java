package io.choerodon.devops.infra.util;

import static io.choerodon.devops.infra.constant.GitOpsConstants.DEFAULT_PIPELINE_RECORD_SIZE;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

import io.choerodon.core.domain.Page;
import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;
import io.choerodon.devops.api.vo.CiCdPipelineVO;
import io.choerodon.devops.api.vo.DevopsCdPipelineRecordVO;
import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.infra.enums.PipelineStatus;

public class CiCdPipelineUtils {


    public static void recordListSort(List<CiCdPipelineRecordVO> list) {
        Collections.sort(list, new Comparator<CiCdPipelineRecordVO>() {
            @Override
            public int compare(CiCdPipelineRecordVO o1, CiCdPipelineRecordVO o2) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {

                    if (o1.getCreatedDate().getTime() > o2.getCreatedDate().getTime()) {
                        return -1;
                    } else if (o1.getCreatedDate().getTime() < o2.getCreatedDate().getTime()) {
                        return 1;
                    } else {
                        return 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        });
    }


    public static void calculateStatus(CiCdPipelineRecordVO ciCdPipelineRecordVO, DevopsCiPipelineRecordVO devopsCiPipelineRecordVO, DevopsCdPipelineRecordVO devopsCdPipelineRecordVO) {
        //计算记录的状态
        if (!PipelineStatus.SUCCESS.toValue().equals(devopsCiPipelineRecordVO.getStatus())) {
            ciCdPipelineRecordVO.setStatus(devopsCdPipelineRecordVO.getStatus());
        }
        if (PipelineStatus.SUCCESS.toValue().equals(devopsCiPipelineRecordVO.getStatus())
                && PipelineStatus.SUCCESS.toValue().equals(devopsCdPipelineRecordVO.getStatus())) {
            ciCdPipelineRecordVO.setStatus(PipelineStatus.SUCCESS.toValue());
        }
        //如果ci状态成功cd是未执行，则状态为执行中
        if (PipelineStatus.SUCCESS.toValue().equals(devopsCiPipelineRecordVO.getStatus()) &&
                PipelineStatus.CREATED.toValue().equals(devopsCdPipelineRecordVO.getStatus())) {
            ciCdPipelineRecordVO.setStatus(PipelineStatus.RUNNING.toValue());
        } else {
            ciCdPipelineRecordVO.setStatus(devopsCdPipelineRecordVO.getStatus());
        }
    }


    public static void calculateStatus(CiCdPipelineVO ciCdPipelineVO, Page<DevopsCiPipelineRecordVO> pipelineCiRecordVOPageInfo, Page<DevopsCdPipelineRecordVO> devopsCdPipelineRecordVOS) {
        //纯CD
        if (!CollectionUtils.isEmpty(devopsCdPipelineRecordVOS.getContent())) {
            List<DevopsCdPipelineRecordVO> cdPipelineRecordVOS = devopsCdPipelineRecordVOS.getContent().stream().sorted(Comparator.comparing(DevopsCdPipelineRecordVO::getId).reversed()).collect(Collectors.toList());
            ciCdPipelineVO.setLatestExecuteDate(cdPipelineRecordVOS.get(0).getCreatedDate());
            ciCdPipelineVO.setLatestExecuteStatus(cdPipelineRecordVOS.get(0).getStatus());
            ciCdPipelineVO.setHasMoreRecords(devopsCdPipelineRecordVOS.getTotalElements() > DEFAULT_PIPELINE_RECORD_SIZE);
        }
        //纯CI
        if (!CollectionUtils.isEmpty(pipelineCiRecordVOPageInfo.getContent())) {
            List<DevopsCiPipelineRecordVO> ciPipelineRecordVOS = pipelineCiRecordVOPageInfo.getContent().stream().sorted(Comparator.comparing(DevopsCiPipelineRecordVO::getId).reversed()).collect(Collectors.toList());
            ciCdPipelineVO.setLatestExecuteDate(ciPipelineRecordVOS.get(0).getCreatedDate());
            ciCdPipelineVO.setLatestExecuteStatus(ciPipelineRecordVOS.get(0).getStatus());
            ciCdPipelineVO.setHasMoreRecords(pipelineCiRecordVOPageInfo.getTotalElements() > DEFAULT_PIPELINE_RECORD_SIZE);
        }
        //cicd
        if (!CollectionUtils.isEmpty(devopsCdPipelineRecordVOS.getContent()) && !CollectionUtils.isEmpty(pipelineCiRecordVOPageInfo.getContent())) {
            ciCdPipelineVO.setHasMoreRecords(pipelineCiRecordVOPageInfo.getTotalElements() > DEFAULT_PIPELINE_RECORD_SIZE);
            DevopsCdPipelineRecordVO devopsCdPipelineRecordVO = devopsCdPipelineRecordVOS.getContent().stream().sorted(Comparator.comparing(DevopsCdPipelineRecordVO::getId).reversed()).collect(Collectors.toList()).get(0);
            DevopsCiPipelineRecordVO devopsCiPipelineRecordVO = pipelineCiRecordVOPageInfo.getContent().stream().sorted(Comparator.comparing(DevopsCiPipelineRecordVO::getId).reversed()).collect(Collectors.toList()).get(0);
            ciCdPipelineVO.setLatestExecuteDate(devopsCiPipelineRecordVO.getCreatedDate());
            if (!PipelineStatus.SUCCESS.toValue().equals(devopsCiPipelineRecordVO.getStatus())) {
                ciCdPipelineVO.setLatestExecuteStatus(devopsCiPipelineRecordVO.getStatus());
            } else if (PipelineStatus.SUCCESS.toValue().equals(devopsCiPipelineRecordVO.getStatus())
                    && PipelineStatus.SUCCESS.toValue().equals(devopsCdPipelineRecordVO.getStatus())) {
                ciCdPipelineVO.setLatestExecuteStatus(PipelineStatus.SUCCESS.toValue());
            } else if (PipelineStatus.SUCCESS.toValue().equals(devopsCiPipelineRecordVO.getStatus()) &&
                    PipelineStatus.CREATED.toValue().equals(devopsCdPipelineRecordVO.getStatus())) {
                ciCdPipelineVO.setLatestExecuteStatus(PipelineStatus.RUNNING.toValue());
            } else {
                ciCdPipelineVO.setLatestExecuteStatus(devopsCdPipelineRecordVO.getStatus());
            }
        }

    }
}
