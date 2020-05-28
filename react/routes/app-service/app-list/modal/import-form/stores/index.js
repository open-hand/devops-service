import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import ImportDataSet from './ImportDataSet';
import ImportTableDataSet from './ImportTableDataSet';
import selectedDataSet from './SelectedDataSet';
import useStore from './useStore';

const Store = createContext();

export function useImportAppServiceStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
      intlPrefix,
    } = props;

    const serviceTypeDs = useMemo(() => new DataSet({
      data: [
        {
          text: formatMessage({ id: `${intlPrefix}.type.normal` }),
          value: 'normal',
        },
        {
          text: formatMessage({ id: `${intlPrefix}.type.test` }),
          value: 'test',
        },
      ],
      selection: 'single',
    }), []);
    const importStore = useStore();
    const importTableDs = useMemo(() => new DataSet(ImportTableDataSet({ intlPrefix, formatMessage, projectId })), [projectId]);
    const selectedDs = useMemo(() => new DataSet(selectedDataSet({ intlPrefix, formatMessage, projectId, importStore })), [projectId]);
    const importDs = useMemo(() => new DataSet(ImportDataSet({ intlPrefix, formatMessage, projectId, serviceTypeDs, selectedDs, importTableDs })), [projectId]);

    const value = {
      ...props,
      IMPORT_METHOD: ['share', 'github', 'gitlab'],
      importDs,
      importTableDs,
      selectedDs,
      importStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
