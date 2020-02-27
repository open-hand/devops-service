import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { DataSet } from 'choerodon-ui/pro';
import BatchDetailDataSet from './BatchDetailDataSet';

const Store = createContext();

export function useBatchDetailStore() {
  return useContext(Store);
}

export const StoreProvider = withRouter(injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
      intlPrefix,
      recordId,
    } = props;

    const batchDetailDs = useMemo(() => new DataSet(BatchDetailDataSet(projectId, recordId)), [projectId, recordId]);

    const value = {
      ...props,
      batchDetailDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
)));
