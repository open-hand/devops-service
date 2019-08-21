import React, { Fragment, useMemo } from 'react';
import { Action } from '@choerodon/master';
import { injectIntl } from 'react-intl';
import { Icon } from 'choerodon-ui/pro';
import PropTypes from 'prop-types';

function AppItem({ name, intl: { formatMessage } }) {
  function handleClick() {
    // console.log(name);
  }

  const getSuffix = useMemo(() => {
    const actionData = [{
      service: [],
      text: formatMessage({ id: 'delete' }),
      action: handleClick,
    }, {
      service: [],
      text: formatMessage({ id: 'delete' }),
      action: handleClick,
    }];
    return <Action placement="bottomRight" data={actionData} />;
  }, []);

  return <Fragment>
    <Icon type="widgets" />
    {name}
    {getSuffix}
  </Fragment>;
}

AppItem.propTypes = {
  name: PropTypes.any,
};

export default injectIntl(AppItem);
