import React, { Fragment } from 'react';
import { PageWrap, PageTab, Page, Breadcrumb, Content } from '@choerodon/master';
import { observer } from 'mobx-react-lite';
import Loading from '../../../components/loading';
import EmptyPage from '../../../components/empty-page';
import { useAppTopStore } from '../stores';
import { useServiceDetailStore } from './stores';
import Version from './Version';
import Allocation from './Allocation';
import Share from './Share';

const DetailContent = observer(() => {
  const {
    listDs,
    detailPermissions,
  } = useAppTopStore();
  const {
    intl: { formatMessage },
    intlPrefix,
    AppStore,
    detailDs,
  } = useServiceDetailStore();

  function getContent() {
    if (listDs.status === 'loading') return <Loading display />;

    return listDs.length ? <PageWrap noHeader={[]} cache>
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
    </PageWrap> : <Fragment>
      <Breadcrumb />
      <Content>
        <EmptyPage
          title={formatMessage({ id: 'empty.title.app' })}
          describe={formatMessage({ id: 'empty.tips.app.owner' })}
        />
      </Content>
    </Fragment>;
  }

  return (<Page
    service={detailPermissions}
  >
    {getContent()}
  </Page>);
});

export default DetailContent;
