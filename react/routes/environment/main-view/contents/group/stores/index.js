import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { useEnvironmentStore } from '../../../../stores';
import TableDataSet from './TableDataSet';

const Store = createContext();

export function useEnvGroupStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const { intl: { formatMessage }, children, AppState: { currentMenuType: { id: projectId } } } = props;
    const {
      intlPrefix,
      envStore: {
        getSelectedMenu: { id },
      },
    } = useEnvironmentStore();
    const groupDs = useMemo(() => new DataSet(TableDataSet({ projectId, id, formatMessage, intlPrefix })), [id, projectId]);

    // useEffect(() => {
    //   // detailDs.transport.read.url=
    // }, []);

    const value = {
      ...props,
      groupDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
