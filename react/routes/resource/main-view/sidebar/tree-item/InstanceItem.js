import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Modal } from 'choerodon-ui/pro';
import Action from '../../../../../components/action';
import PodCircle from '../../components/pod-circle';
import { useResourceStore } from '../../../stores';
import { useTreeItemStore } from './stores';
import { handlePromptError } from '../../../../../utils';

const stopKey = Modal.key();

const ACTION_STOPPED = 'stopped';
const ACTION_FAILED = 'failed';
const ACTION_RUNNING = 'running';

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

  function deleteItem() {
    treeDs.delete(record);
  }

  function openChangeActive(active) {
    Modal.open({
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

  function handleMenuClick(e) {
    e.domEvent.stopPropagation();
    const action = {
      [ACTION_RUNNING]: () => openChangeActive('stop'),
      [ACTION_FAILED]: deleteItem,
      [ACTION_STOPPED]: () => openChangeActive('start'),
    };
    const handler = action[e.key];
    handler && handler();
  }

  function getSuffix() {
    const status = record.get('status');
    const enableDelete = [ACTION_RUNNING, ACTION_STOPPED, ACTION_FAILED].includes(status);
    const actionData = [{
      display: status === ACTION_RUNNING,
      service: ['devops-service.app-service-instance.stop'],
      key: ACTION_RUNNING,
      text: formatMessage({ id: `${intlPrefix}.instance.action.stop` }),
    }, {
      display: status === ACTION_STOPPED,
      service: ['devops-service.app-service-instance.start'],
      key: ACTION_STOPPED,
      text: formatMessage({ id: `${intlPrefix}.instance.action.start` }),
    }, {
      display: enableDelete,
      service: ['devops-service.app-service-instance.delete'],
      key: ACTION_FAILED,
      text: formatMessage({ id: `${intlPrefix}.instance.action.delete` }),
    }];
    return <Action
      placement="bottomRight"
      items={actionData}
      menuClick={handleMenuClick}
    />;
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
