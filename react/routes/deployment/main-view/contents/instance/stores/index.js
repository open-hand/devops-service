import React, { createContext, useMemo, useContext } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import BaseInfoDataSet from './BaseInfoDataSet';
import MainStore from '../../../stores';

const InstanceContext = createContext();

export default InstanceContext;

export const StoreProvider = (props) => {
  const {
    AppState: { currentMenuType: { id } },
    store,
  } = useContext(MainStore);

  const { children } = props;
  const baseInfoDs = useMemo(() => {
    const selectedMenu = store.getSelectedMenu;
    return new DataSet(BaseInfoDataSet(id, selectedMenu.menuId));
  }, [id, store.getSelectedMenu]);

  const value = {
    ...props,
    ...useContext(MainStore),
    baseInfoDs,
    store,
  };
  return (
    <InstanceContext.Provider value={value}>
      {children}
    </InstanceContext.Provider>
  );
};
