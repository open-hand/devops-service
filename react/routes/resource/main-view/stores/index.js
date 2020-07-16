import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';

import { DataSet } from 'choerodon-ui/pro';
import useStore from './useStore';
import useNetworkStore from './useNetworkStore';
import useCustomStore from './useCustomStore';
import useIngressStore from './useIngressStore';
import useConfigMapStore from './useConfigMapStore';
import useSecretStore from './useSecretStore';
import useChildrenContextStore from './useChildrenContextStore';
import useCertStore from './useCertStore';
import BaseInfoDataSet from './BaseInfoDataSet';
import { useResourceStore } from '../../stores';

const Store = createContext();

export function useMainStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(observer(
  (props) => {
    const { AppState: { currentMenuType: { projectId } }, children } = props;
    const {
      resourceStore: {
        getSelectedMenu: {
          parentId, key,
        },
      },
    } = useResourceStore();
    const mainStore = useStore();
    const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet()), []);
    const networkStore = useNetworkStore();
    const customStore = useCustomStore();
    const ingressStore = useIngressStore();
    const configMapStore = useConfigMapStore();
    const secretStore = useSecretStore();
    const certStore = useCertStore();
    const childrenStore = useChildrenContextStore();
    
    useEffect(() => {
      // 此处的key是TreeDataSet里的formatInstance中的key值
      // 这个key的规则是每一级别的节点的id属性 然后用 '-' 相连接 例如：’523-1080-21‘
      // 这里只取第一级别的key值，作为环境id
      if (key && key.indexOf('**') < 0) {
        baseInfoDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/${key}/info`;
        baseInfoDs.query();
      }
    }, [projectId, key]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-deployment',
      intlPrefix: 'c7ncd.deployment',
      podColor: {
        RUNNING_COLOR: '#0bc2a8',
        PADDING_COLOR: '#fbb100',
      },
      mainStore,
      networkStore,
      customStore,
      ingressStore,
      configMapStore,
      secretStore,
      childrenStore,
      certStore,
      baseInfoDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
)));
