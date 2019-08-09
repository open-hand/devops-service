import React, { useMemo, useState, useCallback } from 'react';
import { FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/boot';
import {
  Button,
  Popover,
} from 'choerodon-ui';
import { Table } from 'choerodon-ui/pro';
import MouserOverWrapper from '../../../../../../components/MouseOverWrapper/MouserOverWrapper';
import StatusTags from '../../../../../../components/StatusTags';
import { useDeploymentStore } from '../../../../stores';
import { useMappingStore } from './stores';
import TimePopover from '../../../../../../components/timePopover/TimePopover';
import KeyValueModal from '../modals/key-value';

import './index.less';

const { Column } = Table;

const ConfigMap = observer((props) => {
  const {
    prefixCls,
    intlPrefix,
    deploymentStore: { getSelectedMenu: { menuId, parentId } },
  } = useDeploymentStore();
  const {
    intl: { formatMessage },
    tableDs,
    formStore,
    value: { type },
  } = useMappingStore();

  const [showModal, setShowModal] = useState(false);
  const [recordId, setRecordId] = useState(null);

  function refresh() {
    return tableDs.query();
  }

  const closeSideBar = useCallback((isLoad) => {
    setRecordId(null);
    setShowModal(false);
    isLoad && refresh();
  });

  function handleEdit(record) {
    setRecordId(record.get('id'));
    setShowModal(true);
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
        <a className="content-table-name" onClick={() => handleEdit(record)}>{value}</a>
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

  const renderAction = useCallback(({ record }) => {
    const handleDelete = () => tableDs.delete(record);
    const buttons = [
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        action: () => handleDelete(record),
      },
    ];
    return <Action data={buttons} />;
  }, [formatMessage, tableDs]);
  
  return (
    <div className={`${prefixCls}-mapping-content`}>
      <Table
        dataSet={tableDs}
        border={false}
        queryBar="bar"
      >
        <Column name="name" header={formatMessage({ id: `${intlPrefix}.application.tabs.${type}` })} renderer={renderName} />
        <Column renderer={renderAction} />
        <Column name="key" renderer={renderKey} />
        <Column name="lastUpdateDate" renderer={renderDate} />
      </Table>
      {showModal && <KeyValueModal
        modeSwitch={type === 'mapping'}
        title={type === 'mapping' ? 'configMap' : 'secret'}
        visible={showModal}
        id={recordId}
        envId={parentId}
        appId={menuId}
        onClose={closeSideBar}
        store={formStore}
      />}
    </div>
  );
});

export default ConfigMap;
