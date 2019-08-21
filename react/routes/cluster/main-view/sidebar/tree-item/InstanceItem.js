import React, { Fragment, useMemo } from 'react';
import PropTypes from 'prop-types';
import { injectIntl } from 'react-intl';
import { Action } from '@choerodon/master';
import PodCircle from '../../../../resource/main-view/components/pod-circle';
import { useClusterStore } from '../../../stores';

function InstanceItem({
  record,
  name,
  podColor,
  running,
  unlink,
  intlPrefix,
  intl: { formatMessage },
}) {
  const { treeDs } = useClusterStore();
  const podData = useMemo(() => {
    const {
      RUNNING_COLOR,
      PADDING_COLOR,
    } = podColor;

    return [{
      name: 'running',
      value: running,
      stroke: RUNNING_COLOR,
    }, {
      name: 'unlink',
      value: unlink,
      stroke: PADDING_COLOR,
    }];
  }, [running, unlink]);

  function freshMenu() {
    treeDs.query();
  }

  function deleteItem() {
    treeDs.remove(record);
  }

  const getSuffix = useMemo(() => {
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
  }, []);

  return <Fragment>
    <PodCircle
      size="small"
      dataSource={podData}
    />
    {name}
    {getSuffix}
  </Fragment>;
}

InstanceItem.propTypes = {
  name: PropTypes.any,
  podColor: PropTypes.shape({}),
  running: PropTypes.number,
  unlink: PropTypes.number,
};

export default injectIntl(InstanceItem);
