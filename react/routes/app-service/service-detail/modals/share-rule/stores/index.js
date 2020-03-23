import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import ShareDataSet from './FormDataSet';

const Store = createContext();

export function useShareFormStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
      intlPrefix,
      shareId,
      appServiceId,
    } = props;
    const formDs = useMemo(() => new DataSet(ShareDataSet({ intlPrefix, formatMessage, projectId, appServiceId, shareId })), [projectId, appServiceId, shareId]);

    useEffect(() => {
      if (shareId) {
        formDs.query();
      } else {
        formDs.create();
      }
    }, []);

    const value = {
      ...props,
      VERSION_TYPE: ['master', 'feature', 'hotfix', 'bugfix', 'release'],
      formDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
