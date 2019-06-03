/**
 * @author ale0720@163.com
 * @date 2019-05-31 11:07
 */

import React from 'react';
import PropTypes from 'prop-types';
import CertUploader from './certUploader';
import CertTextarea from './certTextarea';

function CertConfig({ isUploadMode }) {
  return isUploadMode ? <CertUploader /> : <CertTextarea />;
}

CertConfig.propTypes = {
  isUploadMode: PropTypes.bool,
};

export default CertConfig;
