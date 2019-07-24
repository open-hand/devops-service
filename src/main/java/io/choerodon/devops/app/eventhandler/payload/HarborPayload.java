package io.choerodon.devops.app.eventhandler.payload;


/**
 * Created with IntelliJ IDEA.
 * User: Runge
 * Date: 2018/4/4
 * Time: 11:19
 * Description:
 */
public class HarborPayload {

    private Long projectId;
    private String projectCode;

    /**
     * Harbor 事件
     *
     * @param projectId   项目ID
     * @param projectCode 项目编码
     */
    public HarborPayload(Long projectId, String projectCode) {
        this.projectId = projectId;
        this.projectCode = projectCode;
    }

    public HarborPayload() {
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectCode() {
        return projectCode;
    }


}
