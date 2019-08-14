const config = {
  server: 'http://api.upgrade.staging.saas.hand-china.com',
  fileServer: 'http://minio.staging.saas.test.com',
  projectType: 'choerodon',
  buildType: 'single',
  // master: '@choerodon/master',
  master: './node_modules/@choerodon/master/lib/master.js',
  theme: {
    'primary-color': '#3f51b5',
    'icon-font-size-base': '16px',
  },
  dashboard: {},
  resourcesLevel: ['site', 'organization', 'project', 'user'],
};

module.exports = config;
