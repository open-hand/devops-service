package io.choerodon.devops.api.vo.sonar;

import java.util.List;

/**
 * Created by Sheep on 2019/5/7.
 */
public class SonarTables {

    private List<SonarTableMeasure> measures;


    public List<SonarTableMeasure> getMeasures() {
        return measures;
    }

    public void setMeasures(List<SonarTableMeasure> measures) {
        this.measures = measures;
    }
}
