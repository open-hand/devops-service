package io.choerodon.devops.infra.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.hzero.mybatis.BatchInsertHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.choerodon.devops.infra.dto.*;

/**
 * 批量插入的配置
 */
@Configuration
public class BatchInsertHelperConfig {

    @Value("${hzero.supporter.batch-insert.slice-size:500}")
    private int sliceSize;

    @Bean("devopsIssueRelBatchInsertHelper")
    public BatchInsertHelper<DevopsIssueRelDTO> branchIssueBatchInsertHelper(SqlSessionFactory sqlSessionFactory) {
        return new BatchInsertHelper<>(sqlSessionFactory, this.sliceSize);
    }

    @Bean("devopsHostUserPermissionInsertHelper")
    public BatchInsertHelper<DevopsHostUserPermissionDTO> hostUserPermissionDTOBatchInsertHelper(SqlSessionFactory sqlSessionFactory) {
        return new BatchInsertHelper<>(sqlSessionFactory, this.sliceSize);
    }

    @Bean("devopsAppCenterHelper")
    public BatchInsertHelper<DevopsDeployAppCenterEnvDTO> devopsAppCenterHelper(SqlSessionFactory sqlSessionFactory) {
        return new BatchInsertHelper<>(sqlSessionFactory, this.sliceSize);
    }

    @Bean("devopsHelmConfigHelper")
    public BatchInsertHelper<DevopsHelmConfigDTO> devopsHelmConfigDTOBatchInsertHelper(SqlSessionFactory sqlSessionFactory) {
        return new BatchInsertHelper<>(sqlSessionFactory, this.sliceSize);
    }

    @Bean("appServiceHelmConfigHelper")
    public BatchInsertHelper<AppServiceHelmRelDTO> appServiceHelmRelDTOBatchInsertHelper(SqlSessionFactory sqlSessionFactory) {
        return new BatchInsertHelper<>(sqlSessionFactory, this.sliceSize);
    }

    @Bean("appServiceVersionHelmConfigHelper")
    public BatchInsertHelper<AppServiceHelmVersionDTO> appServiceHelmVersionDTOBatchInsertHelper(SqlSessionFactory sqlSessionFactory) {
        return new BatchInsertHelper<>(sqlSessionFactory, this.sliceSize);
    }
}