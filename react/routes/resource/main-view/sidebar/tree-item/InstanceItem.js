import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
import { Modal } from 'choerodon-ui/pro';
import eventStopProp from '../../../../../utils/eventStopProp';
import PodCircle from '../../components/pod-circle';
import { useResourceStore } from '../../../stores';
import { useTreeItemStore } from './stores';
import { handlePromptError } from '../../../../../utils';

const stopKey = Modal.key();
const deleteKey = Modal.key();

function InstanceItem({
  record,
  name,
  podColor: {
    RUNNING_COLOR,
    PADDING_COLOR,
  },
  intlPrefix,
  intl: { formatMessage },
}) {
  const {
    treeDs,
    AppState: { currentMenuType: { id } },
  } = useResourceStore();
  const { treeItemStore } = useTreeItemStore();

  const podRunningCount = record.get('podRunningCount');
  const podCount = record.get('podCount');
  const podUnlinkCount = podCount - podRunningCount;
  const podData = useMemo(() => [{
    name: 'running',
    value: podRunningCount,
    stroke: RUNNING_COLOR,
  }, {
    name: 'unlink',
    value: podUnlinkCount,
    stroke: PADDING_COLOR,
  }], [podUnlinkCount, podRunningCount]);

  async function deleteItem() {
    try {
      const result = await treeItemStore.deleteInstance(id, record.get('id'));
      if (handlePromptError(result, false)) {
        treeDs.query();
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
    }
  }

  function openDeleteModal() {
    Modal.open({
      movable: false,
      closable: false,
      key: deleteKey,
      title: formatMessage({ id: `${intlPrefix}.instance.action.delete` }),
      children: formatMessage({ id: `${intlPrefix}.instance.action.delete.tips` }),
      onOk: deleteItem,
    });
  }

  function openChangeActive(active) {
    Modal.open({
      movable: false,
      closable: false,
      key: stopKey,
      title: formatMessage({ id: `${intlPrefix}.instance.action.${active}` }),
      children: formatMessage({ id: `${intlPrefix}.instance.action.${active}.tips` }),
      onOk: () => handleChangeActive(active),
    });
  }

  async function handleChangeActive(active) {
    try {
      const result = await treeItemStore.changeIstActive(id, record.get('id'), active);
      if (handlePromptError(result, false)) {
        treeDs.query();
      }
    } catch (error) {
      Choerodon.handleResponseError(error);
    }
  }

  function getSuffix() {
    let actionData;
    const actionItems = {
      stop: {
        service: ['devops-service.app-service-instance.stop'],
        text: formatMessage({ id: `${intlPrefix}.instance.action.stop` }),
        action: () => openChangeActive('stop'),
      },
      start: {
        service: ['devops-service.app-service-instance.start'],
        text: formatMessage({ id: `${intlPrefix}.instance.action.start` }),
        action: () => openChangeActive('start'),
      },
      delete: {
        service: ['devops-service.app-service-instance.delete'],
        text: formatMessage({ id: `${intlPrefix}.instance.action.delete` }),
        action: openDeleteModal,
      },
    };
    switch (record.get('status')) {
      case 'running':
        actionData = [actionItems.stop, actionItems.delete];
        break;
      case 'stopped':
        actionData = [actionItems.start, actionItems.delete];
        break;
      case 'failed':
        actionData = [actionItems.delete];
        break;
      default:
        break;
    }

    return actionData ? <Action
      placement="bottomRight"
      data={actionData}
      onClick={eventStopProp}
    /> : null;
  }

  return <Fragment>
    <PodCircle
      size="small"
      dataSource={podData}
    />
    {name}
    {getSuffix()}
  </Fragment>;
}

InstanceItem.propTypes = {
  name: PropTypes.any,
  podColor: PropTypes.shape({}),
};

export default injectIntl(observer(InstanceItem));
