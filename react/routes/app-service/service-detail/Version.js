import React from 'react';
import { TabPage, Content, Breadcrumb, Permission } from '@choerodon/boot';
import { Table } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { withRouter } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import { useAppTopStore } from '../stores';
import { useServiceDetailStore } from './stores';
import HeaderButtons from './HeaderButtons';
import TimePopover from '../../../components/timePopover/TimePopover';

import './index.less';

const { Column } = Table;

const Version = withRouter((props) => {
  const { prefixCls } = useAppTopStore();
  const {
    detailDs,
    versionDs,
  } = useServiceDetailStore();

  function refresh() {
    versionDs.query();
  }

  function renderTime({ value }) {
    return <TimePopover content={value} />;
  }

  function getTitle() {
    if (detailDs.current) {
      return detailDs.current.get('name');
    }
  }

  function openDetail(appServiceIds) {
    const {
      history,
      location,
    } = props;
    history.push(`/rducm/code-lib-management/assign${location.search}&appServiceIds=${appServiceIds}`);
    // Modal.open({
    //   key: modalKey1,
    //   title: <Tips
    //     helpText={formatMessage({ id: `${intlPrefix}.detail.allocation.tips` })}
    //     title={formatMessage({ id: `${intlPrefix}.permission.manage` })}
    //   />,
    //   children: <ServicePermission
    //     dataSet={permissionDs}
    //     baseDs={detailDs}
    //     store={appServiceStore}
    //     nonePermissionDs={nonePermissionDs}
    //     intlPrefix="c7ncd.deployment"
    //     prefixCls={prefixCls}
    //     formatMessage={formatMessage}
    //     projectId={id}
    //     refresh={refresh}
    //   />,
    //   drawer: true,
    //   style: modalStyle,
    //   okText: formatMessage({ id: 'save' }),
    // });
  }

  return (
    <TabPage
      service={[]}
    >
      <HeaderButtons>
        <Permission
          service={['choerodon.code.project.develop.app-service.ps.permission.update']}
        >
          <Button
            icon="authority"
            onClick={() => openDetail(props.match.params.id)}
          >
            权限管理
          </Button>
        </Permission>
        <Button
          icon="refresh"
          onClick={refresh}
        >
          <FormattedMessage id="refresh" />
        </Button>
      </HeaderButtons>
      <Breadcrumb title={getTitle()} />
      <Content className={`${prefixCls}-detail-content`}>
        <Table dataSet={versionDs}>
          <Column name="version" sortable />
          <Column name="creationDate" renderer={renderTime} sortable />
        </Table>
      </Content>
    </TabPage>
  );
});

export default Version;
