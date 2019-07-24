/**
 * hover 显示时间
 */
import React from 'react/index';
import { observer } from 'mobx-react';
import { Tooltip } from 'choerodon-ui';
import TimeAgo from 'timeago-react';
import PropTypes from 'prop-types';
import { formatDate } from '../../utils';

const TimePopoverRequiredProps = {
  content: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.number,
  ]),
  style: PropTypes.object,
};

function TimePopover({ content, style }) {
  const timestamp = content && typeof content === 'string'
    ? Math.min(Date.now(), new Date(content.replace(/-/g, '/')).getTime())
    : false;

  return (
    <div style={style}>
      <Tooltip
        title={formatDate(timestamp || content)}
      >
        <TimeAgo
          datetime={timestamp || content}
          locale={Choerodon.getMessage('zh_CN', 'en')}
        />
      </Tooltip>
    </div>
  );
}

TimePopover.propTypes = TimePopoverRequiredProps;
export default observer(TimePopover);
