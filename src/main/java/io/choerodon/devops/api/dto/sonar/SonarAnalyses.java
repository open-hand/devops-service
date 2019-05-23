package io.choerodon.devops.api.dto.sonar;

import java.util.List;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  15:02 2019/5/23
 * Description:
 */
public class SonarAnalyses {
    private List<Analyse> analyses;

    public List<Analyse> getAnalyses() {
        return analyses;
    }

    public void setAnalyses(List<Analyse> analyses) {
        this.analyses = analyses;
    }
}
