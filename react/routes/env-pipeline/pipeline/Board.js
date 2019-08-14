/* eslint-disable */
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { DragDropContext } from 'react-dnd';
import HTML5Backend from 'react-dnd-html5-backend';
import { Button, Tooltip, Icon } from 'choerodon-ui';
import { Permission } from '@choerodon/boot';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import classNames from 'classnames';
import EnvCard from './EnvCard';
import BoardSquare from './BoardSquare';
import '../index.scss';
import EnvPipelineStore from '../stores';
import { scrollTo } from '../../../utils';

const scrollLeft = {};

@inject('AppState')
@observer
class Board extends Component {
  static propTypes = {
    // envcardPositionChild: PropTypes.arrayOf(
    //   PropTypes.object.isRequired,
    // ).isRequired,
    projectId: PropTypes.number.isRequired,
    Title: PropTypes.string,
  };

  constructor(props) {
    super(props);
    this.state = {
      move: false,
    };
  }

  componentDidMount() {
    const { Title } = this.props;
    scrollLeft[Title || 'none'] = 0;
  }

  editGroup = (id, name) => {
    EnvPipelineStore.setShowGroup(true);
    EnvPipelineStore.setGroupOne({ id, name });
    EnvPipelineStore.setSideType('editGroup');
  };

  delGroup = (id, name) => {
    const { onDeleteGroup } = this.props;
    onDeleteGroup(id, name);
  };

  pushScrollRight = (Title) => {
    scrollLeft[Title] = scrollTo(document.getElementById(Title), -300);
    if (scrollLeft[Title] < 300) {
      scrollLeft[Title] = 0;
    }
    this.setState({
      move: false,
    });
  };

  pushScrollLeft = (Title) => {
    const domPosition = document.getElementById(Title).scrollLeft;
    const { envcardPositionChild } = this.props;
    const flag = envcardPositionChild.length * 330 - window.innerWidth + 297 <= domPosition + 300;
    this.setState({
      move: flag,
    });
    const res = scrollTo(document.getElementById(Title), 300);
    if ( res === 0 ) {
      scrollLeft[Title] = 300;
    } else {
      scrollLeft[Title] = res;
    }
  };

  renderSquare(i) {
    const x = i;
    const y = 0;
    return (
      <div
        key={i}
        className="c7n-env-square"
      >
        <BoardSquare
          x={x}
          y={y}
        >
          {this.renderPiece(x)}
        </BoardSquare>
      </div>
    );
  }

  renderPiece(x) {
    const { projectId, envcardPositionChild, onDisable } = this.props;
    return (<EnvCard
      projectId={projectId}
      cardData={envcardPositionChild[x]}
      handleDisable={onDisable}
    />);
  }

  render() {
    const squares = [];
    const {
      AppState,
      envcardPositionChild,
      groupId,
      Title,
      intl: { formatMessage },
    } = this.props;
    const { id: projectId, organizationId, type } = AppState.currentMenuType;

    for (let i = 0; i < envcardPositionChild.length; i += 1) {
      squares.push(this.renderSquare(i));
    }

    const rightStyle = classNames({
      'c7n-push-right icon icon-navigate_next': ((window.innerWidth >= 1680 && window.innerWidth < 1920) && envcardPositionChild.length >= 5) || (window.innerWidth >= 1920 && envcardPositionChild.length >= 6) || (window.innerWidth < 1680 && envcardPositionChild.length >= 4),
      'c7n-push-none': envcardPositionChild.length <= 4,
    });

    const outerContainer = classNames({
      'c7n-outer-container t-height': Title,
      'c7n-outer-container': !Title,
    });

    const innerContainer = classNames({
      'c7n-inner-container t-height': Title,
      'c7n-inner-container': !Title,
    });

    const envBoard = classNames({
      'c7n-env-board t-padding': Title,
      'c7n-env-board': !Title,
    });

    return (
      <div className={outerContainer}>
        {scrollLeft[Title || 'none'] !== 0
          ? <div role="none" className="c7n-push-left icon icon-navigate_before" style={Title && { top: 90 }} onClick={this.pushScrollRight.bind(this, Title || 'none')} />
          : ''}
        {Title ? (<div className="c7n-env-group-wrap">
          <div
            className="c7n-env-card-group"
          >
            {Title}
          </div>
          <Permission
            service={['devops-service.devops-env-group.update']}
            organizationId={organizationId}
            projectId={projectId}
            type={type}
          >
            <Tooltip title={<FormattedMessage id="envPl.group.edit" />}>
              <Button
                funcType="flat"
                shape="circle"
                onClick={this.editGroup.bind(this, groupId, Title)}
                icon="mode_edit"
              />
            </Tooltip>
          </Permission>
          <Permission
            service={['devops-service.devops-env-group.delete']}
            organizationId={organizationId}
            projectId={projectId}
            type={type}
          >
            <Tooltip title={<FormattedMessage id="envPl.group.del" />}>
              <Button
                funcType="flat"
                shape="circle"
                onClick={this.delGroup.bind(this, groupId, Title)}
                icon="delete_forever"
              />
            </Tooltip>
          </Permission>
        </div>) : null}
        <div className={innerContainer} id={Title || 'none'}>
          <div className={envBoard}>
            {squares.length ? squares : (<div className="c7n-env-card c7n-env-card-ban">
              <div className="c7n-env-card-header">
                <div>
                  {formatMessage({ id: 'envPl.add' })}
                </div>
              </div>
              <div className="c7n-env-card-content">
                <div className="c7n-env-state c7n-env-state-ban">
                  {formatMessage({ id: 'envPl.no.add' })}
                </div>
                <div className="c7n-env-des-wrap">
                  <div className="c7n-env-des">
                    <span className="c7n-env-des-head">{formatMessage({ id: 'envPl.description' })}</span>
                    {formatMessage({ id: 'envPl.add.description' })}
                  </div>
                </div>
              </div>
            </div>)}
          </div>
        </div>
        {this.state.move ? '' : <div role="none" className={rightStyle} style={Title && { top: 90 }} onClick={this.pushScrollLeft.bind(this, Title || 'none')} />}
      </div>
    );
  }
}

export default DragDropContext(HTML5Backend)(injectIntl(Board));
