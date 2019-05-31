const config = {
  server: 'http://api.alpha.saas.hand-china.com',
  // server: 'http://api.staging.saas.hand-china.com',
  fileServer: 'http://minio.staging.saas.hand-china.com',
  projectType: 'choerodon',
  buildType: 'single',
  master: '@choerodon/master',
  theme: {
    'primary-color': '#3F51B5',
    'icon-font-size-base': '16px',
  },
  dashboard: {
    devops: {
      components: './react/src/app/devops/dashboard/*',
      locale: './react/src/app/devops/locale/dashboard/*',
    },
  },
  resourcesLevel: ['site', 'organization', 'project', 'user'],
};

module.exports = config;
