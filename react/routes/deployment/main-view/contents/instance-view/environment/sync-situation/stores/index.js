import React, { createContext, useMemo, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import EnvStore from '../../stores';
import TableDataSet from './TableDataSet';
import GitopsLogDataSet from './GitopsLogDataSet';
import RetryDataSet from './RetryDataSet';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { id } }, children, intl } = props;
    const {
      prefixCls,
      intlPrefix,
      store: {
        getSelectedMenu: { menuId },
      },
    } = useContext(EnvStore);

    const tableDs = useMemo(() => new DataSet(TableDataSet({
      intl,
      intlPrefix,
      projectId: id,
      envId: menuId,
    })), [id, intl, intlPrefix, menuId]);
    const logDs = useMemo(() => new DataSet(GitopsLogDataSet(id, menuId)), [id, menuId]);
    const retryDs = useMemo(() => new DataSet(RetryDataSet(id, menuId)), [id, menuId]);

    const value = {
      ...props,
      tableDs,
      logDs,
      retryDs,
      prefixCls,
      intlPrefix,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
