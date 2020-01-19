import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import AllProjectDataSet from './AllProjectDataSet';
import PermissionDataSet from './PermissionProjectDataSet';
import DetailDataSet from './DetailDataSet';
import OptionsDataSet from './OptionsDataSet';

const Store = createContext();

export function useCertPermissionStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
      intlPrefix,
      certId,
    } = props;
    const allProjectDs = useMemo(() => new DataSet(AllProjectDataSet({ intlPrefix, formatMessage, projectId })), [projectId]);
    const detailDs = useMemo(() => new DataSet(DetailDataSet({ intlPrefix, formatMessage, projectId, certId })), [projectId, certId]);
    const permissionProjectDs = useMemo(() => new DataSet(PermissionDataSet({ intlPrefix, formatMessage, projectId, detailDs, certId })), [projectId, detailDs, certId]);
    const optionsDs = useMemo(() => new DataSet(OptionsDataSet({ projectId, certId })), [projectId, certId]);

    const value = {
      ...props,
      allProjectDs,
      permissionProjectDs,
      detailDs,
      optionsDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
