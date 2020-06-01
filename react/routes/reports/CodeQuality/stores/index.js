import React, { createContext, useContext, useEffect, useMemo } from 'react';
import { inject } from 'mobx-react';
import { injectIntl } from 'react-intl';
import { DataSet } from 'choerodon-ui/pro';
import { withRouter } from 'react-router-dom';
import DetailDataSet from './DetailDataSet';
import AppOptionsDataSet from './AppOptionsDataSet';
import ChartsDataSet from './ChartsDataSet';
import { useReportsStore } from '../../stores';

const Store = createContext();

export function useCodeQualityStore() {
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
    const { appId, type } = state || {};
    const defaultObjectType = type || 'issue';
    const backPath = state && state.appId ? '/devops/code-management' : '/charts';

    const intlPrefix = 'c7ncd.codeQuality';

    const objectTypeDs = useMemo(() => new DataSet({
      data: [
        {
          text: formatMessage({ id: 'report.code-quality.type.issue' }),
          value: 'issue',
        },
        {
          text: formatMessage({ id: 'report.code-quality.type.coverage' }),
          value: 'coverage',
        },
        {
          text: formatMessage({ id: 'report.code-quality.type.duplicate' }),
          value: 'duplicate',
        },
      ],
      selection: 'single',
    }), []);

    const appServiceDs = useMemo(() => new DataSet(AppOptionsDataSet({ projectId })), [projectId]);
    const chartsDs = useMemo(() => new DataSet(ChartsDataSet({ projectId })), [projectId]);
    const detailDs = useMemo(() => new DataSet(DetailDataSet({ intlPrefix, formatMessage, appServiceDs, objectTypeDs, chartsDs, defaultObjectType })), [defaultObjectType]);

    useEffect(() => {
      loadData();
    }, []);

    async function loadData() {
      const appServiceList = await appServiceDs.query();
      ReportsStore.changeIsRefresh(false);
      if (appServiceList && appServiceList.length) {
        const { id } = appServiceList[0];
        const appServiceId = appId || id;
        const { getStartTime, getEndTime, getAppId } = ReportsStore;
        const startTime = getStartTime.format('YYYY-MM-DD HH:mm:ss');
        const endTime = getEndTime.format('YYYY-MM-DD HH:mm:ss');
        chartsDs.setQueryParameter('startTime', startTime);
        chartsDs.setQueryParameter('endTime', endTime);
        chartsDs.setQueryParameter('objectType', defaultObjectType);
        detailDs.current.set('appServiceId', appServiceId);
      } else {
        ReportsStore.judgeRole(['choerodon.code.project.develop.app-service.ps.create']);
      }
    }

    const value = {
      ...props,
      prefixCls: 'c7ncd-codeQuality',
      permissions: ['choerodon.code.project.operation.chart.ps.sonarqube'],
      intlPrefix,
      detailDs,
      appServiceDs,
      chartsDs,
      backPath,
    };
    return (
      <Store.Provider value={value}>
        {children}
      </Store.Provider>
    );
  },
)));
