import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import ListDataSet from './ListDataSet';
import AllProjectDataSet from './AllProjectDataSet';
import PermissionDataSet from './PermissionProjectDataSet';
import DetailDataSet from './DetailDataSet';
import OptionsDataSet from './OptionsDataSet';
import useStore from './useStore';

const Store = createContext();

export function useCertificateStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
    } = props;
    const intlPrefix = 'c7ncd.certificate';
    const listDs = useMemo(() => new DataSet(ListDataSet(intlPrefix, formatMessage, projectId)), [formatMessage, projectId]);
    const allProjectDs = useMemo(() => new DataSet(AllProjectDataSet(intlPrefix, formatMessage, projectId)), [projectId]);
    const permissionProjectDs = useMemo(() => new DataSet(PermissionDataSet(intlPrefix, formatMessage, projectId)), [projectId]);
    const detailDs = useMemo(() => new DataSet(DetailDataSet(intlPrefix, formatMessage)), []);
    const optionsDs = useMemo(() => new DataSet(OptionsDataSet()), []);

    const certStore = useStore();

    const value = {
      ...props,
      prefixCls: 'c7ncd-certificate',
      intlPrefix,
      listDs,
      allProjectDs,
      permissionProjectDs,
      detailDs,
      optionsDs,
      certStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
