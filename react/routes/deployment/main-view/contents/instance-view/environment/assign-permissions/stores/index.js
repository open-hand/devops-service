import React, { createContext, useMemo, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import TableDataSet from './TableDataSet';
import EnvStore from '../../stores';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { AppState: { currentMenuType: { id } }, children, intl } = props;
    const {
      prefixCls,
      intlPrefix,
      store: { getSelectedMenu },
    } = useContext(EnvStore);
    const tableDs = useMemo(() => new DataSet(TableDataSet({
      intl,
      intlPrefix,
      projectId: id,
      envId: getSelectedMenu.menuId,
    })), [getSelectedMenu.menuId, id, intl, intlPrefix]);

    const value = {
      ...props,
      tableDs,
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
