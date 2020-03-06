import React from 'react';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import { Button } from 'choerodon-ui';
import './NoChart.less';

const NoChart = observer((props) => {
  const {
    type,
    history,
    location: { search },
    getProRole,
  } = props;

  const handleClick = () => {
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
        <div className="c7n-no-chart-title">
          {getProRole === '' ? '' : <FormattedMessage id={noChart[getProRole].title} />}
        </div>
        <div className="c7n-no-chart-des">
          {getProRole === '' ? '' : <FormattedMessage id={noChart[getProRole].des} />}
        </div>
        {getProRole === 'owner' && (
        <Button
          type="primary"
          funcType="raised"
          onClick={handleClick}
        >
          <FormattedMessage id={`empty.link.${type}`} />
        </Button>
        )}
      </div>
    </div>
  );
});

NoChart.propTypes = {
  type: PropTypes.string.isRequired,
  getProRole: PropTypes.string.isRequired,
};

export default withRouter(NoChart);
