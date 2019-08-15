import React, { useCallback, Fragment } from 'react';
import { PageWrap, PageTab, Header, Permission, Action, Breadcrumb } from '@choerodon/master';
import { Table, Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { withRouter, Link } from 'react-router-dom';
import { useServiceDetailStore } from './stores';
import Version from './Version';
import Allocation from './Allocation';
import Share from './Share';

const DetailContent = (props) => {
  const {
    intl: { formatMessage },
    intlPrefix,
  } = useServiceDetailStore();
  return (
    <PageWrap noHeader={[]}>
      <PageTab title={formatMessage({ id: `${intlPrefix}.version` })} tabKey="version" component={Version} />
      <PageTab title={formatMessage({ id: `${intlPrefix}.permission` })} tabKey="permission" component={Allocation} />
      <PageTab title={formatMessage({ id: `${intlPrefix}.share` })} tabKey="share" component={Share} />
    </PageWrap>
  );
};

export default DetailContent;
