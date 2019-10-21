import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Action, Choerodon } from '@choerodon/boot';
import { Modal, Icon } from 'choerodon-ui/pro';
import { Input } from 'choerodon-ui';
import { useClusterStore } from '../../../stores';
import { useClusterMainStore } from '../../stores';
import StatusDot from '../../../../../components/status-dot';
import ActivateCluster from '../../contents/cluster-content/modals/activate-cluster';
import { useTreeStore } from './stores';
import { handlePromptError } from '../../../../../utils';
import EditCluster from '../../contents/cluster-content/modals/create-cluster';
import CustomConfirm from '../../../../../components/custom-confirm';

const ActivateClusterModalKey = Modal.key();
const EditClusterModalKey = Modal.key();
const deleteModalKey = Modal.key();
function ClusterItem({
  record,
  name,
  intlPrefix,
  prefixCls,
  intl: { formatMessage },
}) {
  const { treeDs } = useClusterStore();
  const { mainStore, ClusterDetailDs } = useClusterMainStore();

  const { projectId, treeItemStore } = useTreeStore();

  const customConfirm = useMemo(() => new CustomConfirm({ formatMessage }), []);

  function getStatus() {
    const connect = record.get('connect');
    const upgrade = record.get('upgrade');
    if (upgrade) {
      return ['disconnect'];
    } else if (connect) {
      return ['running', 'connect'];
    }
    return ['disconnect'];
  }

  function freshMenu() {
    treeDs.query();
  }

  function deleteItem() {
    const code = record.get('code');
    const clusterName = record.get('name');
    const modalContent = (
      <div>
        <FormattedMessage id="cluster.delDes_1" />
        <div
          className={`${prefixCls}-delete-input`}
        >
          <Input
            value={`helm del choerodon-cluster-agent-code ${code || ''} --purge`}
            readOnly
            copy
          />
        </div>
        <div className={`${prefixCls}-delete-notice`}>
          <Icon type="error" /><FormattedMessage id="cluster.delDes_2" />
        </div>
      </div>
    );
    mainStore.deleteCheck(projectId, record.get('id'))
      .then(data => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
        } else {
          Modal.open({
            key: deleteModalKey,
            title: formatMessage({ id: `${intlPrefix}.action.delete.title` }, { name: clusterName }),
            children: modalContent,
            onOk: handleDelete,
            okText: formatMessage({ id: 'cluster.del.confirm' }),
          });
        }
      });
  }

  async function handleDelete() {
    try {
      const res = await mainStore.deleteCluster({ projectId, clusterId: record.get('id') });
      if (handlePromptError(res, false)) {
        freshMenu();
      } else {
        return false;
      }
    } catch (e) {
      Choerodon.handleResponseError(e);
      return false;
    }
  }

  function openEdit(res) {
    Modal.open({
      key: EditClusterModalKey,
      title: formatMessage({ id: `${intlPrefix}.modal.edit` }),
      // children: <EditCluster isEdit clusterId={record.data.id} record={res || ClusterDetailDs.current} mainStore={mainStore} afterOk={freshMenu} intlPrefix={intlPrefix} formatMessage={formatMessage} treeItemStore={treeItemStore} projectId={projectId} />,
      children: <EditCluster isEdit clusterId={record.data.id} mainStore={mainStore} afterOk={freshMenu} intlPrefix={intlPrefix} formatMessage={formatMessage} treeItemStore={treeItemStore} projectId={projectId} />,
      drawer: true,
      style: {
        width: 380,
      },
      okText: formatMessage({ id: 'save' }),
    });
  }

  async function editItem() {
    // const res = await treeItemStore.queryClusterDetail(projectId, record.data.id);
    // openEdit(res);
    openEdit();
  }

  async function activateItem() {
    const res = await treeItemStore.queryActivateClusterShell(projectId, record.get('id'));
    if (handlePromptError(res)) {
      Modal.open({
        key: ActivateClusterModalKey,
        title: formatMessage({ id: `${intlPrefix}.activate.header` }),
        children: <ActivateCluster cmd={res} intlPrefix={intlPrefix} formatMessage={formatMessage} />,
        drawer: true,
        style: {
          width: 380,
        },
        okCancel: false,
        okText: formatMessage({ id: 'close' }),
      });
    }
  }

  const getPrefix = useMemo(() => <StatusDot
    size="small"
    getStatus={getStatus}
  />, [record]);

  const getSuffix = useMemo(() => {
    const [status] = getStatus();
    const Data = [{
      service: ['devops-service.devops-cluster.update'],
      text: formatMessage({ id: `${intlPrefix}.action.edit` }),
      action: editItem,
    }];
    if (status === 'disconnect') {
      Data.push({
        service: ['devops-service.devops-cluster.queryShell'],
        text: formatMessage({ id: `${intlPrefix}.activate.header` }),
        action: activateItem,
      }, {
        service: ['devops-service.devops-cluster.deleteCluster'],
        text: formatMessage({ id: `${intlPrefix}.action.delete` }),
        action: deleteItem,
      });
    }
    return <Action placement="bottomRight" data={Data} />;
  }, []);

  const clearClick = (e) => {
    e.stopPropagation();
  };
  return <Fragment>
    {getPrefix}
    {name}
    <div onClick={clearClick}>
      {getSuffix}
    </div>
  </Fragment>;
}

ClusterItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(ClusterItem);
