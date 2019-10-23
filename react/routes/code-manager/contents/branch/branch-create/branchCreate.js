import React, { useState } from 'react';
import { withRouter } from 'react-router-dom';
import { observer } from 'mobx-react-lite';
import { Form, TextField, Select, Row, Col } from 'choerodon-ui/pro';
import { Content, stores, Choerodon } from '@choerodon/boot';
import { injectIntl, FormattedMessage } from 'react-intl';
import _ from 'lodash';
import '../../../../main.less';
import './index.less';
import '../index.less';
import MouserOverWrapper from '../../../../../components/MouseOverWrapper';
import DevPipelineStore from '../../../stores/DevPipelineStore';
import InterceptMask from '../../../../../components/intercept-mask';
import { useFormStore } from './store';

const { AppState } = stores;
const { Option, OptGroup } = Select;
const FormItem = Form.Item;
const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 100 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 26 },
  },
};
const selectStyle = {
  width: 340,
};
function BranchCreate(props) {
  const [submitting, setSubmitting] = useState(false);
  const {
    branchTypeDS,
    issueNameDs,
    formDs,
  } = useFormStore();
  function handleOk() {
    handleClose();
  }
  function handleClose() {
    props.onClose(false);
  }
  /**
   * 获取列表的icon
   * @param type 分支类型
   * @returns {*}
   */
  const getIcon = (type) => {
    let icon;
    switch (type) {
      case 'feature':
        icon = <span className="c7n-branch-icon icon-feature">F</span>;
        break;
      case 'bugfix':
        icon = <span className="c7n-branch-icon icon-develop">B</span>;
        break;
      case 'hotfix':
        icon = <span className="c7n-branch-icon icon-hotfix">H</span>;
        break;
      case 'master':
        icon = <span className="c7n-branch-icon icon-master">M</span>;
        break;
      case 'release':
        icon = <span className="c7n-branch-icon icon-release">R</span>;
        break;
      default:
        icon = <span className="c7n-branch-icon icon-custom">C</span>;
    }
    return icon;
  };
  const renderer = ({ text }) => (
    text ? <div style={{ width: '100%' }}>
      {getIcon(text)}
      <span className="c7n-branch-text">{text}</span>
    </div> : null
  );
  const optionRenderer = ({ text }) => (
    renderer({ text })
  );
  return (
    <Content className="sidebar-content c7n-createBranch">
      <Form
        dataSet={formDs}
      >
        <Row gutter={10}>
          <Col span={12}>
            <Select dataSet={issueNameDs} name="issueName" />
          </Col>
        </Row>
        <Row gutter={10}>
          <Col span={12}>
            <Select name="branchOrigin" />
          </Col>
        </Row>
        <Row gutter={10}>
          <Col span={8}>
            <Select searchable name="branchType" dataSet={branchTypeDS} renderer={renderer} optionRenderer={optionRenderer}>
              {['feature', 'bugfix', 'release', 'hotfix', 'custom'].map(
                (s) => (
                  <Option value={s} key={s} title={s}>
                    {s}
                  </Option>
                )
              )}
            </Select>
          </Col>
          <Col span={15}>
            <TextField name="branchName" />
          </Col>
        </Row>
      </Form>
      <InterceptMask visible={submitting} />
    </Content>
  );
}
export default withRouter(injectIntl(observer(BranchCreate)));
