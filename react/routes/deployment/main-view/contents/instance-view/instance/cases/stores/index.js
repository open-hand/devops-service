import React, { createContext, useMemo, useContext } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import InstanceContext from '../../stores';
import CasesDataSet from './CasesDataSet';

const CasesContext = createContext();

export default CasesContext;

export function StoreProvider(props) {
  const { children } = props;
  const {
    AppState: { currentMenuType: { id } },
    store,
  } = useContext(InstanceContext);
  const casesDataSet = useMemo(() => {
    const { menuId } = store.getSelectedMenu;
    return new DataSet(CasesDataSet(id, menuId));
  }, [id, store.getSelectedMenu]);
  const value = {
    ...useContext(InstanceContext),
    ...props,
    casesDataSet,
  };
  return (
    <CasesContext.Provider value={value}>
      {children}
    </CasesContext.Provider>
  );
}
