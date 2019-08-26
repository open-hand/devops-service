import React, { createContext, useMemo, useContext, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import BaseInfoDataSet from './BaseInfoDataSet';
import ResourceCountDataSet from './ResourceCountDataSet';
import { useResourceStore } from '../../../../stores';
import TableDataSet from './TableDataSet';

const Store = createContext();

export function useREStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { AppState: { currentMenuType: { id } }, intl: { formatMessage }, children } = props;
    const { resourceStore: { getSelectedMenu: { menuId } }, intlPrefix } = useResourceStore();
    const baseInfoDs = useMemo(() => new DataSet(BaseInfoDataSet()), []);
    const resourceCountDs = useMemo(() => new DataSet(ResourceCountDataSet()), []);
    const tableDs = useMemo(() => new DataSet(TableDataSet(formatMessage, intlPrefix)), []);

    useEffect(() => {
      baseInfoDs.transport.read.url = `/devops/v1/projects/${id}/envs/${menuId}/info`;
      baseInfoDs.query();
      resourceCountDs.transport.read.url = `/devops/v1/projects/${id}/envs/${menuId}/resource_count`;
      resourceCountDs.query();
    }, [id, menuId]);

    const value = {
      ...props,
      baseInfoDs,
      resourceCountDs,
      tableDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
