package io.choerodon.devops.infra.persistence.impl;

import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.infra.util.PageRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.domain.application.entity.DevopsNotificationE;
import io.choerodon.devops.domain.application.repository.DevopsNotificationRepository;
import io.choerodon.devops.infra.util.TypeUtil;
import io.choerodon.devops.infra.dataobject.DevopsNotificationDO;
import io.choerodon.devops.infra.mapper.DevopsNotificationMapper;


/**
 * Creator: ChangpingShi0213@gmail.com
 * Date:  19:22 2019/5/13
 * Description:
 */
@Component
public class DevopsNotificationRepositoryImpl implements DevopsNotificationRepository {
    private static final Gson gson = new Gson();

    @Autowired
    private DevopsNotificationMapper notificationMapper;

    @Override
    public DevopsNotificationE createOrUpdate(DevopsNotificationE notificationE) {
        DevopsNotificationDO notificationDO = ConvertHelper.convert(notificationE, DevopsNotificationDO.class);
        if (notificationDO.getId() == null) {
            if (notificationMapper.insert(notificationDO) != 1) {
                throw new CommonException("error.notification.create");
            }
        } else {
            if (notificationMapper.updateByPrimaryKeySelective(notificationDO) != 1) {
                throw new CommonException("error.notification.update");
            }
        }
        return ConvertHelper.convert(notificationDO, DevopsNotificationE.class);
    }

    @Override
    public void deleteById(Long notificationId) {
        notificationMapper.deleteByPrimaryKey(notificationId);
    }

    @Override
    public DevopsNotificationE queryById(Long notificationId) {
        return ConvertHelper.convert(notificationMapper.selectByPrimaryKey(notificationId), DevopsNotificationE.class);
    }

    @Override
    public List<DevopsNotificationE> ListByEnvId(Long envId) {
        DevopsNotificationDO devopsNotificationDO = new DevopsNotificationDO();
        devopsNotificationDO.setEnvId(envId);
        return ConvertHelper.convertList(notificationMapper.select(devopsNotificationDO), DevopsNotificationE.class);
    }

    @Override
    public PageInfo<DevopsNotificationE> listByOptions(Long projectId, Long envId, String params, PageRequest pageRequest) {
        Map<String, Object> map = gson.fromJson(params, Map.class);
        Map<String, Object> searchParamMap = TypeUtil.cast(map.get(TypeUtil.SEARCH_PARAM));
        String paramMap = TypeUtil.cast(map.get(TypeUtil.PARAM));
        PageInfo<DevopsNotificationDO> notificationDOPage = PageHelper.startPage(pageRequest.getPage(),pageRequest.getSize(), PageRequestUtil.getOrderBy(pageRequest)).doSelectPageInfo(() -> notificationMapper.listByOptions(projectId, envId, searchParamMap, paramMap));
        return ConvertPageHelper.convertPageInfo(notificationDOPage, DevopsNotificationE.class);
    }

    @Override
    public List<DevopsNotificationE> queryByEnvId(Long projectId, Long envId) {
        DevopsNotificationDO notificationDO = new DevopsNotificationDO();
        notificationDO.setProjectId(projectId);
        notificationDO.setEnvId(envId);
        return ConvertHelper.convertList(notificationMapper.select(notificationDO), DevopsNotificationE.class);
    }
}
