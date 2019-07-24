import React, { Component, Fragment } from "react/index";
import { observer, inject } from "mobx-react";
import { injectIntl, FormattedMessage } from "react-intl";
import PropTypes from "prop-types";
import classNames from "classnames";
import { DragSource } from "react-dnd";
import { Button, Tooltip, Icon } from "choerodon-ui";
import { Permission, stores } from "@choerodon/boot";
import "../EnvPipeLineHome.scss";
import EnvPipelineStore from "../../../stores/project/envPipeline";

const { AppState } = stores;

const ItemTypes = {
  ENVCARD: "envCard",
};

const envCardSource = {
  beginDrag(props) {
    return {
      cardData: props.cardData,
      projectId: props.projectId,
    };
  },
};

function collect(connect, monitor) {
  return {
    connectDragSource: connect.dragSource(),
    isDragging: monitor.isDragging(),
  };
}

@observer
class EnvCard extends Component {
  editEnv = id => {
    const { projectId } = this.props;
    EnvPipelineStore.setSideType("edit");
    EnvPipelineStore.loadEnvById(projectId, id);
    EnvPipelineStore.setShow(true);
  };

  editPrm = id => {
    const { projectId } = this.props;
    EnvPipelineStore.setSideType("permission");
    EnvPipelineStore.loadPrm(projectId, id, 0, 10);
    EnvPipelineStore.loadTags(projectId, id);
    EnvPipelineStore.loadEnvById(projectId, id);
    EnvPipelineStore.setShow(true);
  };

  handleDisable = (id, connect, name) => {
    const { projectId, handleDisable } = this.props;
    handleDisable(id, connect, name);
  };

  getCardTitle(data) {
    if (!data) {
      return formatMessage({ id: "envPl.add" });
    }
    const { id: projectId, organizationId, type } = AppState.currentMenuType;
    const { failed, name, id, connect, synchro } = data;
    return (
      <Fragment>
        <span>{name}</span>
        <div className="c7n-env-card-action">
          {!failed && synchro ? (
            <Fragment>
              <Permission
                service={[
                  "devops-service.devops-environment.updateEnvUserPermission",
                ]}
                organizationId={organizationId}
                projectId={projectId}
                type={type}
              >
                <Tooltip title={<FormattedMessage id="envPl.authority" />}>
                  <Button
                    funcType="flat"
                    shape="circle"
                    icon="authority"
                    onClick={this.editPrm.bind(this, id)}
                  />
                </Tooltip>
              </Permission>
              <Permission
                service={["devops-service.devops-environment.update"]}
                organizationId={organizationId}
                projectId={projectId}
                type={type}
              >
                <Tooltip title={<FormattedMessage id="envPl.edit" />}>
                  <Button
                    funcType="flat"
                    shape="circle"
                    icon="mode_edit"
                    onClick={this.editEnv.bind(this, id)}
                  />
                </Tooltip>
              </Permission>
            </Fragment>
          ) : null}
          <Permission
            service={["devops-service.devops-environment.enableOrDisableEnv"]}
            organizationId={organizationId}
            projectId={projectId}
            type={type}
          >
            <Tooltip title={<FormattedMessage id="envPl.stop" />}>
              <Button
                funcType="flat"
                shape="circle"
                icon="remove_circle_outline"
                onClick={this.handleDisable.bind(this, id, connect, name)}
              />
            </Tooltip>
          </Permission>
        </div>
      </Fragment>
    );
  }

  getCardContent(data) {
    if (!data) {
      return formatMessage({ id: "envPl.add" });
    }
    const {
      intl: { formatMessage },
    } = this.props;
    const { connect, description, failed, synchro, clusterName } = data;
    let status = "";
    let styles = "";
    if (failed) {
      styles = "c7n-env-state-failed";
      status = "failed";
    } else if (!synchro) {
      styles = "c7n-env-state-creating";
      status = "operating";
    } else if (connect) {
      styles = "c7n-env-state-running";
      status = "running";
    } else {
      styles = "c7n-env-state-disconnect";
      status = "disconnect";
    }
    return (
      <div className="c7n-env-card-content">
        <div className={classNames("c7n-env-state", styles)}>
          {formatMessage({ id: status })}
        </div>
        <div className="c7n-env-des-wrap">
          <div className="c7n-env-des" title={description}>
            {clusterName && <div>
              <span className="c7n-env-des-head">
                {formatMessage({ id: "envPl.cluster" })}
              </span>
              {clusterName}
            </div>}
            <div>
              <span className="c7n-env-des-head">
                {formatMessage({ id: "envPl.description" })}
              </span>
              {description || formatMessage({ id: 'null' })}
            </div>
          </div>
        </div>
      </div>
    );
  }

  render() {
    const {
      AppState,
      connectDragSource,
      isDragging,
      cardData,
      intl: { formatMessage },
    } = this.props;
    const envCardStyle = classNames({
      "c7n-env-card": !isDragging,
      "c7n-env-card-dragging": isDragging,
    });

    return connectDragSource(
      <div className={envCardStyle}>
        <div className="c7n-env-card-header">{this.getCardTitle(cardData)}</div>
        {this.getCardContent(cardData)}
      </div>
    );
  }
}

EnvCard.propTypes = {
  connectDragSource: PropTypes.func.isRequired,
  isDragging: PropTypes.bool.isRequired,
  projectId: PropTypes.number.isRequired,
};

export default DragSource(ItemTypes.ENVCARD, envCardSource, collect)(
  injectIntl(EnvCard)
);
