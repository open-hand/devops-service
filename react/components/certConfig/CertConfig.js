/**
 * @author ale0720@163.com
 * @date 2019-05-31 11:07
 */

import React from 'react';
import PropTypes from 'prop-types';
import CertUploader from './certUploader';
import CertTextarea from './certTextarea';

function CertConfig(isUploadMode, propsForm, formatMessage, initData) {
  return isUploadMode ? <CertUploader propsForm={propsForm} /> : CertTextarea(propsForm, formatMessage, initData);
}

CertConfig.propTypes = {
  isUploadMode: PropTypes.bool,
};

export default CertConfig;
