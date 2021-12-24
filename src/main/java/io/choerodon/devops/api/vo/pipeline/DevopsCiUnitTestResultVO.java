package io.choerodon.devops.api.vo.pipeline;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/12/24 11:21
 */
public class DevopsCiUnitTestResultVO {
    @ApiModelProperty(value = "测试用例总数", required = true)
    @NotNull
    private Long tests;

    @ApiModelProperty(value = "失败用例总数", required = true)
    @NotNull
    private Long failures;

    @ApiModelProperty(value = "跳过用例总数", required = true)
    @NotNull
    private Long skipped;

    public DevopsCiUnitTestResultVO(@NotNull Long tests, @NotNull Long failures, @NotNull Long skipped) {
        this.tests = tests;
        this.failures = failures;
        this.skipped = skipped;
    }

    public Long getTests() {
        return tests;
    }

    public void setTests(Long tests) {
        this.tests = tests;
    }

    public Long getFailures() {
        return failures;
    }

    public void setFailures(Long failures) {
        this.failures = failures;
    }

    public Long getSkipped() {
        return skipped;
    }

    public void setSkipped(Long skipped) {
        this.skipped = skipped;
    }
}
