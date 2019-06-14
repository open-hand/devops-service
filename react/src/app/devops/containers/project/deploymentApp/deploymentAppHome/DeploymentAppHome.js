import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';
import { Steps } from 'choerodon-ui';
import { Content, Header, Page } from '@choerodon/boot';
import _ from 'lodash';
import AppWithVersions from '../appWithVersions';
import DeployMode from '../deployMode';
import Configuration from '../configuration';
import ConfirmInfo from '../confirmInfo';

import '../../../main.scss';
import './DeploymentApp.scss';

const { Step } = Steps;
const PERMISSION = [
  'devops-service.application.queryByAppId',
  'devops-service.application-version.queryByAppId',
  'devops-service.devops-environment.listByProjectIdAndActive',
  'devops-service.application-instance.queryValues',
  'devops-service.application-instance.formatValue',
  'devops-service.application-instance.listByAppIdAndEnvId',
  'devops-service.application-instance.deploy',
  'devops-service.application.pageByOptions',
  'devops-service.application-market.listAllApp',
  'devops-service.application-instance.previewValues',
];

@withRouter
@injectIntl
@inject('AppState')
@observer
export default class DeploymentAppHome extends Component {
  state = {
    currentStep: 0,
  };

  handleChangeStep = (index) => {
    this.setState({ currentStep: index });
  };

  componentWillUnmount() {
    this.props.DeployAppStore.initAllData();
  }

  /**
   * 点击取消按钮
   */
  handleStepCancel = () => {
    const {
      DeployAppStore,
      history,
      match: {
        params: { prevPage },
      },
    } = this.props;

    if (prevPage) {
      history.go(-1);
    } else {
      DeployAppStore.initAllData();
      this.setState({
        currentStep: 0,
      });
    }
  };

  render() {
    const {
      intl: { formatMessage },
      DeployAppStore,
      AppState: {
        currentMenuType: {
          name: projectName,
        },
      },
    } = this.props;
    const { currentStep } = this.state;

    const STEP_LIST = ['one', 'two', 'three', 'four'];
    const stepDom = _.map(STEP_LIST, item => (
      <Step key={item} title={formatMessage({ id: `deploy.step.${item}.title` })} />
    ));

    const stepProps = {
      store: DeployAppStore,
      onChange: this.handleChangeStep,
      onCancel: this.handleStepCancel,
    };

    const stepRender = [
      () => <AppWithVersions {...stepProps} />,
      () => <DeployMode {...stepProps} />,
      () => (<Configuration {...stepProps} />),
      () => <ConfirmInfo {...stepProps} />,
    ];

    return (
      <Page service={PERMISSION}>
        <Header title={<FormattedMessage id="deploy.header.title" />} />
        <Content
          className="c7n-deploy-wrapper c7ncd-step-page"
          code="deploy"
          values={{ name: projectName }}
        >
          <div className="c7ncd-step-wrap">
            <Steps className="c7ncd-step-bar" current={currentStep}>
              {stepDom}
            </Steps>
            <div className="c7ncd-step-card">{stepRender[currentStep]()}</div>
          </div>
        </Content>
      </Page>
    );
  }
}
