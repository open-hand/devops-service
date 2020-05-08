import React, { Fragment, Suspense, useMemo, useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Button } from 'choerodon-ui/pro';
import { Choerodon } from '@choerodon/boot';
import { useClusterMainStore } from '../../../stores';
import { useClusterContentStore } from '../stores';
import EmptyPage from '../../../../../../components/empty-page';
import NumberDetail from './number-detail';
import CollapseDetail from './collapse-detail';
import { useClusterStore } from '../../../../stores';
import LoadingBar from '../../../../../../components/loading';
import { useInterval } from '../../../../../../components/costom-hooks';

import './index.less';

const polaris = observer((props) => {
  const {
    clusterStore: {
      getSelectedMenu: { id },
    },
  } = useClusterStore();
  const {
    intlPrefix,
    prefixCls,
  } = useClusterMainStore();
  const {
    AppState: { currentMenuType: { id: projectId } },
    formatMessage,
    ClusterDetailDs,
    contentStore,
    polarisNumDS,
    clusterSummaryDs,
    envDetailDs,
  } = useClusterContentStore();

  const [loading, setLoading] = useState(false);
  const [delay, setDelay] = useState(false);

  const statusLoading = useMemo(() => polarisNumDS.current && polarisNumDS.current.get('status') === 'operating', [polarisNumDS.current]);

  useEffect(() => {
    if (statusLoading) {
      setDelay(5000);
    } else {
      setDelay(false);
    }
  }, [statusLoading]);

  useEffect(() => {
    setLoading(false);
  }, [polarisNumDS.current]);

  function handleScan() {
    contentStore.ManualScan(projectId, id);
    setLoading(true);
    setDelay(5000);
  }

  async function loadData() {
    try {
      await polarisNumDS.query();
      if (polarisNumDS.current) {
        if (polarisNumDS.current.get('status') === 'operating') {
          setDelay(5000);
        } else {
          clusterSummaryDs.query();
          envDetailDs.query();
          setDelay(false);
        }
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      setDelay(false);
    }
  }


  function getContent() {
    const isLoading = loading || statusLoading;
    const connectStatus = ClusterDetailDs.current && ClusterDetailDs.current.get('connect');
    if (contentStore.getPolarisLoading) {
      return <LoadingBar display />;
    }
    if (contentStore.getHasEnv) {
      return (
        <Fragment>
          <Button
            className={`${prefixCls}-polaris-wrap-btn`}
            color="primary"
            funcType="raised"
            onClick={handleScan}
            disabled={isLoading || !connectStatus}
          >
            {formatMessage({ id: `${intlPrefix}.polaris.scanning` })}
          </Button>
          <NumberDetail isLoading={isLoading} />
          <CollapseDetail loading={isLoading} />
        </Fragment>
      );
    } else {
      return (
        <EmptyPage
          title={formatMessage({ id: 'empty.title.env' })}
          describe={formatMessage({ id: `${intlPrefix}.polaris.empty.des` })}
          access
        />
      );
    }
  }

  useInterval(loadData, delay);

  return (
    <div className={`${prefixCls}-polaris-wrap`}>
      {getContent()}
    </div>
  );
});

export default polaris;
