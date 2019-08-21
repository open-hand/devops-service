import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import useStore from '../../stores/useStore';
import ListDataSet from '../../stores/ListDataSet';
import ImportDataSet from './ImportDataSet';
import ImportTableDataSet from './ImportTableDataSet';
import getTablePostData from '../../../../utils/getTablePostData';
import OptionsDataSet from '../../stores/OptionsDataSet';

const Store = createContext();

export function useAppServiceStore() {
  return useContext(Store);
}

export const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
    } = props;
    const intlPrefix = 'c7ncd.appService';
    const AppStore = useMemo(() => useStore(), []);
    const listDs = useMemo(() => new DataSet(ListDataSet(intlPrefix, formatMessage, projectId)), [formatMessage, projectId]);
    const importDs = useMemo(() => new DataSet(ImportDataSet(intlPrefix, formatMessage, projectId)), [formatMessage, projectId]);
    const importTableDs = useMemo(() => new DataSet(ImportTableDataSet(intlPrefix, formatMessage, projectId)), [formatMessage, projectId]);
    const versionOptions = useMemo(() => new DataSet(OptionsDataSet()), []);

    useEffect(() => {
      listDs.transport.read = ({ data }) => {
        const postData = getTablePostData(data);

        return {
          url: `/devops/v1/projects/${projectId}/app_service/page_by_options`,
          method: 'post',
          data: postData,
        };
      };
      listDs.query();
    }, [projectId]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-appService',
      intlPrefix,
      listDs,
      importDs,
      importTableDs,
      AppStore,
      versionOptions,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
