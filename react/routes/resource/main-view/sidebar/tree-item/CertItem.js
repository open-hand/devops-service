import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
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
    resourceStore: { getSelectedMenu: { itemType }, setUpTarget },
    itemTypes: { CERT_GROUP },
  } = useResourceStore();
  const {
    mainStore: { openDeleteModal },
  } = useMainStore();

  function freshMenu() {
    treeDs.query();
    if (itemType === CERT_GROUP) {
      setUpTarget({
        type: CERT_GROUP,
      });
    }
  }

  function getSuffix() {
    const id = record.get('id');
    const certName = record.get('name');
    const actionData = [{
      service: ['devops-service.certification.delete'],
      text: formatMessage({ id: 'delete' }),
      action: () => openDeleteModal(id, certName, 'certificate', freshMenu),
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
