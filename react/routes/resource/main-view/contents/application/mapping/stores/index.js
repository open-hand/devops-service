import React, { createContext, useContext, useMemo } from 'react';
import { DataSet } from 'choerodon-ui/pro';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import TableDataSet from './TableDataSet';
import { useResourceStore } from '../../../../../stores';
import { useApplicationStore } from '../../stores';

const Store = createContext();

export default Store;

export function useMappingStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      resourceStore: { getSelectedMenu: { parentId, menuId } },
      intlPrefix,
    } = useResourceStore();
    const {
      mappingStore,
      cipherStore,
    } = useApplicationStore();
    const {
      AppState: { currentMenuType: { id } },
      intl: { formatMessage },
      value: { type },
      children,
    } = props;

    const tableDs = useMemo(() => new DataSet(TableDataSet({
      formatMessage,
      intlPrefix,
      type,
      projectId: id,
      envId: parentId,
      appId: menuId,
    })), [formatMessage, id, intlPrefix, menuId, parentId, type]);
    const formStore = type === 'mapping' ? mappingStore : cipherStore;
    const value = {
      ...props,
      tableDs,
      formStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  }
));
