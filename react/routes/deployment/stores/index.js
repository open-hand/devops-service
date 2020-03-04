import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { withRouter } from 'react-router-dom';

import map from 'lodash/map';
import ListDataSet from './ListDataSet';
import DetailDataSet from './DetailDataSet';
import useStore from './useStore';
import usePipelineStore from './usePipelineStore';
import OptionsDataSet from './OptionsDataSet';

const Store = createContext();
const STATUS = ['success', 'failed', 'deleted', 'pendingcheck', 'stop', 'running'];

export function useDeployStore() {
  return useContext(Store);
}

export const StoreProvider = withRouter(injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
      location: { state },
    } = props;
    const intlPrefix = 'c7ncd.deploy';
    const deployTypeDs = useMemo(() => new DataSet({
      data: [
        {
          text: formatMessage({ id: `${intlPrefix}.auto` }),
          value: 'auto',
        },
        {
          text: formatMessage({ id: `${intlPrefix}.manual` }),
          value: 'manual',
        },
        {
          text: formatMessage({ id: `${intlPrefix}.batch` }),
          value: 'batch',
        },
      ],
      selection: 'single',
    }), []);
    const deployResultDs = useMemo(() => new DataSet({
      data: map(STATUS, item => ({
        text: formatMessage({ id: `${intlPrefix}.status.${item}` }),
        value: item,
      })),
      selection: 'single',
    }), []);

    const deployStore = useStore();
    const pipelineStore = usePipelineStore();

    const envOptionsDs = useMemo(() => new DataSet(OptionsDataSet()), []);
    const pipelineOptionsDs = useMemo(() => new DataSet(OptionsDataSet()), []);

    const listDs = useMemo(() => new DataSet(ListDataSet(intlPrefix, formatMessage, projectId, envOptionsDs, deployTypeDs, deployResultDs, pipelineOptionsDs)), [projectId]);
    const detailDs = useMemo(() => new DataSet(DetailDataSet()), []);

    useEffect(() => {
      if (state && state.pipelineId) {
        listDs.queryDataSet.getField('pipelineId').set('defaultValue', state.pipelineId);
      }
    }, []);

    useEffect(() => {
      envOptionsDs.transport.read.url = `/devops/v1/projects/${projectId}/envs/list_by_active?active=true`;
      pipelineOptionsDs.transport.read.url = `/devops/v1/projects/${projectId}/pipeline/list_all`;
      envOptionsDs.query();
      pipelineOptionsDs.query();
    }, [projectId]);

    const value = {
      ...props,
      prefixCls: 'c7ncd-deploy',
      permissions: [
        'devops-service.devops-deploy-record.pageByOptions',
        'devops-service.app-service-instance.deploy',
        'devops-service.pipeline.batchExecute',
        'devops-service.pipeline.audit',
        'devops-service.pipeline.retry',
        'devops-service.pipeline.failed',
        'devops-service.app-service-instance.batchDeployment',
      ],
      intlPrefix,
      listDs,
      detailDs,
      deployStore,
      pipelineStore,
      envOptionsDs,
      pipelineOptionsDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
)));
