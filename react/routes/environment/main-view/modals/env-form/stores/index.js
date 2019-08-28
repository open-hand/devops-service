import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import EnvFormDataSet from './EnvFormDataSet';
import ClusterOptionDataSet from './ClusterOptionDataSet';
import GroupOptionDataSet from './GroupOptionDataSet';

const Store = createContext();

export function useEnvFormStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { id: projectId } },
      intl: { formatMessage },
      children,
      intlPrefix,
    } = props;
    const formDs = useMemo(() => new DataSet(EnvFormDataSet({ formatMessage, intlPrefix, projectId })), [projectId]);
    const clusterOptionDs = useMemo(() => new DataSet(ClusterOptionDataSet(projectId)), [projectId]);
    const groupOptionDs = useMemo(() => new DataSet(GroupOptionDataSet(projectId)), [projectId]);

    const value = {
      ...props,
      formDs,
      intlPrefix,
      clusterOptionDs,
      groupOptionDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }
));
