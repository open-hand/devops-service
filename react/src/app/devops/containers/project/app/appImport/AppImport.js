import React, { Component } from 'react';
import { observer, inject } from 'mobx-react';
import { withRouter } from 'react-router-dom';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import { Steps } from 'choerodon-ui';
import { Content, Header, Page } from '@choerodon/boot';
import { Step0, Step1, Step2, Step3 } from './steps/index';
import { STEP_FLAG, REPO_TYPE } from './Constants';
import { handleCheckerProptError } from '../../../../utils';

import '../../../main.scss';
import './AppImport.scss';

const { Step } = Steps;

@withRouter
@injectIntl
@inject('AppState')
@observer
export default class AppImport extends Component {
  state = {
    data: {},
    current: STEP_FLAG.IMPORT_ORIGIN,
  };

  nextStep = (
    {
      key,
      platformType,
      isSkipCheckPermission,
      repositoryUrl,
      accessToken,
      code,
      name,
      applicationTemplateId,
      template,
      harborConfigId,
      chartConfigId,
      harborName,
      chartName,
      userIds,
      membersInfo,
    },
    current,
  ) => {
    const { data } = this.state;
    const { IMPORT_ORIGIN, LANGUAGE_SELECT, PERMISSION_RULE } = STEP_FLAG;

    let stepData = null;

    switch (key) {
      case IMPORT_ORIGIN:
        stepData = {
          repositoryUrl,
          platformType,
        };

        if (platformType === REPO_TYPE.REPO_GITLAB) {
          stepData.accessToken = accessToken;
        }
        break;
      case LANGUAGE_SELECT:
        stepData = {
          code,
          name,
          applicationTemplateId,
          template,
        };

        break;
      case PERMISSION_RULE:
        stepData = {
          isSkipCheckPermission,
          harborConfigId,
          chartConfigId,
          harborName,
          chartName,
        };

        if (isSkipCheckPermission === 'part') {
          stepData.userIds = userIds;
          stepData.membersInfo = membersInfo;
        }
        break;
      default:
    }
    this.setState({ current, data: { ...data, ...stepData } });
  };

  prevStep = target => {
    this.setState({ current: target });
  };

  handleCancel = () => {
    const { current } = this.state;

    if (current === STEP_FLAG.IMPORT_ORIGIN) {
      const {
        AppState: {
          currentMenuType: {
            id: projectId,
            name,
            organizationId,
            type,
          },
        },
        history,
      } = this.props;

      const url = `/devops/app?type=${type}&id=${projectId}&name=${name}&organizationId=${organizationId}`;
      history.push(url);
    } else {
      this.setState({ current: STEP_FLAG.IMPORT_ORIGIN });
    }
  };

  handleImport = async () => {
    const {
      AppStore,
      history,
      AppState: {
        currentMenuType: {
          id: projectId,
          name: projectName,
          organizationId,
          type,
        },
      },
    } = this.props;
    const { data } = this.state;
    const {
      platformType,
      repositoryUrl,
      accessToken,
      name,
      code,
      applicationTemplateId,
      isSkipCheckPermission,
      userIds,
      harborConfigId,
      chartConfigId,
    } = data;
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
      isSkipCheckPermission: isSkipCheckPermission !== 'part',
      type: 'normal',
    };

    AppStore.setImportBtnLoading(true);

    const response = await AppStore.importApp(projectId, value)
      .catch(e => {
        AppStore.setImportBtnLoading(false);
        Choerodon.handleResponseError(e);
      });

    const result = handleCheckerProptError(response);

    if (result) {
      const url = `/devops/app?type=${type}&id=${projectId}&name=${projectName}&organizationId=${organizationId}`;
      history.push(url);
    }

    AppStore.setImportBtnLoading(false);
  };

  render() {
    const { current, data } = this.state;
    const {
      AppStore,
      AppState: {
        currentMenuType: {
          id: projectId,
          name,
          organizationId,
          type,
        },
      },
    } = this.props;

    const {
      IMPORT_ORIGIN,
      LANGUAGE_SELECT,
      PERMISSION_RULE,
      CONFORM_INFO,
    } = STEP_FLAG;

    const steps = _.map(STEP_FLAG, key =>
      <Step
        key={key}
        title={<FormattedMessage id={`app.import.${key}`} />}
      />);

    const stepNum = [IMPORT_ORIGIN, LANGUAGE_SELECT, PERMISSION_RULE, CONFORM_INFO];

    const stepContents = {
      [IMPORT_ORIGIN]: <Step0
        onNext={this.nextStep}
        onCancel={this.handleCancel}
        store={AppStore}
        values={data}
      />,
      [LANGUAGE_SELECT]: <Step1
        onNext={this.nextStep}
        onPrevious={this.prevStep}
        onCancel={this.handleCancel}
        store={AppStore}
        values={data}
      />,
      [PERMISSION_RULE]: <Step2
        onNext={this.nextStep}
        onPrevious={this.prevStep}
        onCancel={this.handleCancel}
        store={AppStore}
        values={data}
      />,
      [CONFORM_INFO]: <Step3
        onImport={this.handleImport}
        onPrevious={this.prevStep}
        onCancel={this.handleCancel}
        store={AppStore}
        values={data}
      />,
    };

    const backPath = `/devops/app?type=${type}&id=${projectId}&name=${name}&organizationId=${organizationId}`;

    return (
      <Page
        service={[
          'devops-service.application-market.pageListMarketAppsByProjectId',
          'devops-service.application.listByActiveAndPubAndVersion',
          'devops-service.application-market.updateVersions',
          'devops-service.application-market.update',
        ]}
      >
        <Header
          title={<FormattedMessage id="app.import" />}
          backPath={backPath}
        />
        <Content code="app.import" values={{ name }}>
          <div className="c7n-app-import-wrap">
            <Steps current={stepNum.indexOf(current)} className="steps-line">
              {steps}
            </Steps>
            <div className="steps-content">{stepContents[current]}</div>
          </div>
        </Content>
      </Page>
    );
  }
}
