import React, { useState, Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { injectIntl, FormattedMessage } from 'react-intl';
import isEmpty from 'lodash/isEmpty';
import { Form, TextArea, Upload } from 'choerodon-ui/pro';
import { Button, Icon } from 'choerodon-ui';
import classnames from 'classnames';
import Tips from '../../../../components/Tips/Tips';


export default injectIntl(observer(({ record, intlPrefix, prefixCls, intl: { formatMessage } }) => {
  const [uploadMode, setUploadMode] = useState(false);
  const [certDisable, setCertDisable] = useState(false);
  const [keyDisable, setKeyDisable] = useState(false);
  const [fireFaild, setFireFailed] = useState(false);

  function getClass(type) {
    const className = {
      key: classnames({
        'c7ncd-upload-select': !keyDisable,
        'c7ncd-upload-disabled': keyDisable,
      }),
      cert: classnames({
        'c7ncd-upload-select': !certDisable,
        'c7ncd-upload-disabled': certDisable,
      }),
    };

    return className[type];
  }


  function changeUploadMode() {
    setUploadMode((pre) => !pre);
  }

  function handleCheckFire(fileList) {
    const res = record.get('keyValue');
    if (isEmpty(fileList)) {
      setKeyDisable(false);
    } else if (fileList.length > 1) {
      setFireFailed(true);
      setKeyDisable(true);
    } else {
      setKeyDisable(true);
    }
  }


  return (
    <div>
      <div className={`${intlPrefix}-create-wrap-add-title`}>
        <Tips
          type="title"
          data="certificate.file.add"
          help={!uploadMode}
        />
        <Button
          type="primary"
          funcType="flat"
          onClick={changeUploadMode}
        >
          <FormattedMessage id="ctf.upload.mode" />
        </Button>
      </div>
      {uploadMode ? (
        <Fragment>
          <Form record={record}>
            <Upload
              disabled={certDisable}
              uploadImmediately={false}
              showUploadBtn={false}
              accept={['.crt']}
              fileListMaxLength={1}
              name="certValue"
            >
              <div className={getClass('cert')}>
                <Icon
                  className="c7ncd-cert-upload-icon"
                  type="add"
                />
                <div className="c7n-upload-text">Upload</div>
              </div>
            </Upload>
            <Upload
              disabled={keyDisable}
              uploadImmediately={false}
              showUploadBtn={false}
              onFileChange={handleCheckFire}
              accept={['.key']}
              fileListMaxLength={1}
              name="keyValue"
            >
              <div className={getClass('key')}>
                <Icon
                  className="c7ncd-cert-upload-icon"
                  type="add"
                />
                <div className="c7n-upload-text">Upload</div>
              </div>
            </Upload>
          </Form>
          {fireFaild && <div>只能上传一个文件</div>}
        </Fragment>
      ) : (
        <Form record={record}>
          <TextArea name="keyValue" />
          <TextArea name="certValue" />
        </Form>
      )}
    </div>
  );
}));
