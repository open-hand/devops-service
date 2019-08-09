import React, { Fragment, useMemo, useCallback } from 'react';
import PropTypes from 'prop-types';
import { Action } from '@choerodon/boot';
import PodCircle from '../../components/pod-circle';
import { useDeploymentStore } from '../../../stores';
import { useSidebarStore } from '../stores';

import './index.less';

const InstanceItem = ({ istId, name, podColor, running, unlink }) => {
  const {
    intl: { formatMessage },
  } = useDeploymentStore();
  const { treeDs } = useSidebarStore();

  const getPrefix = useMemo(() => {
    const {
      RUNNING_COLOR,
      PADDING_COLOR,
    } = podColor;

    return <PodCircle
      size="small"
      dataSource={[{
        name: 'running',
        value: running,
        stroke: RUNNING_COLOR,
      }, {
        name: 'unlink',
        value: unlink,
        stroke: PADDING_COLOR,
      }]}
    />;
  }, [podColor, running, unlink]);

  const freshMenu = useCallback(() => {
    treeDs.query();
  }, [treeDs]);

  const getSuffix = useMemo(() => {
    const actionData = [{
      service: [],
      text: formatMessage({ id: 'delete' }),
      action: () => freshMenu(istId),
    }, {
      service: [],
      text: formatMessage({ id: 'delete' }),
      action: () => freshMenu(istId),
    }];
    return <Action placement="bottomRight" data={actionData} />;
  }, [formatMessage, freshMenu, istId]);

  return <Fragment>
    {getPrefix}
    {name}
    {getSuffix}
  </Fragment>;
};

InstanceItem.propTypes = {
  istId: PropTypes.number,
  name: PropTypes.any,
  podColor: PropTypes.shape({}),
  running: PropTypes.number,
  unlink: PropTypes.number,
};

export default InstanceItem;
