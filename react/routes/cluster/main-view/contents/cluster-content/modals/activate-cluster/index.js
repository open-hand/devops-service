import React, { Fragment, useState, useMemo } from 'react';
import { Input } from 'choerodon-ui';
import { Tooltip, Icon } from 'choerodon-ui';
import CopyToBoard from 'react-copy-to-clipboard';
import { Choerodon } from '@choerodon/boot';

import './index.less';

const { TextArea } = Input;

const ActivateCluster = (props) => {
  const { formatMessage, cmd } = props;
  const [copyMsg, setCopyMsg] = useState('');

  const handleCopy = () => {
    Choerodon.prompt('复制成功');
  };

  return (
    <Fragment>
      <blockquote className="c7ncd-cluster-activate-note">
        {formatMessage({ id: 'c7ncd.cluster.activate.note' })}
      </blockquote>
      <p className="c7ncd-cluster-activate-desc">{formatMessage({ id: 'c7ncd.cluster.activate.desc' })}</p>
      <div className="c7ncd-cluster-activate-wrap">
        <TextArea
          label={formatMessage({ id: 'envPl.token' })}
          className="c7n-input-readOnly"
          autosize
          copy="true"
          readOnly
          value={cmd || ''}
        />
        <span className="c7ncd-cluster-activate-copy">
          <Tooltip
            placement="left"
            arrowPointAtCenter
            autoAdjustOverflow={false}
            title={formatMessage({ id: 'c7ncd.cluster.activate.desc' })}
          >
            <CopyToBoard text={cmd} onCopy={handleCopy} options={{ format: 'text/plain' }}>
              <Icon type="library_books" />
            </CopyToBoard>
          </Tooltip>
        </span>
      </div>
    </Fragment>);
};

export default ActivateCluster;
