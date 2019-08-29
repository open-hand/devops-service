package io.choerodon.devops.infra.dto.harbor;

/**
 * @author jiameng.cao
 * @date 2019/8/29
 */
public class RobotUser {
    private Integer robotId;
    private String robotToken;
    private String robotName;
    private String projectName;
    private Integer projectId;

    public Integer getRobotId() {
        return robotId;
    }

    public void setRobotId(Integer robotId) {
        this.robotId = robotId;
    }

    public String getRobotToken() {
        return robotToken;
    }

    public void setRobotToken(String robotToken) {
        this.robotToken = robotToken;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }
}
