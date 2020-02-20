import React, { Fragment, Suspense, useMemo, useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Button } from 'choerodon-ui/pro';
import { useClusterMainStore } from '../../../stores';
import { useClusterContentStore } from '../stores';
import EmptyPage from '../../../../../../components/empty-page';
import NumberDetail from './number-detail';
import CollapseDetail from './collapse-detail';
import { useClusterStore } from '../../../../stores';

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
    contentStore: {
      getTabKey,
    },
    formatMessage,
    tabs: {
      POLARIS_TAB,
    },
    ClusterDetailDs,
    contentStore,
    clusterSummaryDs,
    envDetailDs,
    polarisNumDS,
  } = useClusterContentStore();

  const [loading, setLoading] = useState(false);

  const statusLoading = useMemo(() => polarisNumDS.current && polarisNumDS.current.get('status') === 'operating', [polarisNumDS.current]);

  const scanFailed = useMemo(() => polarisNumDS.current && polarisNumDS.current.get('status') === 'failed', [polarisNumDS.current]);

  useEffect(() => {
    setLoading(false);
  }, [polarisNumDS.current]);

  function handleScan() {
    contentStore.ManualScan(projectId, id);
    setLoading(true);
  }

  function renderBtnStatus() {
    const connectStatus = ClusterDetailDs.current && ClusterDetailDs.current.get('connect');
    const isLoading = loading || statusLoading;
    if (connectStatus) {
      if (isLoading) return true;
      if (scanFailed) return false;
      return false;
    } else {
      return true;
    }
  }
  function getContent() {
    const isLoading = loading || statusLoading;
    if (contentStore.getHasEnv) {
      return (
        <Fragment>
          <Button
            className={`${prefixCls}-polaris-wrap-btn`}
            color="primary"
            funcType="raised"
            onClick={handleScan}
            disabled={renderBtnStatus()}
          >
            {formatMessage({ id: `${intlPrefix}.polaris.scanning` })}
          </Button>
          <NumberDetail isLoading={isLoading} />
          <CollapseDetail loading={loading} />
        </Fragment>
      );
    } else {
      return (
        <EmptyPage
          title={formatMessage({ id: 'empty.title.env' })}
          describe={formatMessage({ id: `${intlPrefix}.polaris.empty.des` })}
        />
      );
    }
  }

  return (
    <div className={`${prefixCls}-polaris-wrap`}>
      {getContent()}
    </div>
  );
});

export default polaris;
