import React, { Fragment, Component } from 'react/index';
import { Card, Button, Icon } from 'choerodon-ui';
import { Content, Header } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import { withRouter } from 'react-router-dom';

import './EmptyPage.scss';

@injectIntl
@withRouter
export default class EmptyPage extends Component {
  linkToPipelineHome = () => {
    const {
      history,
      location: {
        pathname,
        search,
      },
    } = this.props;
    history.push(`${pathname.replace(/\/edit\/\d*/, '')}${search}`);
  };

  render() {
    const { intl: { formatMessage } } = this.props;
    return (<Fragment>
      <Header title={<FormattedMessage id="pipeline.header.edit" />} />
      <Content>
        <div className="c7ncd-pipeline-empty">
          <Card title={formatMessage({ id: 'pipeline.edit.error' })}>
            <span className="c7ncd-pipeline-empty-msg">{formatMessage({ id: 'pipeline.edit.error.msg' })}</span>
            <div className="c7ncd-pipeline-empty-btn">
              <Button
                type="primary"
                funcType="raised"
                onClick={this.linkToPipelineHome}
              >
                <FormattedMessage id="pipeline.edit.back" />
              </Button>
            </div>
          </Card>
        </div>
      </Content>
    </Fragment>);
  }
}
