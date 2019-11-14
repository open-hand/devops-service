import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import isEmpty from 'lodash/isEmpty';
import FormDataSet from './FormDataSet';
import useStore from './useStore';

const Store = createContext();

export function useEditAppServiceStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
      intlPrefix,
      appServiceId,
    } = props;

    const store = useStore();
    const formDs = useMemo(() => new DataSet(FormDataSet({ intlPrefix, formatMessage, projectId, appServiceId })), [projectId, appServiceId]);

    useEffect(() => {
      appServiceId && loadData();
    }, [appServiceId]);

    async function loadData() {
      const res = await formDs.query();
      if (res) {
        const record = formDs.current;
        record.set('oldName', res.name);
        if (!isEmpty(res.chart)) {
          record.set('chartType', 'custom');
        } else {
          record.set('chartType', 'default');
        }
        if (!isEmpty(res.harbor)) {
          record.set('harborType', 'custom');
        } else {
          record.set('harborType', 'default');
        }
      }
    }

    const value = {
      ...props,
      formDs,
      store,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
