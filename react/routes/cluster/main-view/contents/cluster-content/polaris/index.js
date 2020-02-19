import React, { Fragment, Suspense, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Spin, Button } from 'choerodon-ui';
import { useClusterMainStore } from '../../../stores';
import { useClusterContentStore } from '../stores';
import EmptyPage from '../../../../../../components/empty-page';
import NumberDetail from './number-detail';
import CollapseDetail from './collapse-detail';

import './index.less';

const polaris = observer((props) => {
  const {
    intlPrefix,
    prefixCls,
  } = useClusterMainStore();
  const {
    contentStore: {
      setTabKey,
    },
    formatMessage,
    tabs: {
      POLARIS_TAB,
    },
    ClusterDetailDs,
    contentStore,
  } = useClusterContentStore();

  function refresh() {

  }

  function getContent() {
    if (contentStore.getHasEnv) {
      return (
        <Fragment>
          <Button
            className={`${prefixCls}-polaris-wrap-btn`}
            type="primary"
            funcType="raised"
            //   onClick={handleScan}
            disabled={!(ClusterDetailDs.current && ClusterDetailDs.current.get('connect'))}
          >手动扫描</Button>
          <NumberDetail />
          <CollapseDetail />
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
