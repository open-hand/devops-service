import React, { createContext, useContext, useState, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';

import optionsDataSet from './optionsDataSet';
import selectDataSet from './selectDataSet';

const Store = createContext();

export function useSelectStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  observer((props) => {
    const {
      AppState: { currentMenuType: { id: projectId } },
      intl: { formatMessage },
      issueId,
      branchName,
      appServiceId,
      objectVersionNumber,
      children,
      intlPrefix,
    } = props;

    const optionsDs = useMemo(() => new DataSet(optionsDataSet({ projectId, issueId }), [projectId]));
    const selectDs = useMemo(() => new DataSet(selectDataSet({ projectId, optionsDs, formatMessage, appServiceId, objectVersionNumber, branchName }), [projectId]));

    const value = {
      ...props,
      appServiceId,
      selectDs,
      optionsDs,
      formatMessage,
      intlPrefix,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  })
));
