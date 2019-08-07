import React from 'react';
import { TabPage, Content, Permission, Breadcrumb } from '@choerodon/boot';
import { Table } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { useServiceDetailStore } from './stores';
import HeaderButtons from './HeaderButtons';

const { Column } = Table;

const Share = (props) => {
  const {
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    shareDs,
  } = useServiceDetailStore();

  return (
    <TabPage>
      <HeaderButtons>
        <Permission
          service={['devops-service.app-share-rule.createOrUpdate']}
        >
          <Button
            icon="playlist_add"
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
