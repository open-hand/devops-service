import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { forEach, isEmpty, map, keys } from 'lodash';
import { DataSet } from 'choerodon-ui/pro';
import { axios } from '@choerodon/boot';
import CreateFormDataSet, { transFormData } from './CreateFormDataSet';
import PortDataSet from './PortDataSet';
import AppInstanceOptionsDataSet from './AppInstanceOptionsDataSet';
import TargetLabelsDataSet from './TargetLabelsDataSet';
import NetworkInfoDataSet from './NetworkInfoDataSet';
import EndPointsDataSet from './EndPointsDataSet';

const NetWorkStore = createContext();

function useNetWorkStore() {
  return useContext(NetWorkStore);
}

function StoreProvider(props) {
  const {
    AppState: { currentMenuType: { projectId } },
    intl: { formatMessage },
    children,
    envId,
    store: networkStore,
    netId: networkId,
    appServiceId,
  } = props;


  const appInstanceOptionsDs = useMemo(() => new DataSet(AppInstanceOptionsDataSet(projectId, envId, formatMessage)), [projectId, envId]);

  const portDs = useMemo(() => new DataSet(PortDataSet({ formatMessage, projectId, envId })), [projectId, envId]);
  const endPointsDs = useMemo(() => new DataSet(EndPointsDataSet({ formatMessage })), []);
  const targetLabelsDs = useMemo(() => new DataSet(TargetLabelsDataSet({ formatMessage })), []);

  const networkInfoDs = useMemo(() => new DataSet(NetworkInfoDataSet(projectId, networkId)), [networkId]);

  const formDs = useMemo(() => new DataSet(CreateFormDataSet({ formatMessage, portDs, endPointsDs, targetLabelsDs, appInstanceOptionsDs, networkStore, projectId, envId, networkEdit: { networkInfoDs, networkId, initTargetLabel, initPorts, initEndPoints } })), [projectId, envId]);
  

  useEffect(() => {
    if (networkId && formDs.current) {
      formDs.transport.create = ({ data: [data] }) => ({
        method: 'put',
        url: `/devops/v1/projects/${projectId}/service/${networkId}`,
        data: transFormData(data, formatMessage, envId),
      });
      if (appServiceId) {
        appInstanceOptionsDs.transport.read.url = `/devops/v1/projects/${projectId}/app_service_instances/list_running_instance?env_id=${envId}&app_service_id=${appServiceId}`;
        axios.all([networkInfoDs.query(), appInstanceOptionsDs.query()])
          .then(([res]) => {
            const { type, target, target: { instances, targetAppServiceId } } = res;
            loadInfo({ data: res, formatMessage, targetLabelsDs, portDs, endPointsDs, formDs, networkInfoDs });
            // 这里做兼容旧数据的处理 一个网络对应部分实例
            if (!targetAppServiceId && instances && instances.length) {
              forEach(instances, (item, index) => {
                if (!appInstanceOptionsDs.find((record) => record.get('code') === item.code)) {
                  const record = appInstanceOptionsDs.create(item);
                  appInstanceOptionsDs.push(record);
                }
              });
            }
          });
      } else {
        networkInfoDs.query()
          .then((res) => {
            const { type, target, target: { instances, targetAppServiceId }, appServiceId: currentAppServiceId } = res;
            loadInfo({ data: res, formatMessage, targetLabelsDs, portDs, endPointsDs, formDs, networkInfoDs });
            // 这里做兼容旧数据的处理 一个网络对应部分实例
            if (currentAppServiceId || targetAppServiceId) {
              appInstanceOptionsDs.transport.read.url = `/devops/v1/projects/${projectId}/app_service_instances/list_running_instance?env_id=${envId}&app_service_id=${currentAppServiceId || targetAppServiceId}`;
              appInstanceOptionsDs.query()
                .then(() => {
                  if (!targetAppServiceId && instances && instances.length) {
                    forEach(instances, (item, index) => {
                      if (!appInstanceOptionsDs.find((record) => record.get('code') === item.code)) {
                        const record = appInstanceOptionsDs.create(item);
                        appInstanceOptionsDs.push(record);
                      }
                    });
                  }
                });
            }
          });
      }
    }
  }, [networkId, formDs.current]);
  
  useEffect(() => {
    if (!networkId) {
      portDs.create();
      endPointsDs.create();
      targetLabelsDs.create();
    }
  }, []);


  const value = {
    ...props,
    formDs,
    portDs,
    endPointsDs,
    targetLabelsDs,
    appInstanceOptionsDs,
    networkInfoDs,
    networkId,
  };

  return (
    <NetWorkStore.Provider value={value}>
      {children}
    </NetWorkStore.Provider>
  );
}

function loadInfo({ data, formatMessage, targetLabelsDs, portDs, endPointsDs, formDs, networkInfoDs }) {
  const { name, id, type, target, config, appServiceId } = data;
  const { instances, targetAppServiceId, selectors, endPoints } = target;

  // 判断目标对象类型  
  let targetType = 'param';
  if (targetAppServiceId || !isEmpty(instances)) {
    targetType = 'instance';
  }
  if (!isEmpty(endPoints)) {
    targetType = 'endPoints';
  }
  
  const appInstance = initTargetLabel({ targetLabelsDs, type: targetType, record: formDs.current, networkInfoDs, formatMessage });
  const externalIps = initPorts({ portDs, type, networkInfoDs });
  const targetIps = initEndPoints({ endPointsDs, targetLabelsDs, record: formDs.current, networkInfoDs });
  

  formDs.current.init('name', name);
  formDs.current.init('appServiceId', appServiceId);
  formDs.current.init('type', type);
  formDs.current.init('target', targetType);
  formDs.current.init('appInstance', appInstance);
  formDs.current.init('externalIps', externalIps);
  formDs.current.init('targetIps', targetIps);
}


function initTargetLabel({ targetLabelsDs, type, record, networkInfoDs, formatMessage }) {
  const { instances, targetAppServiceId, selectors } = networkInfoDs.current.get('target');
  if (type === 'instance') {
    if (targetAppServiceId) {
      // 如果存在targetAppServiceId那么 实例的值为所有实例
      return formatMessage({ id: 'all_instance' });
    } else if (instances && instances.length) {
      // 如果存在instances 且 有值 那么将appInstance设置为instances数组的第一项（暂且这么处理，后期要对旧数据做兼容处理）
      if (instances.length > 1) {
        return map(instances, (item) => item.code).join(',');
      }
      return instances[0].code;
    }
  } else {
    targetLabelsDs.reset();
    if (isEmpty(selectors)) {
      targetLabelsDs.create();
    } else {
      forEach(selectors, (value, keyword) => (
        targetLabelsDs.create({
          keyword,
          value,
        })
      ));
    }
    return null;
  }
}

function initPorts({ portDs, type, networkInfoDs }) {
  const { externalIps: ips, ports } = networkInfoDs.current.get('config');
  portDs.reset();
  if (type === networkInfoDs.current.get('type')) {
    forEach(ports, (item) => {
      portDs.create(item);
    });
  } else {
    portDs.create();
  }
  if (type === 'ClusterIP') {
    if (typeof externalIps === 'string') {
      return [ips];
    } else {
      return ips;
    }
  }
}

function initEndPoints({ endPointsDs, targetLabelsDs, record, networkInfoDs }) {
  const { endPoints } = networkInfoDs.current.get('target');
  if (!isEmpty(endPoints)) {
    endPointsDs.reset();
    targetLabelsDs.reset();
    record.init('appServiceId', null);
    record.init('appInstance', null);
    
    const ipstr = keys(endPoints)[0];
    if (ipstr) {
      const targetPortObj = endPoints[ipstr];
      forEach(targetPortObj, (item) => {
        endPointsDs.create({
          targetPort: item.port,
        });
      });
      return ipstr.split(',');
    }
  }
}

export const NetWorkStoreProvider = injectIntl(inject('AppState')(observer(StoreProvider)));
export default useNetWorkStore;
