import React from "react";
import { injectIntl } from "react-intl";
import PropTypes from "prop-types";
import _ from "lodash";
import "./PodStatus.scss";

/**
 * 计算正在运行的pod
 * @param {*} data
 * @param {string} [key="devopsEnvPodDTOS"]
 * @returns
 */
function counter(data, key = "devopsEnvPodDTOS") {
  let sum = 0;
  let error = 0;
  let correct = 0;
  if (!data) {
    return {
      sum,
      correct,
    };
  }
  _.forEach(data, item => {
    const count = _.countBy(item[key], pod => !!pod.ready);
    correct += count["true"] || 0;
    error += count["false"] || 0;
  });
  sum = correct + error;
  return {
    sum,
    correct,
  };
}

function PodStatus(props) {
  const {
    dataSource: { deploymentDTOS, statefulSetDTOS, daemonSetDTOS },
  } = props;

  const dmCount = counter(deploymentDTOS);
  const sfCount = counter(statefulSetDTOS);
  const dsCount = counter(daemonSetDTOS);

  const sum = dmCount.sum + sfCount.sum + dsCount.sum;
  const correctCount = dmCount.correct + sfCount.correct + dsCount.correct;

  const correct = sum > 0 ? (correctCount / sum) * (Math.PI * 2 * 10) : 0;

  const circle = (
    <svg width="24" height="24">
      <circle
        cx="50%"
        cy="50%"
        r="10"
        fill="none"
        strokeWidth={sum === 0 || sum > correctCount ? 4 : 0}
        stroke={sum > 0 ? "#FFB100" : "#f3f3f3"}
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
        {sum}
      </text>
    </svg>
  );
  return <div className="c7n-deploy-pod-status">{circle}</div>;
}

PodStatus.propTypes = {
  dataSource: PropTypes.object.isRequired,
};

export default injectIntl(PodStatus);
