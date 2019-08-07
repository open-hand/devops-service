const config = {
  server: 'http://api.staging.saas.hand-china.com',
  fileServer: 'http://minio.staging.saas.hand-china.com',
  projectType: 'choerodon',
  buildType: 'single',
  master: '@choerodon/master',
  theme: {
    'primary-color': '#3f51b5',
    'icon-font-size-base': '16px',
  },
  dashboard: {},
  resourcesLevel: ['site', 'organization', 'project', 'user'],
};

module.exports = config;
