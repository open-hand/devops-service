import React, { createContext, useMemo, useContext } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import InstanceContext from '../../stores';
import TableDataSet from './TableDataSet';

const PodDetailsContext = createContext();

export default PodDetailsContext;

export function StoreProvider(props) {
  const { children } = props;
  const {
    intl,
    intlPrefix,

    AppState: { currentMenuType: { id } },
    store,
  } = useContext(InstanceContext);
  const tableDs = useMemo(() => {
    const { menuId, parentId } = store.getSelectedMenu;
    const [envId, appId] = parentId.split('-');
    return new DataSet(TableDataSet({ intl, intlPrefix, projectId: id, envId, appId, istId: menuId }));
  }, [id, intl, intlPrefix, store.getSelectedMenu]);
  const value = {
    ...useContext(PodDetailsContext),
    ...props,
    tableDs,
  };
  return (
    <PodDetailsContext.Provider value={value}>
      {children}
    </PodDetailsContext.Provider>
  );
}
