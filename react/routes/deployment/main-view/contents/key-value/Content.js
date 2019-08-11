import React, { useMemo, useState, useCallback } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/boot';
import {
  Button,
  Popover,
} from 'choerodon-ui';
import { Table } from 'choerodon-ui/pro';
import MouserOverWrapper from '../../../../../components/MouseOverWrapper/MouserOverWrapper';
import StatusTags from '../../../../../components/StatusTags';
import TimePopover from '../../../../../components/timePopover/TimePopover';
import { useDeploymentStore } from '../../../stores';
import { useKeyValueStore } from './stores';
import Modals from './modals';

import './index.less';

const { Column } = Table;

const ConfigMap = observer((props) => {
  const {
    prefixCls,
    intlPrefix,
    deploymentStore: { getSelectedMenu: { parentId } },
  } = useDeploymentStore();
  const {
    intl: { formatMessage },
    listDs,
    itemType,
    permissions,
  } = useKeyValueStore();

  function refresh() {
    return listDs.query();
  }

  function renderName({ value, record }) {
    const commandStatus = record.get('commandStatus');
    return (
      <div>
        <StatusTags
          name={formatMessage({ id: commandStatus || 'null' })}
          colorCode={commandStatus || 'success'}
          style={{ minWidth: 40, marginRight: '0.08rem' }}
        />
        <span>{value}</span>
      </div>
    );
  }

  function renderKey({ value = [] }) {
    return (
      <MouserOverWrapper width={0.5}>
        {value.join(',')}
      </MouserOverWrapper>
    );
  }

  function renderDate({ value }) {
    return <TimePopover content={value} />;
  }

  function renderAction() {
    const buttons = [
      {
        service: permissions.edit,
        text: formatMessage({ id: 'edit' }),
        // action: handleEdit,
      },
      {
        service: permissions.delete,
        text: formatMessage({ id: 'delete' }),
        action: handleDelete,
      },
    ];
    return <Action data={buttons} />;
  }

  function handleDelete() {
    listDs.delete(listDs.current);
  }

  return (
    <div className={`${prefixCls}-keyValue-table`}>
      <Modals />
      <Table
        dataSet={listDs}
        border={false}
        queryBar="bar"
      >
        <Column name="name" header={formatMessage({ id: `${intlPrefix}.${itemType}` })} renderer={renderName} />
        <Column renderer={renderAction} width="0.7rem" />
        <Column name="key" renderer={renderKey} />
        <Column name="lastUpdateDate" renderer={renderDate} />
      </Table>
    </div>
  );
});

export default ConfigMap;
