import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { forEach, isEmpty, map } from 'lodash';
import { DataSet } from 'choerodon-ui/pro';
import CreateFormDataSet, { transFormData } from './CreateFormDataSet';
import PortDataSet from './PortDataSet';
import AppInstanceOptionsDataSet from './AppInstanceOptionsDataSet';
import KeyOptionsDataSet from './KeyOptionsDataSet';
import TargetLabelsDataSet from './TargetLabelsDataSet';
import NetworkInfoDataSet from './NetworkInfoDataSet';

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
    appId,
    networkStore,
    networkId,
  } = props;


  const appInstanceOptionsDs = useMemo(() => new DataSet(AppInstanceOptionsDataSet(projectId, envId, appId, formatMessage)), [projectId, envId, appId]);
  const keyOptionsDs = useMemo(() => new DataSet(KeyOptionsDataSet(projectId, envId, appId)), [projectId, envId, appId]);

  const portDs = useMemo(() => new DataSet(PortDataSet({ formatMessage, projectId, envId, appId })), [projectId, envId, appId]);
  const targetLabelsDs = useMemo(() => new DataSet(TargetLabelsDataSet({ formatMessage, keyOptionsDs })), [keyOptionsDs]);

  const networkInfoDs = useMemo(() => new DataSet(NetworkInfoDataSet(projectId, networkId)), [networkId]);

  const formDs = useMemo(() => new DataSet(CreateFormDataSet({ formatMessage, portDs, targetLabelsDs, appInstanceOptionsDs, networkStore, projectId, envId, appId, networkEdit: { networkInfoDs, networkId, initTargetLabel, initPorts } })), [projectId, envId, appId]);
  

  useEffect(() => {
    if (networkId && formDs.current) {
      formDs.transport.create = ({ data: [data] }) => ({
        method: 'put',
        url: `/devops/v1/projects/${projectId}/service/${networkId}`,
        data: transFormData(data, formatMessage, appId, envId),
      });
      networkInfoDs.query().then(res => {
        const { type, target, target: { instances, targetAppServiceId } } = res;
        loadInfo({ data: res, formatMessage, targetLabelsDs, portDs, formDs, networkInfoDs });
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
    }
  }, [networkId, formDs.current]);
  
  useEffect(() => {
    if (!networkId) {
      portDs.create();
      targetLabelsDs.create();
    }
  }, []);

  const value = {
    ...props,
    formDs,
    portDs,
    targetLabelsDs,
    appInstanceOptionsDs,
    keyOptionsDs,
    networkInfoDs,
    networkId,
  };

  return (
    <NetWorkStore.Provider value={value}>
      {children}
    </NetWorkStore.Provider>
  );
}

function loadInfo({ data, formatMessage, targetLabelsDs, portDs, formDs, networkInfoDs }) {
  const { name, id, type, target, config } = data;
  const { instances, targetAppServiceId, selectors, endPoints } = target;

  // 判断目标对象类型  
  let targetType = 'param';
  if (targetAppServiceId || (instances && instances.length)) {
    targetType = 'instance';
  }
  
  const appInstance = initTargetLabel({ targetLabelsDs, type: targetType, record: formDs.current, networkInfoDs, formatMessage });
  const externalIps = initPorts({ portDs, type, networkInfoDs });

  formDs.current.init('name', name);
  formDs.current.init('type', type);
  formDs.current.init('target', targetType);
  formDs.current.init('appInstance', appInstance);
  formDs.current.init('externalIps', externalIps);
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


export const NetWorkStoreProvider = injectIntl(inject('AppState')(StoreProvider));
export default useNetWorkStore;
