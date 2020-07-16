import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/boot';
import { Icon } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';
import eventStopProp from '../../../../../utils/eventStopProp';
import { useMainStore } from '../../stores';

function CertItem({
  record,
  name,
  intlPrefix,
  intl: { formatMessage },
}) {
  const {
    treeDs,
    resourceStore: { getSelectedMenu: { itemType, parentId }, setUpTarget },
    itemTypes: { CERT_GROUP },
  } = useResourceStore();
  const {
    mainStore: { openDeleteModal },
  } = useMainStore();

  function freshMenu() {
    treeDs.query();
    const [envId] = record.get('parentId').split('**');
    if (itemType === CERT_GROUP && envId === parentId) {
      setUpTarget({
        type: CERT_GROUP,
        id: parentId,
      });
    }
  }

  function getEnvIsNotRunning() {
    const [envId] = record.get('parentId').split('**');
    const envRecord = treeDs.find((item) => item.get('key') === envId);
    const connect = envRecord.get('connect');
    return !connect;
  }

  function getSuffix() {
    const id = record.get('id');
    const certName = record.get('name');
    const [envId] = record.get('parentId').split('**');
    const status = record.get('status');
    const disabled = getEnvIsNotRunning() || status === 'operating';
    if (disabled) {
      return null;
    }
    const actionData = [{
      service: ['choerodon.code.project.deploy.app-deployment.resource.ps.delete-certificate'],
      text: formatMessage({ id: 'delete' }),
      action: () => openDeleteModal(envId, id, certName, 'certificate', freshMenu),
    }];
    return <Action placement="bottomRight" data={actionData} onClick={eventStopProp} />;
  }

  return <Fragment>
    <Icon type="class" />
    {name}
    {getSuffix()}
  </Fragment>;
}

CertItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(observer(CertItem));
