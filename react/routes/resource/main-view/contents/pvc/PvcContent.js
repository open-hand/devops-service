import React, { useMemo, useState, useCallback } from 'react';
import { observer } from 'mobx-react-lite';
import { Action } from '@choerodon/boot';
import { Modal, Table } from 'choerodon-ui/pro';
import { keys } from 'lodash';
import MouserOverWrapper from '../../../../../components/MouseOverWrapper/MouserOverWrapper';
import StatusTags from '../../../../../components/status-tag';
import TimePopover from '../../../../../components/timePopover/TimePopover';
import { useResourceStore } from '../../../stores';
// // import { useKeyValueStore } from './stores';
// // import Modals from './modals';
// import KeyValueModal from '../application/modals/key-value';
import { useMainStore } from '../../stores';
import { usePVCStore } from './stores';
import Modals from './modals';
import ClickText from '../../../../../components/click-text';
import ResourceListTitle from '../../components/resource-list-title';

import './index.less';

const { Column } = Table;
const modalKey = Modal.key();
const modalStyle = {
  width: 'calc(100vw - 3.52rem)',
};

const ConfigMap = observer((props) => {
  const {
    prefixCls,
    intlPrefix,
    resourceStore: { getSelectedMenu: { parentId } },
    treeDs,
  } = useResourceStore();

  const { mainStore: { openDeleteModal } } = useMainStore();

  const {
    PVCtableDS,
    intl: { formatMessage },
  } = usePVCStore();

  function refresh() {

  }

  function getEnvIsNotRunning() {

  }


  function openModal() {

  }

  function renderAction() {

  }
  function renderName() {

  }
  function renderValue() {

  }
  return (
    <div className={`${prefixCls}-keyValue-PVC`}>
      <Modals />
      <ResourceListTitle type="pvcs" />
      <Table
        dataSet={PVCtableDS}
        border={false}
        queryBar="bar"
      >
        <Column name="name" header={formatMessage({ id: `${intlPrefix}.cipher` })} renderer={renderName} />
        <Column renderer={renderAction} width="0.7rem" />
        <Column name="status" renderer={renderValue} header={formatMessage({ id: 'status' })} />
        {/* <Column name="lastUpdateDate" renderer={renderDate} width="1rem" /> */}
      </Table>
    </div>
  );
});

export default ConfigMap;
