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
        tabKey="Version"
        component={Version}
        alwaysShow
      />
      <PageTab
        title={formatMessage({ id: `${intlPrefix}.permission` })}
        tabKey="Allocation"
        component={Allocation}
        alwaysShow={AppStore.getProjectRole === 'owner'}
      />
      <PageTab
        title={formatMessage({ id: `${intlPrefix}.share` })}
        tabKey="Share"
        component={Share}
        alwaysShow={AppStore.getProjectRole === 'owner' && detailDs.current && detailDs.current.get('type') === 'normal'}
      />
    </PageWrap>
  );
});

export default DetailContent;
