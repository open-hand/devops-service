package io.choerodon.devops.infra.util;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.choerodon.devops.api.vo.CiCdPipelineRecordVO;

public class CiCdPipelineSortUtils {


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
}
