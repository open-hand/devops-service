import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import TableDataSet from './TableDataSet';
import { useResourceStore } from '../../../../stores';

const Store = createContext();

export function usePVCStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { AppState: { currentMenuType: { projectId } }, children } = props;
    const {
      intl: { formatMessage },
      resourceStore: { getSelectedMenu: { parentId } },
      intlPrefix,
    } = useResourceStore();

    const tableDs = useMemo(() => new DataSet(TableDataSet({ formatMessage, intlPrefix, projectId, envId: parentId })), [projectId, parentId]);

    const value = {
      ...props,
      tableDs,
    };

    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
