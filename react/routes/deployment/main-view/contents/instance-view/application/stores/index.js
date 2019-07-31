import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import BaseInfoDataSet from './BaseInfoDataSet';
import MainStore from '../../../../stores';

const Store = createContext();

export default Store;

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      prefixCls,
      intlPrefix,
      store,
    } = useContext(MainStore);
    const { AppState: { currentMenuType: { id } }, children } = props;
    const baseInfoDs = useMemo(() => {
      const selectedMenu = store.getSelectedMenu;
      return new DataSet(BaseInfoDataSet(id, selectedMenu.menuId));
    }, [id, store.getSelectedMenu]);

    const value = {
      ...props,
      baseInfoDs,
      prefixCls,
      intlPrefix,
      store,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }
));
