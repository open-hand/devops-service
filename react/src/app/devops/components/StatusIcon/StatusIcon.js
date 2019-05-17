import React from "react";
import { injectIntl } from "react-intl";
import PropTypes from "prop-types";
import { Tooltip, Progress, Icon } from "choerodon-ui";
import MouseOverWrapper from "../../components/MouseOverWrapper";
import "./StatusIcon.scss";

function StatusIcon(props) {
  const {
    status,
    error,
    name,
    intl: { formatMessage },
  } = props;
  let statusDom = null;
  switch (status) {
    case "failed":
      const msg = error ? `: ${error}` : "";
      statusDom = (
        <Tooltip title={`failed ${msg}`}>
          <Icon type="error" className="c7n-status-failed" />
        </Tooltip>
      );
      break;
    case "operating":
      statusDom = (
        <Tooltip title={formatMessage({ id: `ist_operating` })}>
          <Progress type="loading" width={15} className="c7n-status-progress" />
        </Tooltip>
      );
      break;
    default:
      statusDom = null;
  }
  return (
    <React.Fragment>
      <MouseOverWrapper className="c7n-status-text" text={name} width={0.15}>
        {name}
      </MouseOverWrapper>
      {statusDom}
    </React.Fragment>
  );
}

StatusIcon.propTypes = {
  status: PropTypes.string.isRequired,
  name: PropTypes.string.isRequired,
  error: PropTypes.string,
};

export default injectIntl(StatusIcon);
