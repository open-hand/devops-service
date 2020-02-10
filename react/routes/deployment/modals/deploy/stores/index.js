import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import ManualDeployDataSet from './ManualDeployDataSet';
import OptionsDataSet from '../../../stores/OptionsDataSet';
import NetworkDataSet from './NetworkDataSet';
import PortDataSet from './PortDataSet';

const Store = createContext();

export function useManualDeployStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
      intlPrefix,
      deployStore,
      envId,
    } = props;

    const envOptionsDs = useMemo(() => new DataSet(OptionsDataSet()), []);
    const valueIdOptionsDs = useMemo(() => new DataSet(OptionsDataSet()), []);
    const versionOptionsDs = useMemo(() => new DataSet(OptionsDataSet()), []);
    const manualDeployDs = useMemo(() => new DataSet(ManualDeployDataSet(intlPrefix, formatMessage, projectId, envOptionsDs, valueIdOptionsDs, versionOptionsDs, deployStore)), [projectId]);

    const portsDs = useMemo(() => new DataSet(PortDataSet({ formatMessage })), []);
    const networkDs = useMemo(() => new DataSet(NetworkDataSet({ formatMessage, projectId, portsDs })), []);

    useEffect(() => {
      envOptionsDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/list_by_active?active=true`;
      envOptionsDs.query();
    }, [projectId]);

    const value = {
      ...props,
      manualDeployDs,
      portsDs,
      networkDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
