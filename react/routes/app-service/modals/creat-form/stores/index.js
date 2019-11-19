import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import FormDataSet from './FormDataSet';
import useStore from './useStore';

const Store = createContext();

export function useCreateAppServiceStore() {
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

    const store = useStore();

    const sourceDs = useMemo(() => new DataSet({
      data: [
        {
          text: formatMessage({ id: `${intlPrefix}.source.project` }),
          value: 'normal_service',
        },
        {
          text: formatMessage({ id: `${intlPrefix}.source.organization` }),
          value: 'share_service',
        },
      ],
      selection: 'single',
    }), []);

    const formDs = useMemo(() => new DataSet(FormDataSet({ intlPrefix, formatMessage, projectId, sourceDs, store })), [projectId]);

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
