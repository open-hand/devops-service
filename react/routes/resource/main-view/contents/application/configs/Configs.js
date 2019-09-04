import React, { useMemo, useState } from 'react';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/master';
import { Table } from 'choerodon-ui/pro';
import MouserOverWrapper from '../../../../../../components/MouseOverWrapper/MouserOverWrapper';
import StatusTags from '../../../../../../components/status-tag';
import TimePopover from '../../../../../../components/timePopover/TimePopover';
import KeyValueModal from '../modals/key-value';
import { useResourceStore } from '../../../../stores';
import { useApplicationStore } from '../stores';
import { useConfigsStore } from './stores';

import './index.less';

const { Column } = Table;

const Configs = observer(() => {
  const {
    intl: { formatMessage },
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { id, parentId } },
  } = useResourceStore();
  const {
    appStore: {
      getTabKey: type,
    },
  } = useApplicationStore();
  const {
    tableDs,
    formStore,
  } = useConfigsStore();
  const statusStyle = useMemo(() => ({ marginRight: '0.08rem' }), []);

  const [showModal, setShowModal] = useState(false);
  const [recordId, setRecordId] = useState(null);

  function refresh() {
    return tableDs.query();
  }

  function closeSideBar(fresh) {
    setRecordId(null);
    setShowModal(false);
    fresh && refresh();
  }

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
          style={statusStyle}
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

  function renderAction({ record }) {
    const buttons = [
      {
        service: [],
        text: formatMessage({ id: 'delete' }),
        action: () => {
          tableDs.delete(record);
        },
      },
    ];
    return <Action data={buttons} />;
  }

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
        appId={id}
        onClose={closeSideBar}
        store={formStore}
      />}
    </div>
  );
});

export default Configs;
