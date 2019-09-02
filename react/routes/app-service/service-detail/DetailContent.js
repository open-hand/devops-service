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

  function renderTabContent() {
    let content;
    if (AppStore.getProjectRole === 'owner') {
      if (detailDs.current && detailDs.current.get('type') === 'normal') {
        content = (
          <PageWrap noHeader={[]}>
            <PageTab title={formatMessage({ id: `${intlPrefix}.version` })} tabKey="version" component={Version} />
            <PageTab title={formatMessage({ id: `${intlPrefix}.permission` })} tabKey="permission" component={Allocation} />
            <PageTab title={formatMessage({ id: `${intlPrefix}.share` })} tabKey="share" component={Share} />
          </PageWrap>
        );
      } else {
        content = (
          <PageWrap noHeader={[]}>
            <PageTab title={formatMessage({ id: `${intlPrefix}.version` })} tabKey="version" component={Version} />
            <PageTab title={formatMessage({ id: `${intlPrefix}.permission` })} tabKey="permission" component={Allocation} />
          </PageWrap>
        );
      }
    } else {
      content = (
        <PageWrap noHeader={[]}>
          <PageTab title={formatMessage({ id: `${intlPrefix}.version` })} tabKey="version" component={Version} />
        </PageWrap>
      );
    }

    return content;
  }
  return renderTabContent();
});

export default DetailContent;
