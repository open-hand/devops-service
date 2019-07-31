import React, { createContext, useMemo, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import TreeDataSet from './TreeDataSet';
import MainStore from '../../stores';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const { store } = useContext(MainStore);
    const { AppState: { currentMenuType: { id } }, children } = props;
    const treeDs = useMemo(() => new DataSet(TreeDataSet(id, store)), [id, store]);

    const value = {
      ...props,
      treeDs,
      store,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
