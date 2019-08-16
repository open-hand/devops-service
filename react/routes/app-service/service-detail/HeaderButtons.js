import React, { memo } from 'react';
import { FormattedMessage } from 'react-intl';
import { Header, Permission } from '@choerodon/master';
import { Button } from 'choerodon-ui';
import { Modal } from 'choerodon-ui/pro';
import { observer } from 'mobx-react-lite';
import { useServiceDetailStore } from './stores';
import Detail from './modals/detail';


const modalKey1 = Modal.key();
const modalStyle = {
  width: '26%',
};


const HeaderButtons = observer(({ children }) => {
  const {
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    detailDs,
  } = useServiceDetailStore();

  function openDetail() {
    const detailModal = Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.detail` }),
      children: <Detail record={detailDs.current} intlPrefix={intlPrefix} prefixCls={prefixCls} formatMessage={formatMessage} />,
      drawer: true,
      style: modalStyle,
      footer: (
        <Button funcType="raised" type="primary" onClick={() => detailModal.close()}>
          <FormattedMessage id="close" />
        </Button>
      ),
    });
  }


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
          onClick={openDetail}
        >
          <FormattedMessage id={`${intlPrefix}.detail`} />
        </Button>
      </Permission>
    </Header>
  );
});

export default HeaderButtons;
