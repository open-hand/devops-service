import React, { Fragment } from 'react';
import _ from 'lodash';
import { observer } from 'mobx-react';
import { injectIntl, FormattedMessage } from 'react-intl';
import { Button, Tag } from 'choerodon-ui';
import { Permission, stores } from '@choerodon/boot';
import { STEP_FLAG } from '../Constants';

import '../index.scss';

const { AppState } = stores;

function StepFour({ values, onPrevious, onCancel, onImport, store }) {
  const {
    type,
    id: projectId,
    organizationId,
  } = AppState.currentMenuType;

  const tagDom = values.isSkipCheckPermission === 'all'
    ? null
    : _.map(values.membersInfo, ({ iamUserId, loginName, realName }) => (
      <Tag className="c7n-import-tag" key={iamUserId}>
        {loginName} {realName}
      </Tag>
    ));

  return (<Fragment>
    <div className="steps-content-des">
      <FormattedMessage id="app.import.step3.des" />
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
          <FormattedMessage id="app.import.step2" />：
        </div>
        <div>{values.isSkipCheckPermission === 'all' ? <FormattedMessage id="app.mbr.all" /> :
          <FormattedMessage id="app.mbr.part" />}</div>
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
        service={['devops-service.application.create']}
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
        onClick={() => onPrevious(STEP_FLAG.PERMISSION_RULE)}
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

export default injectIntl(observer(StepFour));
