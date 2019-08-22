import React, { Fragment } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
import { Icon } from 'choerodon-ui';
import { useResourceStore } from '../../../stores';

function CertItem({
  record,
  name,
  intlPrefix,
  intl: { formatMessage },
}) {
  const { treeDs } = useResourceStore();

  function freshMenu() {
    treeDs.query();
  }

  function deleteItem() {
    treeDs.remove(record);
  }

  function getSuffix() {
    const actionData = [{
      service: ['devops-service.certification.delete'],
      text: formatMessage({ id: 'delete' }),
      action: deleteItem,
    }];
    return <Action placement="bottomRight" data={actionData} />;
  }

  return <Fragment>
    <Icon type="router" />
    {name}
    {getSuffix()}
  </Fragment>;
}

CertItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(observer(CertItem));
