import React from 'react';
import { FormattedMessage } from 'react-intl';
import { Tooltip } from 'choerodon-ui';
import PropTypes from 'prop-types';
import _ from 'lodash';
import '../index.less';

export default function MaxTagPopover(props) {
  const { value, dataSource, width, placement } = props;
  const moreOption = [];
  _.forEach(value, (item, index) => {
    const appName = _.find(dataSource, ['id', item]);
    appName
      && moreOption.push(
        <span key={item}>
          {appName.name}
          {index < value.length - 1 ? '，' : ''}
        </span>
      );
  });
  return (
    <Tooltip
      arrowPointAtCenter
      placement={placement}
      overlayClassName="c7n-report-maxPlace"
      title={moreOption}
    >
      <div className="c7n-report-maxPlace-inner" title="">
        <FormattedMessage id="dashboard.devops.more" />
      </div>
    </Tooltip>
  );
}

/* eslint-disable-next-line */
MaxTagPopover.propTypes = {
  placement: PropTypes.string,
  width: PropTypes.number,
};

MaxTagPopover.defaultProps = {
  placement: 'bottomLeft',
  width: 350,
};
