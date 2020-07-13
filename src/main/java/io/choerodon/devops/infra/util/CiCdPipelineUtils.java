package io.choerodon.devops.infra.util;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;
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


}
