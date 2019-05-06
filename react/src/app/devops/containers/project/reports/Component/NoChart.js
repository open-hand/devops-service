import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import './NoChart.scss';
import ReportsStore from '../../../../stores/project/reports';

function NoChart(props) {
  const { type } = props;
  const { getProRole } = ReportsStore;
  const noChart = {
    'owner': {
      title: `report.no-${type}`,
      des: `report.no-${type}-des`,
    },
    'member': {
      title: 'depPl.noPermission',
      des: `empty.member.no-${type}`,
    },
    '': {
      title: 'null',
      des: 'null',
    },
  };
  return (
    <div className="c7n-no-chart">
      <div className="c7n-no-chart-pic">
        <div />
      </div>
      <div className="c7n-no-chart-desc">
        <div className="c7n-no-chart-title"><FormattedMessage id={noChart[getProRole].title} /></div>
        <div className="c7n-no-chart-des"><FormattedMessage id={noChart[getProRole].des} /></div>
      </div>
    </div>
  );
}

NoChart.propTypes = {
  type: PropTypes.string.isRequired,
};

export default withRouter(NoChart);
