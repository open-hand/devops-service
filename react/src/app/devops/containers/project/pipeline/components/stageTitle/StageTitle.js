import React, { PureComponent } from 'react';
import PropTypes from 'prop-types';
import { FormattedMessage } from 'react-intl';
import { Button } from 'choerodon-ui';
import classnames from 'classnames';

import './StageTitle.scss';

export default class StageTitle extends PureComponent {
  static propTypes = {
    name: PropTypes.string.isRequired,
    type: PropTypes.string.isRequired,
    head: PropTypes.bool,
    allowDelete: PropTypes.bool,
  };

  static defaultProps = {
    name: '',
    type: 'auto',
    head: false,
    allowDelete: true,
  };

  render() {
    const {
      name,
      type,
      onChange,
      onRemove,
      allowDelete,
      head,
    } = this.props;

    const titleClass = classnames({
      'c7ncd-pipeline-stage-head': true,
      'c7ncd-pipeline-stage-first': head,
    });

    return (
      <div className={titleClass}>
        <div className="c7ncd-pipeline-stage-title">
          <h3 className="c7ncd-pipeline-stage-name">{name}</h3>
          <div className="c7ncd-pipeline-stage-type">
            {type && <FormattedMessage id={`pipeline.flow.${type}`} />}
          </div>
        </div>
        <div className="c7ncd-pipeline-stage-op">
          <Button
            className="c7ncd-pipeline-stage-btn"
            size="small"
            icon="mode_edit"
            shape="circle"
            onClick={onChange}
          />
          {allowDelete && <Button
            size="small"
            icon="delete_forever"
            shape="circle"
            onClick={onRemove}
          />}
        </div>
      </div>
    );
  }
}
