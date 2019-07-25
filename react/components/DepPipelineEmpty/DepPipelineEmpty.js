import React, { Component, Fragment } from 'react';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react';
import { Card, Button, Icon } from 'choerodon-ui';
import { stores, Header, Content } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import './DepPipelineEmpty.scss';
import EnvPipelineStore from '../../routes/env-pipeline/stores';
import DeploymentPipelineStore from '../../stores/project/deploymentPipeline';

const { AppState } = stores;

@observer
class DepPipelineEmpty extends Component {

  handleClick = () => {
    const { history } = this.props;
    const { projectId, name, organizationId, type } = AppState.currentMenuType;
    EnvPipelineStore.setSideType('create');
    EnvPipelineStore.setShow(true);
    history.push(`/devops/env-pipeline?type=${type}&id=${projectId}&name=${name}&organizationId=${organizationId}`);
  };

  createApp = () => {
    const { history } = this.props;
    const { projectId, name, organizationId, type } = AppState.currentMenuType;
    history.push({
      pathname: `/devops/app`,
      search: `?type=${type}&id=${projectId}&name=${name}&organizationId=${organizationId}`,
      state: { show: true, modeType: 'create' },
    });
  };

  render() {
    const { intl: { formatMessage }, title, type } = this.props;
    const { app, env } = DeploymentPipelineStore.getProRole;
    return (<Fragment>
      <Header title={title} />
      <Content>
        <div className="c7n-depPi-empty-card">
          {type === 'env' && env === 'owner' && (<Card title={formatMessage({ id: 'envPl.create' })}>
            <div className="c7n-noEnv-content">
              <FormattedMessage id="depPl.noEnv" />
              <a
                href={formatMessage({ id: 'env.link' })}
                rel="nofollow me noopener noreferrer"
                target="_blank"
              >
                <FormattedMessage id="depPl.more" /><Icon type="open_in_new" />
              </a>
            </div>
            <Button
              type="primary"
              funcType="raised"
              onClick={this.handleClick}
            >
              <FormattedMessage id="envPl.create" />
            </Button>
          </Card>)}
          {type === 'env'&& env === 'member' && (<Card title={formatMessage({ id: 'depPl.noPermission' })}>
            <FormattedMessage id="depPl.noPerDes" /><br />
            <FormattedMessage id="depPl.addPermission" />
            <a
              href={formatMessage({ id: 'env.link' })}
              rel="nofollow me noopener noreferrer"
              target="_blank"
            >
              <FormattedMessage id="depPl.more" /><Icon type="open_in_new" />
            </a>
          </Card>)}
          {type === 'app' && app === 'owner' && (<Card title={formatMessage({ id: 'app.create' })}>
            <div className="c7n-noEnv-content">
              <FormattedMessage id="empty.owner.noApp" />
              <a
                href={formatMessage({ id: 'app.link' })}
                rel="nofollow me noopener noreferrer"
                target="_blank"
              >
                <FormattedMessage id="depPl.more" /><Icon type="open_in_new" />
              </a>
            </div>
            <Button
              type="primary"
              funcType="raised"
              onClick={this.createApp}
            >
              <FormattedMessage id="app.create" />
            </Button>
          </Card>)}
          {type === 'app' && app === 'member' && (<Card title={formatMessage({ id: 'depPl.noPermission' })}>
            <FormattedMessage id="empty.member.no-app" />
            <a
              href={formatMessage({ id: 'app.link' })}
              rel="nofollow me noopener noreferrer"
              target="_blank"
            >
              <FormattedMessage id="depPl.more" /><Icon type="open_in_new" />
            </a>
          </Card>)}
        </div>
      </Content>
    </Fragment>);
  }
}

export default withRouter(injectIntl(DepPipelineEmpty));
