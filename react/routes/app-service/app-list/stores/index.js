import React, { createContext, useContext, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import useStore from '../../stores/useStore';
import { useAppTopStore } from '../../stores';
import ImportDataSet from './ImportDataSet';
import ImportTableDataSet from './ImportTableDataSet';
import selectedDataSet from './SelectedDataSet';

const Store = createContext();

export function useAppServiceStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
    } = props;
    const { intlPrefix } = useAppTopStore();
    const AppStore = useStore();
    const importTableDs = useMemo(() => new DataSet(ImportTableDataSet(intlPrefix, formatMessage, projectId)), [formatMessage, projectId]);
    const selectedDs = useMemo(() => new DataSet(selectedDataSet(intlPrefix, formatMessage, projectId)), [projectId]);
    const importDs = useMemo(() => new DataSet(ImportDataSet(intlPrefix, formatMessage, projectId, selectedDs)), [formatMessage, projectId, selectedDs]);

    const value = {
      ...props,
      importDs,
      importTableDs,
      AppStore,
      selectedDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
