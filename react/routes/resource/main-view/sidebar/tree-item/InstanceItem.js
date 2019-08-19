import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { observer } from 'mobx-react-lite';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
import PodCircle from '../../components/pod-circle';
import { useResourceStore } from '../../../stores';

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
  const { treeDs } = useResourceStore();

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

  function freshMenu() {
    treeDs.query();
  }

  function deleteItem() {
    treeDs.remove(record);
  }

  function getSuffix() {
    const actionData = [{
      service: [],
      text: formatMessage({ id: `${intlPrefix}.instance.action.stop` }),
      action: deleteItem,
    }, {
      service: [],
      text: formatMessage({ id: `${intlPrefix}.instance.action.delete` }),
      action: freshMenu,
    }];
    return <Action placement="bottomRight" data={actionData} />;
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
