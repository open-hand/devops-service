/**
 * @author ale0720@163.com
 * @date 2019-06-14 09:43
 */
import React from 'react/index';
import { Permission } from '@choerodon/boot';
import { FormattedMessage } from 'react-intl';
import PropTypes from 'prop-types';
import { Button } from 'choerodon-ui';

export default function ButtonGroup({ disabled, loading, onNext, onPrev, onCancel, primary, nextTextId }) {

  const nextButton = <Button
    type="primary"
    funcType="raised"
    disabled={disabled}
    onClick={onNext}
    loading={loading}
  >
    <FormattedMessage id={nextTextId} />
  </Button>;

  const nextNode = primary.length ? <Permission service={primary}>
    {nextButton}
  </Permission> : nextButton;

  const prevNode = onPrev ? <Button
    funcType="raised"
    onClick={onPrev}
  >
    <FormattedMessage id="previous" />
  </Button> : null;

  return <div className="c7ncd-step-btn">
    {nextNode}
    {prevNode}
    <Button
      funcType="raised"
      className="c7ncd-step-cancel-btn"
      onClick={onCancel}
    >
      <FormattedMessage id="cancel" />
    </Button>
  </div>;
}

ButtonGroup.propTypes = {
  nextTextId: PropTypes.string,
  disabled: PropTypes.bool,
  loading: PropTypes.bool,
  onNext: PropTypes.func,
  onPrev: PropTypes.func,
  onCancel: PropTypes.func,
  primary: PropTypes.array,
};

ButtonGroup.defaultProps = {
  primary: [],
  loading: false,
  disabled: false,
  nextTextId: 'next',
};
