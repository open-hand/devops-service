import React, { Component, Fragment } from 'react';
import { observer } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Steps } from 'choerodon-ui';
import { Content, Header, Page, stores } from '@choerodon/boot';
import '../../../main.scss';
import './AppImport.scss';
import { Step0, Step1, Step2, Step3 } from './steps/index';

const { AppState } = stores;
const Step = Steps.Step;

@observer
class AppImport extends Component {
  constructor() {
    super(...arguments);
    this.state = {
      data: {},
      current: 0,
    };
  }

  next = (values, current) => {
    const data = this.state.data;
    switch (values.key) {
      case 'step0':
        data.repositoryUrl = values.repositoryUrl;
        data.platformType = values.platformType;
        if (values.platformType === 'gitlab') {
          data.accessToken = values.accessToken;
        }
        break;
      case 'step1':
        data.code = values.code;
        data.name = values.name;
        data.applicationTemplateId = values.applicationTemplateId;
        data.template = values.template;
        break;
      case 'step2':
        data.isSkipCheckPermission = values.isSkipCheckPermission;
        data.harborConfigId = values.harborConfigId;
        data.chartConfigId = values.chartConfigId;
        data.harborName = values.harborName;
        data.chartName = values.chartName;
        if (values.isSkipCheckPermission === 'part') {
          data.userIds = values.userIds;
          data.membersInfo = values.membersInfo;
        }
        break;
      default:
        break;
    }
    this.setState({ current, data });
  };

  prev = () => {
    const current = this.state.current - 1;
    this.setState({ current });
  };

  cancel = () => {
    const current = this.state.current;
    if (current === 0) {
      const { type, id: projectId, organizationId: orgId, name } = AppState.currentMenuType;
      const { history } = this.props;
      const url = `/devops/app?type=${type}&id=${projectId}&name=${name}&organizationId=${orgId}`;
      history.push(url);
    } else {
      this.setState({ current: 0 });
    }
  };

  importApp = () => {
    const { AppStore, history } = this.props;
    const { type, id: projectId, organizationId: orgId, name: proName } = AppState.currentMenuType;
    const { data } = this.state;
    const { platformType, repositoryUrl, accessToken, name, code, applicationTemplateId, isSkipCheckPermission, userIds, harborConfigId, chartConfigId } = data;
    const value = {
      platformType,
      accessToken,
      repositoryUrl,
      name,
      code,
      applicationTemplateId,
      userIds,
      harborConfigId,
      chartConfigId,
    };
    value.isSkipCheckPermission = isSkipCheckPermission !== 'part';
    value.type = 'normal';
    AppStore.setImportBtnLoading(true);
    AppStore.importApp(projectId, value)
      .then((data) => {
        if (data && data.failed) {
          Choerodon.prompt(data.message);
          AppStore.setImportBtnLoading(false);
        } else {
          const url = `/devops/app?type=${type}&id=${projectId}&name=${proName}&organizationId=${orgId}`;
          history.push(url);
          AppStore.setImportBtnLoading(false);
        }
      })
      .catch(e => {
        Choerodon.prompt(e);
        AppStore.setImportBtnLoading(false);
      });
  };

  render() {
    const { type, id: projectId, organizationId, name } = AppState.currentMenuType;
    const { current, data } = this.state;
    const { AppStore } = this.props;

    const steps = [{
      key: 'step0',
      title: <FormattedMessage id="app.import.step1" />,
      content: <Step0 onNext={this.next} onCancel={this.cancel} store={AppStore} values={data} />,
    }, {
      key: 'step1',
      title: <FormattedMessage id="app.import.step2" />,
      content: <Step1 onNext={this.next} onPrevious={this.prev} onCancel={this.cancel} store={AppStore}
                      values={data} />,
    }, {
      key: 'step2',
      title: <FormattedMessage id="app.import.step3" />,
      content: <Step2 onNext={this.next} onPrevious={this.prev} onCancel={this.cancel} store={AppStore}
                      values={data} />,
    }, {
      key: 'step3',
      title: <FormattedMessage id="app.import.step4" />,
      content: <Step3 onImport={this.importApp} onPrevious={this.prev} onCancel={this.cancel} store={AppStore}
                      values={data} />,
    }];

    return (
      <Page
        service={[
          'devops-service.application-market.pageListMarketAppsByProjectId',
          'devops-service.application.listByActiveAndPubAndVersion',
          'devops-service.application-market.updateVersions',
          'devops-service.application-market.update',
        ]}
      >
        <Header title={<FormattedMessage id="app.import" />}
                backPath={`/devops/app?type=${type}&id=${projectId}&name=${name}&organizationId=${organizationId}`} />
        <Content code="app.import" values={{ name }}>
          <div className="c7n-app-import-wrap">
            <Steps current={current} className="steps-line">
              {steps.map(item => <Step key={item.key} title={item.title} />)}
            </Steps>
            <div className="steps-content">{steps[current].content}</div>
          </div>
        </Content>
      </Page>
    );
  }
}

export default withRouter(injectIntl(AppImport));
