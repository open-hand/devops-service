import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import CreateDataSet from './BranchCreateDataSet';
import useStore from './useStore';

const Store = createContext();

export function useFormStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const {
      AppState: { currentMenuType: { id: projectId } },
      intl: { formatMessage },
      children,
      appServiceId,
      intlPrefix,
    } = props;

    const contentStore = useStore();
    const formDs = useMemo(() => new DataSet(CreateDataSet({ formatMessage, projectId, appServiceId, contentStore }), [projectId, appServiceId]));

    const value = {
      ...props,
      projectId,
      appServiceId,
      contentStore,
      formDs,
      intlPrefix,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
