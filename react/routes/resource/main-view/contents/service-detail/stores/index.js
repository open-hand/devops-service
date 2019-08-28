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
    const { AppState: { currentMenuType: { id } }, children } = props;
    const {
      intlPrefix,
      intl: { formatMessage },
      resourceStore: { getSelectedMenu: { menuId } },
    } = useResourceStore();
    const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet()), []);

    useEffect(() => {
      baseInfoDs.transport.read.url = `/devops/v1/projects/${id}/service/${menuId}`;
      baseInfoDs.query();
    }, [id, menuId]);

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
