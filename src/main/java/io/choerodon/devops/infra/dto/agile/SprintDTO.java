package io.choerodon.devops.infra.dto.agile;


import java.util.Date;

public class SprintDTO {
    private Long sprintId;
    private String sprintName;
    private Date startDate;
    private Date endDate;
    private Long dayRemain;

    public Long getSprintId() {
        return sprintId;
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }

    public String getSprintName() {
        return sprintName;
    }

    public void setSprintName(String sprintName) {
        this.sprintName = sprintName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getDayRemain() {
        return dayRemain;
    }

    public void setDayRemain(Long dayRemain) {
        this.dayRemain = dayRemain;
    }
}
