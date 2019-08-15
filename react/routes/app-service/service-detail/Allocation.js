import React from 'react';
import { TabPage, Content, Permission, Breadcrumb } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';
import { useServiceDetailStore } from './stores';
import HeaderButtons from './HeaderButtons';
import TimePopover from '../../../components/timePopover/TimePopover';

const { Column } = Table;

const Allocation = (props) => {
  const {
    intl: { formatMessage },
    intlPrefix,
    prefixCls,
    permissionDs,
  } = useServiceDetailStore();

  function renderTime({ value }) {
    return <TimePopover content={value} />;
  }

  return (
    <TabPage>
      <HeaderButtons>
        <Permission
          service={['devops-service.app-service.updatePermission']}
        >
          <Button
            icon="authority"
          >
            <FormattedMessage id={`${intlPrefix}.permission.manage`} />
          </Button>
        </Permission>
      </HeaderButtons>
      <Breadcrumb title="服务详情" />
      <Content>
        <Table dataSet={permissionDs}>
          <Column name="realName" />
          <Column name="loginName" />
          <Column name="role" />
          <Column name="creationDate" renderer={renderTime} />
        </Table>
      </Content>
    </TabPage>
  );
};

export default Allocation;
