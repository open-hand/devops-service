import React from 'react';
import { PageWrap, PageTab, Page } from '@choerodon/boot';
import { observer } from 'mobx-react-lite';
import { useAppTopStore } from '../stores';
import { useServiceDetailStore } from './stores';
import Version from './Version';
import Allocation from './Allocation';
import Share from './Share';
import Tips from '../../../components/new-tips';

const DetailContent = observer(() => {
  const {
    intlPrefix,
    detailPermissions,
    appServiceStore,
  } = useAppTopStore();
  const {
    intl: { formatMessage },
    detailDs,
    access: {
      accessPermission,
      accessShare,
    },
  } = useServiceDetailStore();

  return (<Page
    service={detailPermissions}
  >
    <PageWrap noHeader={[]} cache>
      <PageTab
        title={formatMessage({ id: `${intlPrefix}.version` })}
        tabKey="Version"
        component={Version}
        alwaysShow
      />
      {/* <PageTab */}
      {/*  title={<Tips */}
      {/*    helpText={formatMessage({ id: `${intlPrefix}.detail.permission.tips` })} */}
      {/*    title={formatMessage({ id: `${intlPrefix}.permission` })} */}
      {/*  />} */}
      {/*  tabKey="Allocation" */}
      {/*  component={Allocation} */}
      {/*  alwaysShow={accessPermission} */}
      {/* /> */}
      <PageTab
        title={<Tips
          helpText={formatMessage({ id: `${intlPrefix}.detail.share.tips` })}
          title={formatMessage({ id: `${intlPrefix}.share` })}
        />}
        tabKey="Share"
        component={Share}
        alwaysShow={accessShare && detailDs.current && detailDs.current.get('type') === 'normal'}
      />
    </PageWrap>
  </Page>);
});

export default DetailContent;
