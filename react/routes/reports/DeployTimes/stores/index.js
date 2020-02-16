import React, { createContext, useContext, useMemo } from 'react';
import { injectIntl } from 'react-intl';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { DataSet } from 'choerodon-ui/pro';
import deployTimesSelectDataSet from './DeployTimesSelectDataSet';
import deployTimesTableDataSet from './DeployTimeTableDataSet';

const Store = createContext();

export function useDeployTimesStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(
  inject(
    'AppState'
  )(
    observer((props) => {
      const {
        intl: { formatMessage },
        children,
        AppState: {
          currentMenuType: {
            projectId,
          },
        },
      } = props;

      const DeployTimesSelectDataSet = useMemo(() => new DataSet(deployTimesSelectDataSet({ formatMessage })), []);
      const DeployTimesTableDataSet = useMemo(() => new DataSet(deployTimesTableDataSet({ projectId, formatMessage })), [projectId]);

      const value = {
        ...props,
        DeployTimesSelectDataSet,
        DeployTimesTableDataSet,
      };

      return (
        <Store.Provider value={value}>
          {children}
        </Store.Provider>
      );
    })
  )
);
