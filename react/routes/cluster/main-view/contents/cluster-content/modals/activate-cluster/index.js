import React, { Fragment, useState, useMemo } from 'react';
import { Input, Tooltip } from 'choerodon-ui';
import CopyToBoard from 'react-copy-to-clipboard';
import _ from 'lodash';
import { handlePromptError } from '../../../../../../../utils';

import './index.less';

const { TextArea } = Input;

const ActivateCluster = (props) => {
  const { formatMessage, intlPrefix, modal, cmd } = props;
  const [copyMsg, setCopyMsg] = useState('');

  const mouseEnter = () => {
    setCopyMsg(formatMessage({ id: 'c7ncd.cluster.activate.desc' }));
  };

  const handleCopy = () => {
    setCopyMsg(formatMessage({ id: 'c7ncd.cluster.activate.coped' }));
  };

  return (
    <Fragment>
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
          <Tooltip placement="right" trigger="hover" title={copyMsg}>
            <div onMouseEnter={mouseEnter}>
              <CopyToBoard text={cmd} onCopy={handleCopy}>
                <i className="icon icon-library_books" />
              </CopyToBoard>
            </div>
          </Tooltip>
        </span>
      </div>
    </Fragment>);
};


export default ActivateCluster;
