import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { observer } from 'mobx-react-lite';
import BaseInfoDataSet from './BaseInfoDataSet';
import { useResourceStore } from '../../../../stores';

const Store = createContext();

export function useNetworkDetailStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { AppState: { currentMenuType: { id: projectId } }, children } = props;
    const {
      resourceStore: { getSelectedMenu: { id } },
    } = useResourceStore();
    const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet()), []);

    useEffect(() => {
      baseInfoDs.transport.read.url = `/devops/v1/projects/${projectId}/service/${id}`;
      baseInfoDs.query();
    }, [id, projectId]);

    const value = {
      ...props,
      baseInfoDs,
    };

    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
