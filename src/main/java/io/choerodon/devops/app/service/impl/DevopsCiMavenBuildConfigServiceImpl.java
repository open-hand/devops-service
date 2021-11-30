package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.devops.api.vo.DevopsCiMavenBuildConfigVO;
import io.choerodon.devops.api.vo.MavenRepoVO;
import io.choerodon.devops.app.service.DevopsCiMavenBuildConfigService;
import io.choerodon.devops.infra.dto.DevopsCiMavenBuildConfigDTO;
import io.choerodon.devops.infra.mapper.DevopsCiMavenBuildConfigMapper;
import io.choerodon.devops.infra.util.ConvertUtils;
import io.choerodon.devops.infra.util.JsonHelper;
import io.choerodon.devops.infra.util.MapperUtil;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2021/11/30 15:03
 */
@Service
public class DevopsCiMavenBuildConfigServiceImpl implements DevopsCiMavenBuildConfigService {
    @Autowired
    private DevopsCiMavenBuildConfigMapper devopsCiMavenBuildConfigMapper;


    @Override
    public DevopsCiMavenBuildConfigDTO baseQuery(Long id) {
        return devopsCiMavenBuildConfigMapper.selectByPrimaryKey(id);
    }

    @Override
    public DevopsCiMavenBuildConfigVO queryById(Long id) {
        DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO = baseQuery(id);
        DevopsCiMavenBuildConfigVO devopsCiMavenBuildConfigVO = ConvertUtils.convertObject(devopsCiMavenBuildConfigDTO, DevopsCiMavenBuildConfigVO.class);
        if (StringUtils.isNoneBlank(devopsCiMavenBuildConfigVO.getNexusMavenRepoIdStr())) {
            devopsCiMavenBuildConfigVO
                    .setNexusMavenRepoIds(JsonHelper
                            .unmarshalByJackson(devopsCiMavenBuildConfigVO.getNexusMavenRepoIdStr(), new TypeReference<Set<Long>>() {}));
        }

        if (StringUtils.isNoneBlank(devopsCiMavenBuildConfigVO.getRepoStr())) {
            devopsCiMavenBuildConfigVO
                    .setRepos(JsonHelper
                            .unmarshalByJackson(devopsCiMavenBuildConfigVO.getRepoStr(), new TypeReference<List<MavenRepoVO>>() {}));
        }

        return devopsCiMavenBuildConfigVO;
    }

    @Override
    @Transactional
    public void baseCreate(DevopsCiMavenBuildConfigDTO devopsCiMavenBuildConfigDTO) {
        MapperUtil.resultJudgedInsertSelective(devopsCiMavenBuildConfigMapper,
                devopsCiMavenBuildConfigDTO,
                "error.save.maven.build.config.failed");
    }
}
