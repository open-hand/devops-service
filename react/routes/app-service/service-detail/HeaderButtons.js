import React, { memo } from 'react';
import { FormattedMessage } from 'react-intl';
import { Header, Permission } from '@choerodon/boot';
import { Button } from 'choerodon-ui';
import { useServiceDetailStore } from './stores';

const HeaderButtons = ({ children }) => {
  const {
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
  } = useServiceDetailStore();

  return (
    <Header>
      <Permission
        service={['devops-service.app-service.update']}
      >
        <Button
          icon="mode_edit "
        >
          <FormattedMessage id={`${intlPrefix}.edit`} />
        </Button>
      </Permission>
      <Permission
        service={['devops-service.app-service.updateActive']}
      >
        <Button
          icon="remove_circle_outline"
        >
          <FormattedMessage id={`${intlPrefix}.disable`} />
        </Button>
      </Permission>
      {children}
      <Permission
        service={['devops-service.app-service.query']}
      >
        <Button
          icon="find_in_page"
        >
          <FormattedMessage id={`${intlPrefix}.detail`} />
        </Button>
      </Permission>
    </Header>
  );
};

export default HeaderButtons;
