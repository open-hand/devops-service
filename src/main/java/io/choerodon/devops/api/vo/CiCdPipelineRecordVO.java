package io.choerodon.devops.api.vo;


/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/7 22:18
 */
public class CiCdPipelineRecordVO {
    private DevopsCiPipelineRecordVO ciPipelineRecordVO;
    private DevopsCdPipelineRecordVO cdPipelineRecordVO;

    public DevopsCiPipelineRecordVO getCiPipelineRecordVO() {
        return ciPipelineRecordVO;
    }

    public void setCiPipelineRecordVO(DevopsCiPipelineRecordVO ciPipelineRecordVO) {
        this.ciPipelineRecordVO = ciPipelineRecordVO;
    }

    public DevopsCdPipelineRecordVO getCdPipelineRecordVO() {
        return cdPipelineRecordVO;
    }

    public void setCdPipelineRecordVO(DevopsCdPipelineRecordVO cdPipelineRecordVO) {
        this.cdPipelineRecordVO = cdPipelineRecordVO;
    }
}
