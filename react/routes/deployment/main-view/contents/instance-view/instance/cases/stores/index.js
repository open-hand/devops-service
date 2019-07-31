import React, { createContext, useMemo, useContext } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import Store from '../../../../../../stores';
import CasesDataSet from './CasesDataSet';

const CasesContext = createContext();

export default CasesContext;

export function StoreProvider(props) {
  const { children } = props;
  const {
    selectedMenu: { menuId },
    AppState: { currentMenuType: { id } },
  } = useContext(Store);
  const casesDataSet = useMemo(() => new DataSet(CasesDataSet(id, menuId)), [id, menuId]);
  const value = {
    ...useContext(Store),
    ...props,
    casesDataSet,
  };
  return (
    <CasesContext.Provider value={value}>
      {children}
    </CasesContext.Provider>
  );
}
