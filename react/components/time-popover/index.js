import React, { Fragment, memo } from 'react';
import PropTypes from 'prop-types';
import { Tooltip } from 'choerodon-ui';
import TimeAgo from 'timeago-react';
import { formatDate } from '../../utils';

function TimePopover({ datetime, placement }) {
  let time;

  if (datetime) {
    // eslint-disable-next-line no-underscore-dangle
    if (typeof datetime === 'object' && datetime._isAMomentObject) {
      time = datetime.format();
    } else if (typeof datetime === 'string') {
      time = new Date(datetime.replace(/-/g, '/')).getTime();
    }
  }

  return <Fragment>
    {time ? <Tooltip
      placement={placement}
      title={formatDate(time)}
    >
      <TimeAgo
        datetime={time}
        locale={Choerodon.getMessage('zh_CN', 'en')}
      />
    </Tooltip> : null}
  </Fragment>;
}

TimePopover.propTypes = {
  placement: PropTypes.string,
};

TimePopover.defaultProps = {
  placement: 'top',
};

export default memo(TimePopover);
