import React, { useMemo } from 'react';
import {
  TabPage, Content, Breadcrumb, Permission,
} from '@choerodon/boot';
import {
  Table, Tooltip, Button, CheckBox,
} from 'choerodon-ui/pro';
import { withRouter } from 'react-router-dom';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import filter from 'lodash/filter';
import { useAppTopStore } from '../stores';
import { useServiceDetailStore } from './stores';
import HeaderButtons from './HeaderButtons';
import TimePopover from '../../../components/timePopover/TimePopover';
import Tips from '../../../components/new-tips';

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

  function renderCheckboxHeader() {
    const indeterminate = versionDs.some((record) => record.selectable && record.isSelected);
    const isSelectAll = !versionDs.some((record) => record.selectable && !record.isSelected);
    return (
      <CheckBox
        checked={isSelectAll && indeterminate}
        indeterminate={!isSelectAll && indeterminate}
        onChange={(value) => {
          versionDs.forEach((record) => {
            if (record.selectable) {
              // eslint-disable-next-line no-param-reassign
              record.isSelected = value;
            }
          });
        }}
      />
    );
  }

  function renderCheckbox({ record }) {
    return (
      <Tooltip
        title={!record.selectable ? formatMessage({ id: `${intlPrefix}.version.tips` }) : ''}
        placement="top"
      >
        <CheckBox
          checked={record.isSelected}
          disabled={!record.selectable}
          onChange={(value) => {
            // eslint-disable-next-line no-param-reassign
            record.isSelected = value;
          }}
        />
      </Tooltip>
    );
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
          service={['choerodon.code.project.develop.app-service.ps.version.delete']}
        >
          <Tooltip title={selectedRecordLength ? '' : formatMessage({ id: `${intlPrefix}.version.delete.disable` })}>
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
      <Content className={`${prefixCls}-detail-content ${prefixCls}-detail-content-version`}>
        <Table dataSet={versionDs}>
          <Column header={renderCheckboxHeader} renderer={renderCheckbox} width={50} />
          <Column name="version" sortable />
          <Column name="creationDate" renderer={renderTime} sortable />
        </Table>
      </Content>
    </TabPage>
  );
}));

export default Version;
