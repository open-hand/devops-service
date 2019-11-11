import React, { Component } from 'react';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import { Button } from 'choerodon-ui';
import './NoChart.less';
import ReportsStore from '../stores';

@withRouter
@observer
class NoChart extends Component {
  static propTypes = {
    type: PropTypes.string.isRequired,
  };

  handleClick = () => {
    const {
      type,
      history,
      location: { search },
    } = this.props;
    switch (type) {
      case 'env':
        history.push({
          pathname: '/devops/environment',
          search,
        });
        break;
      case 'app':
        history.push({
          pathname: '/devops/app-service',
          search,
        });
        break;
      default:
        break;
    }
  };

  render() {
    const { type } = this.props;
    const { getProRole } = ReportsStore;
    const noChart = {
      owner: {
        title: `empty.title.${type}`,
        des: `empty.tips.${type}.owner`,
      },
      member: {
        title: 'empty.title.prohibited',
        des: `empty.tips.${type}.member`,
      },
      '': {
        title: 'null',
        des: 'null',
      },
    };
    return (
      <div className="c7n-no-chart">
        <div className="c7n-no-chart-pic">
          <div className={`c7n-no-chart-pic-${getProRole}`} />
        </div>
        <div className="c7n-no-chart-desc">
          <div className="c7n-no-chart-title"><FormattedMessage id={noChart[getProRole].title} /></div>
          <div className="c7n-no-chart-des"><FormattedMessage id={noChart[getProRole].des} /></div>
          {getProRole === 'owner' && (
            <Button
              type="primary"
              funcType="raised"
              onClick={this.handleClick}
            >
              <FormattedMessage id={`empty.link.${type}`} />
            </Button>
          )}
        </div>
      </div>
    );
  }
}

export default NoChart;
