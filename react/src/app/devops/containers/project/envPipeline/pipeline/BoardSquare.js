import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { DropTarget } from 'react-dnd';
import _ from 'lodash';
import EnvPipelineStore from '../../../../stores/project/envPipeline/index';

const ItemTypes = {
  ENVCARD: 'envCard',
};

const squareTarget = {
  drop(props, monitor) {
    // Obtain the dragged item
    const item = monitor.getItem();
    let pos = {};
    if (item.cardData.devopsEnvGroupId) {
      pos = _.filter(EnvPipelineStore.getEnvcardPosition, { devopsEnvGroupId: item.cardData.devopsEnvGroupId })[0].devopsEnviromentRepDTOs;
    } else {
      pos = EnvPipelineStore.getEnvcardPosition[0].devopsEnviromentRepDTOs;
    }
    if ((props.children.props.cardData.sequence !== item.cardData.sequence) && (props.children.props.cardData.devopsEnvGroupId === item.cardData.devopsEnvGroupId)) {
      EnvPipelineStore.switchData(props.children.props.cardData.sequence,
        item.cardData.sequence, props.children.props.cardData.devopsEnvGroupId);
      const envIds = _.map(pos, 'id');
      EnvPipelineStore.updateSort(props.children.props.projectId, envIds, item.cardData.devopsEnvGroupId);
    }
  },
};

function collect(connect, monitor) {
  return {
    connectDropTarget: connect.dropTarget(),
    isOver: monitor.isOver(),
  };
}

class BoardSquare extends Component {
  render() {
    const { x, y, connectDropTarget, isOver } = this.props;
    return connectDropTarget(
      <div className="c7n-env-boardsquare">
        <div>{this.props.children}</div>
        {isOver
        && <div className="c7n-env-moveing" />
        }
        <span className="c7n-env-arrow">→</span>
      </div>,
    );
  }
}

BoardSquare.propTypes = {
  x: PropTypes.number.isRequired,
  y: PropTypes.number.isRequired,
  connectDropTarget: PropTypes.func.isRequired,
  isOver: PropTypes.bool.isRequired,
};

export default DropTarget(ItemTypes.ENVCARD, squareTarget, collect)(BoardSquare);
