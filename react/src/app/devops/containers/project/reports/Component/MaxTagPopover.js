import React from "react";
import { FormattedMessage } from "react-intl";
import { Popover } from "choerodon-ui";
import PropTypes from "prop-types";
import _ from "lodash";
import "../common.scss";

export default function MaxTagPopover(props) {
  const { value, dataSource, width, placement } = props;
  const moreOption = [];
  _.forEach(value, (item, index) => {
    const appName = _.find(dataSource, ["id", item]);
    appName &&
      moreOption.push(
        <span key={item}>
          {appName.name}
          {index < value.length - 1 ? "，" : ""}
        </span>
      );
  });
  return (
    <Popover
      arrowPointAtCenter
      placement={placement}
      content={
        <div className="c7n-report-maxPlace" style={{ width }}>
          {moreOption}
        </div>
      }
    >
      <div className="c7n-report-maxPlace-inner" title="">
        <FormattedMessage id="dashboard.devops.more" />
      </div>
    </Popover>
  );
}

/* eslint-disable-next-line */
MaxTagPopover.propTypes = {
  placement: PropTypes.string,
  width: PropTypes.number,
};

MaxTagPopover.defaultProps = {
  placement: "bottomLeft",
  width: 260,
};
