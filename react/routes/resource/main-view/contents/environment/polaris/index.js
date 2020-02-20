import React, { Fragment, Suspense, useMemo, useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Button } from 'choerodon-ui/pro';
import EmptyPage from '../../../../../../components/empty-page';
import NumberDetail from './number-detail';
import CollapseDetail from './collapse-detail';
import { useResourceStore } from '../../../../stores';
import { useEnvironmentStore } from '../stores';

import './index.less';

const polaris = observer((props) => {
  const {
    intl: { formatMessage },
    AppState: { currentMenuType: { id: projectId } },
    baseInfoDs,
    intlPrefix,
    polarisNumDS,
    envStore,
  } = useEnvironmentStore();
  const {
    resourceStore: { getSelectedMenu: { id } },
    prefixCls,
  } = useResourceStore();

  const [loading, setLoading] = useState(false);

  const statusLoading = useMemo(() => polarisNumDS.current && polarisNumDS.current.get('status') === 'operating', [polarisNumDS.current]);

  useEffect(() => {
    setLoading(false);
  }, [polarisNumDS.current]);

  function handleScan() {
    envStore.ManualScan(projectId, id);
    setLoading(true);
  }

  function renderBtnStatus() {
    const envStatus = baseInfoDs.current && baseInfoDs.current.get('connect');
    const isLoading = loading || statusLoading;
    if (envStatus) {
      if (isLoading) return true;
      return false;
    } else {
      return true;
    }
  }

  function getContent() {
    const isLoading = loading || statusLoading;
    if (envStore.getHasInstance) {
      return (
        <Fragment>
          <Button
            className={`${prefixCls}-polaris-wrap-btn`}
            color="primary"
            funcType="raised"
            onClick={handleScan}
            disabled={renderBtnStatus()}
          >
            {formatMessage({ id: 'c7ncd.cluster.polaris.scanning' })}
          </Button>
          <NumberDetail isLoading={isLoading} />
          <CollapseDetail loading={loading} />
        </Fragment>
      );
    } else {
      return (
        <EmptyPage
          title={formatMessage({ id: 'empty.title.instance' })}
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
