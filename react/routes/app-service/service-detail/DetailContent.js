import React, { useCallback, Fragment } from 'react';
import { PageWrap, PageTab } from '@choerodon/master';
import { observer } from 'mobx-react-lite';
import { useServiceDetailStore } from './stores';
import Version from './Version';
import Allocation from './Allocation';
import Share from './Share';

const DetailContent = observer((props) => {
  const {
    intl: { formatMessage },
    intlPrefix,
    AppStore,
    detailDs,
  } = useServiceDetailStore();

  return (
    <PageWrap noHeader={[]} cache>
      <PageTab
        title={formatMessage({ id: `${intlPrefix}.version` })}
        tabKey="choerodon.code.develop.app-service.version"
        component={Version}
      />
      <PageTab
        title={formatMessage({ id: `${intlPrefix}.permission` })}
        tabKey="choerodon.code.develop.app-service.permission"
        component={Allocation}
      />
      <PageTab
        title={formatMessage({ id: `${intlPrefix}.share` })}
        tabKey="choerodon.code.develop.app-service.share"
        component={Share}
      />
    </PageWrap>
  );
});

export default DetailContent;
