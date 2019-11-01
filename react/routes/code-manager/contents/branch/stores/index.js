import React, { createContext, useContext, useState, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';

import tableDataset from './tableDataSet';

const Store = createContext();

export function useTableStore() {
  return useContext(Store);
}
export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const {
      AppState: { currentMenuType: { id: projectId } },
      intl: { formatMessage },
      children,
      intlPrefix,
    } = props;

    const tableDs = useMemo(() => new DataSet(tableDataset({ projectId, formatMessage }), [projectId]));

    const value = {
      ...props,
      projectId,
      formatMessage,
      intlPrefix,
      tableDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
