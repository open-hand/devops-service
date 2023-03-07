package io.choerodon.devops.infra.handler;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import io.choerodon.devops.app.service.DevopsCiJobRecordService;
import io.choerodon.devops.app.service.DevopsCiPipelineRecordService;
import io.choerodon.devops.infra.constant.GitOpsConstants;
import io.choerodon.devops.infra.enums.PipelineStatus;

/**
 * 主要是为了解决WebHook和DevOps-service之间同步数据时, 丢数据的问题
 * 这个问题会导致CI流水线的执行纪录数据和GitLab显示的不一致
 *
 * @author zmf
 * @since 2020/6/9
 */
@Component
public class CiPipelineSyncHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CiPipelineSyncHandler.class);

    private static final String CI_PIPELINE_REFRESH = "ci-pipeline-refresh";

    /**
     * ci流水线对未终结的流水线进行数据补偿的时间阈值, 单位: 毫秒 (默认600秒)
     */
    @Value("${devops.ci.pipeline.sync.unterminated.thresholdMilliSeconds:600000}")
    private Long unterminatedPipelineSyncThresholdMilliSeconds;

    /**
     * ci流水线对pending的流水线进行数据补偿的时间阈值, 单位: 毫秒 (默认600秒)
     */
    @Value("${devops.ci.pipeline.sync.pending.thresholdMilliSeconds:600000}")
    private Long pendingPipelineSyncThresholdMilliSeconds;

    /**
     * ci流水线对非跳过状态的且没有job信息流水线进行数据补偿的时间阈值, 单位: 毫秒 (默认600秒)
     */
    @Value("${devops.ci.pipeline.sync.jobEmpty.thresholdMilliSeconds:600000}")
    private Long emptyStageThresholdMilliSeconds;

    /**
     * redisKey的过期时间, 用于控制同一条流水线的刷新间隔, 减少对gitlab的访问次数
     */
    @Value("${devops.ci.pipeline.sync.refresh.periodSeconds:60}")
    private Long ciPipelineRefreshSeconds;

    @Lazy
    @Autowired
    private DevopsCiPipelineRecordService devopsCiPipelineRecordService;

    @Autowired
    private DevopsCiJobRecordService devopsCiJobRecordService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 判断是否需要异步地拉取gitlab中流水线的状态到数据库进行更新
     *
     * @param pipelineStatus   流水线状态
     * @param lastUpdateDate   流水线最后更新时间
     * @param pipelineRecordId 流水线纪录id
     * @param gitlabPipelineId 对应的gitlab流水线纪录id
     */
    public void syncPipeline(String pipelineStatus, Date lastUpdateDate,
                             Long pipelineRecordId, Integer gitlabPipelineId) {
        Assert.notNull(pipelineStatus, "Pipeline status can't be null.");
        LOGGER.debug("Sync pipeline... status: {}, date: {}, pipelineRecordId: {}, gitlabPipelineId: {}", pipelineStatus, lastUpdateDate, pipelineRecordId, gitlabPipelineId);

        if (PipelineStatus.RUNNING.toValue().equals(pipelineStatus)) {
            if (beforeSeconds(lastUpdateDate, unterminatedPipelineSyncThresholdMilliSeconds)) {
                if (inFetchPeriod(gitlabPipelineId)) {
                    return;
                }
                LOGGER.info("Sync pipeline... status: {}, date: {}, pipelineRecordId: {}, gitlabPipelineId: {}", pipelineStatus, lastUpdateDate, pipelineRecordId, gitlabPipelineId);
                devopsCiPipelineRecordService.asyncPipelineUpdate(pipelineRecordId, gitlabPipelineId);
                return;
            }
        } else if (PipelineStatus.PENDING.toValue().equals(pipelineStatus)) {
            if (beforeSeconds(lastUpdateDate, pendingPipelineSyncThresholdMilliSeconds)) {
                if (inFetchPeriod(gitlabPipelineId)) {
                    return;
                }
                LOGGER.info("Sync pipeline... status: {}, date: {}, pipelineRecordId: {}, gitlabPipelineId: {}", pipelineStatus, lastUpdateDate, pipelineRecordId, gitlabPipelineId);
                devopsCiPipelineRecordService.asyncPipelineUpdate(pipelineRecordId, gitlabPipelineId);
                return;
            }
        }

        if (!PipelineStatus.SKIPPED.toValue().equals(pipelineStatus)) {
            // 如果流水线状态不是跳过, 但是job数据为空, 也进行同步
            if (beforeSeconds(lastUpdateDate, emptyStageThresholdMilliSeconds)
                    && devopsCiJobRecordService.selectCountByCiPipelineRecordId(pipelineRecordId) == 0) {
                if (inFetchPeriod(gitlabPipelineId)) {
                    return;
                }
                LOGGER.info("Sync pipeline for pipeline with empty jobs... status: {}, date: {}, pipelineRecordId: {}, gitlabPipelineId: {}", pipelineStatus, lastUpdateDate, pipelineRecordId, gitlabPipelineId);
                devopsCiPipelineRecordService.asyncPipelineUpdate(pipelineRecordId, gitlabPipelineId);
            }
        }
    }

    /**
     * 确定是否在查询间隔中, 可以进行数据刷新, 减少对gitlab的访问次数
     *
     * @param gitlabPipelineId 流水线id
     * @return true表示在查询间隔中, 不可以继续执行刷新逻辑
     */
    public boolean inFetchPeriod(Integer gitlabPipelineId) {
        String redisKey = String.format(GitOpsConstants.CI_PIPELINE_REDIS_KEY_TEMPLATE, gitlabPipelineId);
        // 如果已经有key, 说明此时还处于刷新间隔中
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey))) {
            LOGGER.debug("Pipeline with gitlab pipeline id {} is in the refresh period {}, abort...", gitlabPipelineId, ciPipelineRefreshSeconds);
            return false;
        }
        // 没有key就设置一个过期时间为特定值的key
        stringRedisTemplate.opsForValue().set(redisKey, CI_PIPELINE_REFRESH, ciPipelineRefreshSeconds, TimeUnit.SECONDS);
        return true;
    }

    /**
     * 是否时间在指定的毫秒数之前
     *
     * @param date               时间
     * @param beforeMilliSeconds 毫秒数
     * @return true表示是
     */
    private static boolean beforeSeconds(Date date, long beforeMilliSeconds) {
        Date before = new Date(System.currentTimeMillis() - beforeMilliSeconds);
        return date.before(before);
    }
}
