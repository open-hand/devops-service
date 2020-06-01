import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { withRouter } from 'react-router-dom';
import forEach from 'lodash/forEach';
import filter from 'lodash/filter';
import DetailDataSet from './DetailDataSet';
import AppOptionsDataSet from './AppOptionsDataSet';
import ChartsDataSet from './ChartsDataSet';
import { useReportsStore } from '../../stores';
import EnvOptionsDataSet from './EnvOptionsDataSet';
import TableDataSet from './TableDataSet';

const Store = createContext();

export function useDeployDurationStore() {
  return useContext(Store);
}

export const StoreProvider = withRouter(injectIntl(inject('AppState')(
  (props) => {
    const {
      AppState: { currentMenuType: { projectId } },
      intl: { formatMessage },
      children,
      location: { state, search },
    } = props;
    const {
      ReportsStore,
    } = useReportsStore();
    const backPath = state && state.appId ? '/devops/code-management' : '/charts';

    const envDs = useMemo(() => new DataSet(EnvOptionsDataSet({ projectId })), [projectId]);
    const appServiceDs = useMemo(() => new DataSet(AppOptionsDataSet({ projectId })), [projectId]);
    const tableDs = useMemo(() => new DataSet(TableDataSet({ formatMessage, projectId })), [projectId]);
    const chartsDs = useMemo(() => new DataSet(ChartsDataSet({ projectId })), [projectId]);
    const detailDs = useMemo(() => new DataSet(DetailDataSet({ formatMessage, appServiceDs, envDs, chartsDs, tableDs })), []);

    useEffect(() => {
      loadData();
    }, []);

    async function loadData() {
      const envs = await envDs.query();
      const envList = filter(envs, ['permission', true]);
      ReportsStore.changeIsRefresh(false);
      if (envList && envList.length) {
        const { id } = envList[0];
        const { getStartTime, getEndTime } = ReportsStore;
        const startTime = getStartTime.format('YYYY-MM-DD HH:mm:ss');
        const endTime = getEndTime.format('YYYY-MM-DD HH:mm:ss');
        forEach([chartsDs, tableDs], (ds) => {
          ds.setQueryParameter('envId', id);
          ds.setQueryParameter('startTime', startTime);
          ds.setQueryParameter('endTime', endTime);
        });
        appServiceDs.setQueryParameter('envId', id);
        detailDs.current.init('envId', id);
        const appServiceList = await appServiceDs.query();
        if (appServiceList && appServiceList.length) {
          const { id: appServiceId } = appServiceList[0];
          detailDs.current.set('appServiceIds', [appServiceId]);
        }
      } else {
        ReportsStore.judgeRole(['choerodon.code.project.deploy.environment.ps.detail-create-env']);
      }
    }

    const value = {
      ...props,
      permissions: ['choerodon.code.project.operation.chart.ps.deploy.duration'],
      detailDs,
      appServiceDs,
      chartsDs,
      backPath,
      envDs,
      tableDs,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
)));
