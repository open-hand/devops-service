package io.choerodon.devops.api.vo.sonar;

import java.util.List;

/**
 * Created by Sheep on 2019/5/7.
 */
public class QualityGateResult {

    private String level;

    private List<QualityGateConditionResult> conditions;

    public List<QualityGateConditionResult> getConditions() {
        return conditions;
    }

    public void setConditions(List<QualityGateConditionResult> conditions) {
        this.conditions = conditions;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public static class QualityGateConditionResult {
        private String metric;
        private String op;
        private String period;
        private String error;
        private String actual;
        private String level;

        public String getMetric() {
            return metric;
        }

        public void setMetric(String metric) {
            this.metric = metric;
        }

        public String getOp() {
            return op;
        }

        public void setOp(String op) {
            this.op = op;
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getActual() {
            return actual;
        }

        public void setActual(String actual) {
            this.actual = actual;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }
    }
}
