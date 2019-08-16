import React from 'react';
import { TabPage, Content, Permission, Breadcrumb } from '@choerodon/master';
import { Table, Modal } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { useServiceDetailStore } from './stores';
import HeaderButtons from './HeaderButtons';
import ShareRule from './modals/share-rule';

const { Column } = Table;

const modalKey1 = Modal.key();

const modalStyle = {
  width: '26%',
};
const Share = (props) => {
  const {
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    shareDs,
  } = useServiceDetailStore();

  function openDetail() {
    const detailModal = Modal.open({
      key: modalKey1,
      title: formatMessage({ id: `${intlPrefix}.share.rule.add` }),
      children: <ShareRule intlPrefix={intlPrefix} prefixCls={prefixCls} formatMessage={formatMessage} />,
      drawer: true,
      style: modalStyle,
      okText: formatMessage({ id: 'save' }),
    });
  }

  return (
    <TabPage>
      <HeaderButtons>
        <Permission
          service={['devops-service.app-share-rule.createOrUpdate']}
        >
          <Button
            icon="playlist_add"
            onClick={openDetail}
          >
            <FormattedMessage id={`${intlPrefix}.share.rule.add`} />
          </Button>
        </Permission>
      </HeaderButtons>
      <Breadcrumb title="服务详情" />
      <Content>
        <Table dataSet={shareDs}>
          <Column name="versionType" />
          <Column name="version" />
          <Column name="shareLevel" />
        </Table>
      </Content>
    </TabPage>
  );
};

export default Share;
