import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import SelectDataSet from './SelectDataSet';

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
      initIssue,
    } = props;

    const selectDs = useMemo(() => new DataSet(SelectDataSet({ projectId, issueId, formatMessage, appServiceId, objectVersionNumber, branchName }), [projectId]));

    useEffect(() => {
      issueId && selectDs.current.init('issue', initIssue);
    }, []);

    const value = {
      ...props,
      appServiceId,
      selectDs,
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
