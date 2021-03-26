package io.choerodon.devops.app.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.DevopsEnvFileErrorVO;
import io.choerodon.devops.app.service.DevopsEnvFileErrorService;
import io.choerodon.devops.infra.dto.DevopsEnvFileErrorDTO;
import io.choerodon.devops.infra.mapper.DevopsEnvFileErrorMapper;
import io.choerodon.devops.infra.util.PageRequestUtil;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;

/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  13:43 2019/7/12
 * Description:
 */
@Service
public class DevopsEnvFileErrorServiceImpl implements DevopsEnvFileErrorService {
    @Autowired
    DevopsEnvFileErrorMapper devopsEnvFileErrorMapper;


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DevopsEnvFileErrorDTO baseCreateOrUpdate(DevopsEnvFileErrorDTO devopsEnvFileErrorDTO) {
        DevopsEnvFileErrorDTO newDevopsEnvFileErrorDTO = devopsEnvFileErrorMapper.selectOne(new DevopsEnvFileErrorDTO(devopsEnvFileErrorDTO.getEnvId(), devopsEnvFileErrorDTO.getFilePath()));
        if (newDevopsEnvFileErrorDTO != null) {
            newDevopsEnvFileErrorDTO.setCommit(devopsEnvFileErrorDTO.getCommit());
            newDevopsEnvFileErrorDTO.setError(devopsEnvFileErrorDTO.getError());
            if (devopsEnvFileErrorMapper.updateByPrimaryKeySelective(newDevopsEnvFileErrorDTO) != 1) {
                throw new CommonException("error.env.error.file.update");
            }
        } else {
            if (devopsEnvFileErrorMapper.insert(devopsEnvFileErrorDTO) != 1) {
                throw new CommonException("error.env.error.file.create");
            }
        }
        return devopsEnvFileErrorMapper.selectByPrimaryKey(devopsEnvFileErrorDTO.getId());
    }

    @Override
    public List<DevopsEnvFileErrorDTO> baseListByEnvId(Long envId) {
        DevopsEnvFileErrorDTO devopsEnvFileErrorDTO = new DevopsEnvFileErrorDTO();
        devopsEnvFileErrorDTO.setEnvId(envId);
        return devopsEnvFileErrorMapper.select(devopsEnvFileErrorDTO);
    }

    @Override
    public Page<DevopsEnvFileErrorDTO> basePageByEnvId(Long envId, PageRequest pageable) {
        DevopsEnvFileErrorDTO devopsEnvFileErrorDTO = new DevopsEnvFileErrorDTO();
        devopsEnvFileErrorDTO.setEnvId(envId);
        return PageHelper.doPageAndSort(PageRequestUtil.simpleConvertSortForPage(pageable), () -> devopsEnvFileErrorMapper.select(devopsEnvFileErrorDTO));
    }


    @Override
    public void baseDelete(DevopsEnvFileErrorDTO devopsEnvFileErrorDTO) {
        devopsEnvFileErrorMapper.delete(devopsEnvFileErrorDTO);
    }

    @Override
    public DevopsEnvFileErrorDTO baseQueryByEnvIdAndFilePath(Long envId, String filePath) {
        DevopsEnvFileErrorDTO devopsEnvFileErrorDO = new DevopsEnvFileErrorDTO();
        devopsEnvFileErrorDO.setEnvId(envId);
        devopsEnvFileErrorDO.setFilePath(filePath);
        return devopsEnvFileErrorMapper.selectOne(devopsEnvFileErrorDO);
    }

    @Override
    public void baseCreate(DevopsEnvFileErrorVO devopsEnvFileErrorVO) {
        DevopsEnvFileErrorDTO devopsEnvFileErrorDTO = new DevopsEnvFileErrorDTO();
        BeanUtils.copyProperties(devopsEnvFileErrorVO, devopsEnvFileErrorDTO);
        devopsEnvFileErrorMapper.insert(devopsEnvFileErrorDTO);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public void deleteByEnvId(Long envId) {
        DevopsEnvFileErrorDTO devopsEnvFileErrorDO = new DevopsEnvFileErrorDTO();
        devopsEnvFileErrorDO.setEnvId(Objects.requireNonNull(envId));
        devopsEnvFileErrorMapper.delete(devopsEnvFileErrorDO);
    }
}
