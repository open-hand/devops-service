import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { useAppTopStore } from '../../stores';
import ImportDataSet from './ImportDataSet';
import ImportTableDataSet from './ImportTableDataSet';
import selectedDataSet from './SelectedDataSet';
import ListDataSet from '../../stores/ListDataSet';
import getTablePostData from '../../../../utils/getTablePostData';

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
    const { intlPrefix } = useAppTopStore();
    const importTableDs = useMemo(() => new DataSet(ImportTableDataSet(intlPrefix, formatMessage, projectId)), [formatMessage, projectId]);
    const selectedDs = useMemo(() => new DataSet(selectedDataSet(intlPrefix, formatMessage, projectId)), [projectId]);
    const importDs = useMemo(() => new DataSet(ImportDataSet(intlPrefix, formatMessage, projectId, selectedDs)), [formatMessage, projectId, selectedDs]);
    const listDs = useMemo(() => new DataSet(ListDataSet(intlPrefix, formatMessage, projectId, 'list')), [projectId]);

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
      importDs,
      importTableDs,
      selectedDs,
      listDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));
