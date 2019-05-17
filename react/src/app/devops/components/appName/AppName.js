import React, { Fragment } from "react";
import { FormattedMessage } from "react-intl";
import PropTypes from "prop-types";
import { Tooltip, Icon } from "choerodon-ui";
import MouseOverWrapper from "../MouseOverWrapper";
import "./AppName.scss";

/**
 * 带icon的应用名称
 * @param { 应用名称，显示应用前icon，本组织or应用市场 } props
 */
export default function AppName(props) {
  const { name, showIcon, self, width } = props;
  const type = self ? "project" : "market";
  const icon = self ? "project" : "apps";
  return (
    <Fragment>
      {showIcon ? (
        <Tooltip title={<FormattedMessage id={type} />}>
          <Icon type={icon} className="c7ncd-app-icon" />
        </Tooltip>
      ) : null}
      <MouseOverWrapper className="c7ncd-app-text" text={name} width={width}>
        {name}
      </MouseOverWrapper>
    </Fragment>
  );
}

AppName.propTypes = {
  name: PropTypes.string.isRequired,
  showIcon: PropTypes.bool.isRequired,
  self: PropTypes.bool.isRequired,
  width: PropTypes.oneOfType([
    PropTypes.string.isRequired,
    PropTypes.number.isRequired,
  ]),
};
