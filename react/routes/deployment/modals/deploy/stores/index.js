import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import ManualDeployDataSet from './ManualDeployDataSet';
import OptionsDataSet from '../../../stores/OptionsDataSet';

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
    } = props;

    const envOptionsDs = useMemo(() => new DataSet(OptionsDataSet()), []);
    const valueIdOptionsDs = useMemo(() => new DataSet(OptionsDataSet()), []);
    const versionOptionsDs = useMemo(() => new DataSet(OptionsDataSet()), []);
    const manualDeployDs = useMemo(() => new DataSet(ManualDeployDataSet(intlPrefix, formatMessage, projectId, envOptionsDs, valueIdOptionsDs, versionOptionsDs, deployStore)), [projectId]);

    useEffect(() => {
      envOptionsDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/list_by_active?active=true`;
      envOptionsDs.query();
    }, [projectId]);

    const value = {
      ...props,
      manualDeployDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
