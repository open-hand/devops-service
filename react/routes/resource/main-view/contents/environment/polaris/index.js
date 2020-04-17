import React, { Fragment, Suspense, useMemo, useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Button, Spin } from 'choerodon-ui/pro';
import { Choerodon } from '@choerodon/boot';
import EmptyPage from '../../../../../../components/empty-page';
import NumberDetail from './number-detail';
import CollapseDetail from './collapse-detail';
import { useResourceStore } from '../../../../stores';
import { useEnvironmentStore } from '../stores';
import Loading from '../../../../../../components/loading';
import { useInterval } from '../../../../../../components/costom-hooks';

import './index.less';

const polaris = observer((props) => {
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { id: projectId } },
    baseInfoDs,
    intlPrefix,
    polarisNumDS,
    envStore,
    istSummaryDs,
  } = useEnvironmentStore();
  const {
    resourceStore: { getSelectedMenu: { id } },
    prefixCls,
  } = useResourceStore();

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
    envStore.ManualScan(projectId, id);
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
          istSummaryDs.query();
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
    const envStatus = baseInfoDs.current && baseInfoDs.current.get('connect');
    if (envStore.getPolarisLoading) {
      return <Loading display />;
    }
    if (envStore.getHasInstance) {
      return (
        <Fragment>
          <Button
            className={`${prefixCls}-polaris-wrap-btn`}
            color="primary"
            funcType="raised"
            onClick={handleScan}
            disabled={isLoading || !envStatus}
          >
            {formatMessage({ id: 'c7ncd.cluster.polaris.scanning' })}
          </Button>
          <NumberDetail isLoading={isLoading} />
          <CollapseDetail loading={isLoading} />
        </Fragment>
      );
    } else {
      return (
        <EmptyPage
          title={formatMessage({ id: 'empty.title.instance' })}
          describe={formatMessage({ id: `${intlPrefix}.polaris.empty.des` })}
          access
        />
      );
    }
  }

  useInterval(loadData, delay);

  if (polarisNumDS.status === 'sync') {
    return <Spin />;
  }

  return (
    <div className={`${prefixCls}-polaris-wrap`}>
      {getContent()}
    </div>
  );
});

export default polaris;
