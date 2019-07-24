import React from "react/index";
import { injectIntl } from "react-intl";
import PropTypes from "prop-types";
import "./PodStatus.scss";

function PodStatus(props) {
  const {
    dataSource: { podCount, podRunningCount },
  } = props;
  const strokeWidth = podCount === 0 || podCount > podRunningCount ? 4 : 0;
  const correct = podCount > 0 ? (podRunningCount / podCount) * (Math.PI * 2 * 10) : 0;

  const circle = (
    <svg width="24" height="24">
      <circle
        cx="50%"
        cy="50%"
        r="10"
        fill="none"
        strokeWidth={strokeWidth}
        stroke={podCount > 0 ? "#FFB100" : "#f3f3f3"}
      />
      <circle
        cx="50%"
        cy="50%"
        r="10"
        fill="none"
        className="c7n-pod-circle-small"
        strokeWidth="4px"
        stroke="#0bc2a8"
        strokeDasharray={`${correct}, 10000`}
      />
      <text x="50%" y="16" className="c7n-pod-circle-num-small">
        {podCount}
      </text>
    </svg>
  );
  return <div className="c7n-deploy-pod-status">{circle}</div>;
}

PodStatus.propTypes = {
  dataSource: PropTypes.object.isRequired,
};

export default injectIntl(PodStatus);
