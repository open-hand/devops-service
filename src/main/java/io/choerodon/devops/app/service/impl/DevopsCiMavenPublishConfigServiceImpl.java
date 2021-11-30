package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.DevopsCiMavenPublishConfigVO;
import io.choerodon.devops.api.vo.MavenRepoVO;
import io.choerodon.devops.app.service.DevopsCiMavenPublishConfigService;
import io.choerodon.devops.infra.dto.DevopsCiMavenPublishConfigDTO;
import io.choerodon.devops.infra.mapper.DevopsCiMavenPublishConfigMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 18:05
 */
@Service
public class DevopsCiMavenPublishConfigServiceImpl implements DevopsCiMavenPublishConfigService {
    @Autowired
    private DevopsCiMavenPublishConfigMapper devopsCiMavenPublishConfigMapper;

    @Override
    public DevopsCiMavenPublishConfigDTO baseQueryById(Long id) {
        return devopsCiMavenPublishConfigMapper.selectByPrimaryKey(id);
    }

    @Override
    public DevopsCiMavenPublishConfigVO queryById(Long id) {
        DevopsCiMavenPublishConfigDTO devopsCiMavenPublishConfigDTO = baseQueryById(id);
        DevopsCiMavenPublishConfigVO devopsCiMavenPublishConfigVO = ConvertUtils.convertObject(devopsCiMavenPublishConfigDTO, DevopsCiMavenPublishConfigVO.class);
        if (StringUtils.isNoneBlank(devopsCiMavenPublishConfigVO.getNexusMavenRepoIdStr())) {
            devopsCiMavenPublishConfigVO
                    .setNexusMavenRepoIds(JsonHelper
                            .unmarshalByJackson(devopsCiMavenPublishConfigVO.getNexusMavenRepoIdStr(), new TypeReference<Set<Long>>() {}));
        }

        if (StringUtils.isNoneBlank(devopsCiMavenPublishConfigVO.getRepoStr())) {
            devopsCiMavenPublishConfigVO
                    .setRepos(JsonHelper
                            .unmarshalByJackson(devopsCiMavenPublishConfigVO.getRepoStr(), new TypeReference<List<MavenRepoVO>>() {}));
        }
        return devopsCiMavenPublishConfigVO;
    }

    @Override
    @Transactional
    public void baseCreate(DevopsCiMavenPublishConfigDTO devopsCiMavenPublishConfigDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiMavenPublishConfigMapper,
                devopsCiMavenPublishConfigDTO,
                "error.save.maven.publish.config.failed");
    }

    @Override
    @Transactional
    public void batchDeleteByIds(Set<Long> ids) {
        devopsCiMavenPublishConfigMapper.batchDeleteByIds(ids);
    }
}
