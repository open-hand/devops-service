package io.choerodon.devops.infra.mapper;

import io.choerodon.devops.infra.dto.DockerComposeValueDTO;
import io.choerodon.mybatis.common.BaseMapper;

/**
 * docker compose部署时保存的yaml文件内容(DockerComposeValue)应用服务
 *
 * @author hao.wang08@hand-china.com
 * @since 2022-04-07 10:25:55
 */
public interface DockerComposeValueMapper extends BaseMapper<DockerComposeValueDTO> {

}

