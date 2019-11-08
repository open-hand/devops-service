import React, { createContext, useContext, useState, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import CreateDataSet from './branchCreateDataSet';
import issueNameDataSet from './issueNameDataSet';
import useStore from './useStore';


const Store = createContext();

export function useFormStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const {
      AppState: { currentMenuType: { id: projectId } },
      intl: { formatMessage },
      children,
      appServiceId,
      intlPrefix,
    } = props;

    const contentStore = useStore();
    const selectedApp = appServiceId;
    const issueNameOptionDs = useMemo(() => new DataSet(issueNameDataSet({ projectId }), [projectId]));
    const formDs = useMemo(() => new DataSet(CreateDataSet({ formatMessage, issueNameOptionDs, projectId, appServiceId, contentStore }), [projectId]));
    const value = {
      ...props,
      projectId,
      appServiceId,
      contentStore,
      issueNameOptionDs,
      formDs,
      intlPrefix,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
