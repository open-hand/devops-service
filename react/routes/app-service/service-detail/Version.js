import React, { useMemo } from 'react';
import {
  TabPage, Content, Breadcrumb, Permission,
} from '@choerodon/boot';
import {
  Table, Tooltip, Button,
} from 'choerodon-ui/pro';
import { withRouter } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { useAppTopStore } from '../stores';
import { useServiceDetailStore } from './stores';
import HeaderButtons from './HeaderButtons';
import TimePopover from '../../../components/timePopover/TimePopover';

import './index.less';

const { Column } = Table;

const Version = withRouter(observer((props) => {
  const { prefixCls, intlPrefix } = useAppTopStore();
  const {
    detailDs,
    versionDs,
    intl: { formatMessage },
  } = useServiceDetailStore();

  const selectedRecordLength = useMemo(
    () => versionDs.selected && versionDs.selected.length, [versionDs.selected],
  );

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
    return '';
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

  function handleDelete() {
    const selectedRecords = versionDs.selected;
    const version = selectedRecords[0] ? selectedRecords[0].get('version') : '';
    const modalProps = {
      title: formatMessage({ id: `${intlPrefix}.version.delete.title` }),
      children: selectedRecords.length > 1
        ? formatMessage({ id: `${intlPrefix}.version.delete.des` }, { version, length: selectedRecordLength })
        : formatMessage({ id: `${intlPrefix}.version.delete.des.single` }, { version }),
      okText: formatMessage({ id: 'delete' }),
      okProps: { color: 'red' },
      cancelProps: { color: 'dark' },
    };
    versionDs.delete(selectedRecords, modalProps);
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
            className={`${prefixCls}-detail-content-version-btn`}
          >
            权限管理
          </Button>
        </Permission>
        <Permission
          service={[]}
        >
          <Tooltip title={selectedRecordLength ? '' : '请在下方列表中选择服务版本'}>
            <Button
              icon="delete"
              onClick={handleDelete}
              disabled={!selectedRecordLength}
            >
              {formatMessage({ id: `${intlPrefix}.version.delete` })}
            </Button>
          </Tooltip>
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
}));

export default Version;
