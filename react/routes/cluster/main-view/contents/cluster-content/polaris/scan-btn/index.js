import React, { Fragment, Suspense, useMemo, useState, useEffect } from 'react';
import { observer } from 'mobx-react-lite';
import { Spin, Button, Icon } from 'choerodon-ui';

import { useClusterMainStore } from '../../../../stores';
import { useClusterContentStore } from '../../stores';

const ScanBtn = observer((props) => {
  const {
    contentStore: {
      setTabKey,
    },
    formatMessage,
    tabs: {
      POLARIS_TAB,
    },
    ClusterDetailDs,
    polarisNumDS,
  } = useClusterContentStore();

  return (
    <Button
      type="primary"
      funcType="raised"
      style={{
        width: '.92rem',
        boShadow: '0px 2px 4px 0px rgba(106,117,203,0.6)',
        borderRadius: '6px',
      }}
    //   onClick={handleScan}
    >手动扫描</Button>
  );
});

export default ScanBtn;
