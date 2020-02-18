import React, { Fragment, Suspense, useMemo } from 'react';
import { observer } from 'mobx-react-lite';
import { Spin } from 'choerodon-ui';
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
          <NumberDetail />
          <CollapseDetail />
        </Fragment>
      );
    } else {
      return (
        <EmptyPage
          title="暂无环境"
          describe="该集群下暂无任何环境"
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
