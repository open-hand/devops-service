import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
import { Modal } from 'choerodon-ui/pro';
import { useClusterStore } from '../../../stores';
import { useClusterMainStore } from '../../stores';
import StatusDot from '../../../../../components/status-dot';
import ActivateCluster from '../../contents/cluster-content/modals/activate-cluster';
import { useTreeStore } from './stores';
import { handlePromptError } from '../../../../../utils';
import EditCluster from '../../contents/cluster-content/modals/create-cluster';

const ActivateClusterModalKey = Modal.key();
const EditClusterModalKey = Modal.key();
function ClusterItem({
  record,
  name,
  intlPrefix,
  intl: { formatMessage },
}) {
  const { treeDs } = useClusterStore();
  const { mainStore, ClusterDetailDs } = useClusterMainStore();

  const { projectId, treeItemStore } = useTreeStore();
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
    // treeDs.remove(record);
    Modal.confirm({
      title: 'Confirm',
      children: formatMessage({ id: `${intlPrefix}.action.delete.msg` }),
    }).then((button) => {
      if (button === 'ok') {
        mainStore.deleteCluster({ projectId, clusterId: record.get('id') })
          .then((res) => {
            if (handlePromptError(res, false)) {
              freshMenu();
            }
          });
      }
    });
  }

  function editItem() {
    Modal.open({
      key: EditClusterModalKey,
      title: formatMessage({ id: `${intlPrefix}.modal.create` }),
      children: <EditCluster isEdit record={ClusterDetailDs.current} mainStore={mainStore} afterOk={freshMenu} intlPrefix={intlPrefix} formatMessage={formatMessage} treeItemStore={treeItemStore} projectId={projectId} />,
      drawer: true,
      style: {
        width: 500,
      },
      okText: formatMessage({ id: 'save' }),
    });
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
          width: 500,
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
      service: [],
      text: formatMessage({ id: `${intlPrefix}.action.edit` }),
      action: editItem,
    }];
    if (status === 'disconnect') {
      Data.push({
        service: [],
        text: formatMessage({ id: `${intlPrefix}.activate.header` }),
        action: activateItem,
      }, {
        service: [],
        text: formatMessage({ id: `${intlPrefix}.action.delete` }),
        action: deleteItem,
      });
    }
    return <Action placement="bottomRight" data={Data} />;
  }, []);
  return <Fragment>
    {getPrefix}
    {name}
    {getSuffix}
  </Fragment>;
}

ClusterItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(ClusterItem);
