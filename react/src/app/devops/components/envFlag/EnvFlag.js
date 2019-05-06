import React from "react";
import { FormattedMessage } from "react-intl";
import PropTypes from "prop-types";
import { Tooltip } from "choerodon-ui";
import MouseOverWrapper from "../MouseOverWrapper";
import "../../containers/main.scss";
import "./EnvFlag.scss";

export default function EnvFlag(props) {
  const { status, name } = props;
  return (
    <div className="c7ncd-env-status">
      {status ? (
        <Tooltip title={<FormattedMessage id="connect" />}>
          <span className="c7ncd-status c7ncd-status-success" />
        </Tooltip>
      ) : (
        <Tooltip title={<FormattedMessage id="disconnect" />}>
          <span className="c7ncd-status c7ncd-status-disconnect" />
        </Tooltip>
      )}
      <MouseOverWrapper
        text={name || ""}
        width={0.12}
        className="c7ncd-env-name"
      >
        {name}
      </MouseOverWrapper>
    </div>
  );
}

EnvFlag.PropTypes = {
  status: PropTypes.bool.isRequired,
  name: PropTypes.string.isRequired,
};
