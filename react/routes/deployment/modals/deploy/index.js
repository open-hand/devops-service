import React, { Fragment, useEffect } from 'react';
import { Table } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';
import UserInfo from '../../../../components/userInfo';

const { Column } = Table;

export default injectIntl(observer(({ intlPrefix, prefixCls, modal }) => {
  modal.handleOk(async () => {
    // as
  });

  return (
    <div className={`${prefixCls}-process-wrap`}>
      手动部署
    </div>
  );
}));
