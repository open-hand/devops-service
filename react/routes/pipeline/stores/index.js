import React, { createContext, useContext, useMemo, useEffect } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import PipelineTableDataSet from './PipelineTableDataSet';
import OptionsDataSet from './OptionsDataSet';
import useStore from './useStore';

const Store = createContext();
export function usePiplineStore() {
  return useContext(Store);
}

const StoreProvider = injectIntl(inject('AppState')(
  (props) => {
    const {
      children,
      AppState: { currentMenuType: { id: organizationId, projectName, projectId } },
      intl: { formatMessage },
    } = props;
    const PiplineStore = useStore();

    const searchDS = useMemo(() => new DataSet({
      data: [
        {
          text: formatMessage({ id: 'pipeline.search.creator' }),
          value: 'creator',
        },
        {
          text: formatMessage({ id: 'pipeline.search.executor' }),
          value: 'executor',
        },
        {
          text: formatMessage({ id: 'pipeline.search.manager' }),
          value: 'manager',
        },
      ],
      selection: 'multiple',
    }));

    const envIdDS = useMemo(() => new DataSet(OptionsDataSet()), []);

    const triggerTypeDs = useMemo(() => new DataSet({
      data: [
        {
          text: formatMessage({ id: 'c7ncd.deploy.trigger.auto' }),
          value: 'auto',
        },
        {
          text: formatMessage({ id: 'c7ncd.deploy.trigger.manual' }),
          value: 'manual',
        },
      ],
      selection: 'single',
    }));
    const piplineDS = useMemo(() => new DataSet(PipelineTableDataSet(formatMessage, PiplineStore, projectId, searchDS, envIdDS, triggerTypeDs)), []);
   

    useEffect(() => {
      envIdDS.transport.read.url = `/devops/v1/projects/${projectId}/envs/list_by_active?active=true`;
      envIdDS.query();
    }, [projectId]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-pipeline',
      piplineDS, // 表格的dataset
      PiplineStore,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
));

export default StoreProvider;
