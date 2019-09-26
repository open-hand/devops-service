import React from 'react';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import { injectIntl } from 'react-intl';
import { Button } from 'choerodon-ui/pro';

import './index.less';

const EmptyPage = withRouter(injectIntl(((props) => {
  const {
    history,
    location: { search },
    pathname,
    approve,
    title,
    describe,
    btnText,
  } = props;

  function handleClick() {
    history.push({
      pathname,
      search,
      state: { isCreate: true },
    });
  }

  return <div className="c7ncd-empty-page-wrap">
    <div className="c7ncd-empty-page">
      <div className={`c7ncd-empty-page-image c7ncd-empty-page-image-${approve ? 'owner' : 'member'}`} />
      <div className="c7ncd-empty-page-text">
        <div className="c7ncd-empty-page-title">
          {title}
        </div>
        <div className="c7ncd-empty-page-des">
          {describe}
        </div>
        {approve && (
          <Button
            color="primary"
            onClick={handleClick}
            funcType="raised"
          >
            {btnText}
          </Button>
        )}
      </div>
    </div>
  </div>;
})));

EmptyPage.propTypes = {
  pathname: PropTypes.string,
  approve: PropTypes.bool,
  title: PropTypes.string,
  btnText: PropTypes.string,
  describe: PropTypes.string,
};

EmptyPage.defaultProps = {
  pathname: '',
  approve: false,
  btnText: 'Ok',
};

export default EmptyPage;
