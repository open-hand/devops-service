import React, { Component, Fragment } from 'react';
import _ from "lodash";
import { observer } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Tag } from 'choerodon-ui';
import { Permission, stores } from '@choerodon/boot';
import '../AppImport.scss';

const { AppState } = stores;

@observer
class StepFour extends Component {
  render(){
    const { values, onPrevious, onCancel, onImport, store } = this.props;
    const tagDom = values.isSkipCheckPermission === 'all' ? null : _.map(values.membersInfo, t => (
      <Tag className="c7n-import-tag" key={t.iamUserId}>
        {t.loginName} {t.realName}
      </Tag>
    ));
    const { type, id: projectId, organizationId } = AppState.currentMenuType;

    return (<Fragment>
      <div className="steps-content-des">
        <FormattedMessage id="app.import.step4.des" />
      </div>
      <div className="steps-content-section">
        <div className="steps-content-list">
          <div className="steps-content-list-title">
            <FormattedMessage id="app.import.source" />：
          </div>
          <div>{values.platformType}</div>
        </div>
        <div className="steps-content-list">
          <div className="steps-content-list-title">
            <FormattedMessage id="ciPipeline.appCode" />：
          </div>
          <div>{values.code}</div>
        </div>
        <div className="steps-content-list">
          <div className="steps-content-list-title">
            <FormattedMessage id="ciPipeline.appName" />：
          </div>
          <div>{values.name}</div>
        </div>
        <div className="steps-content-list">
          <div className="steps-content-list-title">
            <FormattedMessage id="template.head" />：
          </div>
          <div>{values.template}</div>
        </div>
        <div className="steps-content-list">
          <div className="steps-content-list-title">
            <FormattedMessage id="app.import.step3" />：
          </div>
          <div>{values.isSkipCheckPermission === 'all' ? <FormattedMessage id="app.mbr.all" /> : <FormattedMessage id="app.mbr.part" />}</div>
        </div>
        {values.isSkipCheckPermission === 'part' ? <div className="steps-content-list">
          <div className="steps-content-list-title">
            <FormattedMessage id="app.authority.mbr" />：
          </div>
          <div className="tag-wrap">{tagDom}</div>
        </div> : null}
        <div className="steps-content-list">
          <div className="steps-content-list-title">
            <FormattedMessage id="elements.type.harbor" />：
          </div>
          <div>{values.harborName}</div>
        </div>
        <div className="steps-content-list">
          <div className="steps-content-list-title">
            <FormattedMessage id="elements.type.chart" />：
          </div>
          <div>{values.chartName}</div>
        </div>
      </div>
      <div className="steps-content-section">
        <Permission
          service={["devops-service.application.create"]}
          type={type}
          projectId={projectId}
          organizationId={organizationId}
        >
          <Button
            loading={store.importBtnLoading}
            type="primary"
            funcType="raised"
            onClick={onImport}
          >
            <FormattedMessage id="app.import.ok" />
          </Button>
        </Permission>
        <Button
          onClick={onPrevious}
          funcType="raised"
          className="c7n-btn-cancel"
        >
          <FormattedMessage id="previous" />
        </Button>
        <Button
          onClick={onCancel}
          funcType="raised"
          className="c7n-btn-cancel"
        >
          <FormattedMessage id="cancel" />
        </Button>
      </div>
    </Fragment>);
  }
}

export default injectIntl(StepFour);
