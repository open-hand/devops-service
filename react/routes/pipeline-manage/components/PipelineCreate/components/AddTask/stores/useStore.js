import { useLocalStore } from 'mobx-react-lite';
import { axios, Choerodon } from '@choerodon/boot';

export default function useStore() {
  return useLocalStore(() => ({
    yaml: {
      Maven: `
# 功能： 更新pom文件中指定的项目的版本号
# 说明： 此函数是猪齿鱼内置的shell函数，用于更新pom文件的版本号为对应commit的版本号,
#        (这个值在猪齿鱼内置变量 CI_COMMIT_TAG 中)
#        如果配置了依赖库，这里应该加上参数true，即：update_pom_version true
# update_pom_version


# 功能： 以jacoco为代理进行单元测试，可以分析单元测试覆盖率
# 参数说明：
#\t\t-Dmaven.test.skip=false：不跳过单元测试
#\t\t-U：每次构建检查依赖更新，可避免缓存中快照版本依赖不更新问题，但会牺牲部分性能
#\t\t-e -X ：打印调试信息，定位疑难构建问题时建议使用此参数构建
#\t\t-B：以batch模式运行，可避免日志打印时出现ArrayIndexOutOfBoundsException异常
#       -s 指定用户级别的maven settings配置文件，如果在流水线中定义了maven仓库设置，
#          运行时一份settings.xml会下载到根目录，此时可以使用-s settings指定使用
#       -gs 指定系统级别的settings.xml
# 更多帮助信息请执行此命令进行查看：mvn org.jacoco:jacoco-maven-plugin:help
# mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent test -Dmaven.test.failure.ignore=true -DskipTests=false -U -e -X -B


# 功能： springboot项目打包
# repackage可以将已经存在的jar和war格式的文件重新打包
# 打出的jar包将可以在命令行使用java -jar命令执行。
# 更多帮助信息请执行此命令进行查看：mvn spring-boot:help
#       -s 指定用户级别的maven settings配置文件，如果在流水线中定义了maven仓库设置，
#          运行时一份settings.xml会下载到根目录，此时可以使用-s settings指定使用
#       -gs 指定系统级别的settings.xml
# mvn package spring-boot:repackage


# 功能：  打包
# 参数说明：
#-Dmaven.test.skip=true：跳过单元测试，不建议
#-U：每次构建检查依赖更新，可避免缓存中快照版本依赖不更新问题，但会牺牲部分性能
#-e -X ：打印调试信息，定位疑难构建问题时建议使用此参数构建
#-B：以batch模式运行，可避免日志打印时出现ArrayIndexOutOfBoundsException异常
#       -s 指定用户级别的maven settings配置文件，如果在流水线中定义了maven仓库设置，
#          运行时一份settings.xml会下载到根目录，此时可以使用-s settings指定使用
#       -gs 指定系统级别的settings.xml
# 使用场景： 打包项目且不需要执行单元测试时使用
# 更多帮助信息请执行此命令进行查看：mvn help:describe -Dcmd=package
mvn package -Dmaven.test.skip=true -U -e -X -B


# 功能：打包并发布依赖包到私有依赖库
# 使用场景： 需要将当前项目构建结果发布到私有依赖仓库以供其他maven项目引用时使用
# 使用参数：settings.xml由用户在猪齿鱼CI流水线界面上填写信息生成存放在服务器，CI过程自动从远程服务器拉取到本地项目根目录，
#           文件名为 settings.xml，使用-s参数指定为用户settings （Alternate path for the user settings file）
#           使用-s指定settings.xml后，maven不会再去$HOME/.m2/settings.xml读取内容。
#           也可以使用-gs指定为全局 settings.xml 文件
#           此处的用户认证信息id就是在配置仓库时的仓库名称
# 更多帮助信息请执行此命令进行查看：mvn help:describe -Dcmd=deploy
# 注意：如果发布软件包失败请检查在此之前是否有执行 update_pom_version 函数，此函数会更改pom.xml指定的version值，
#       如果有执行，建议执行前将pom文件保存一份，到此处再将原始pom.xml恢复
#mvn deploy -Dmaven.test.skip=true -U -e -X -B -s settings.xml -DaltDeploymentRepository=用户认证信息id::default::仓库url`,
      npm: `
# 安装依赖
npm install --registry \${NPM_REPO} --sass-binary-site=http://npm.taobao.org/mirrors/node-sass
# 权限
chmod -R 755 node_modules
# 编译
npm run compile
echo "//\${NPM_REGISTRY}:_authToken=\${NPM_TOKEN}">.npmrc
# 发布，发包到私库需要设置token,私库地址等参数
npm publish --registry \${NPM_PUBLISH_REGISTRY}
# 构建
npm run dist
cp -r src/main/resources/lib $CI_PROJECT_DIR/docker/lib`,

      custom: `
# job模板，使用时根据需求替换
# job名称（与任务名称保持一致）
job_1:
  # job所属阶段名称，与创建阶段时保持一致
  stage: build
  # job执行内容
  script:
    - echo "hello world!"
  # 执行job的必须条件
  only:
    refs:
      - master
  # 不执行job的条件
  except:
    refs:
      - tags
# 详细定义，请参考：https://docs.gitlab.com/ee/ci/yaml/README.html
      `,
      go: `
# 指定目标操作系统为linux
GOOS=linux

# 指定目标处理器架构为amd64
GOARCH=amd64

# 更多变量设置及可选值参考go env

# 编译源文件生成可执行文件
# -x                 打印编译时会用到的所有命令
# -i                 安装依赖
# -o ./docker/app    指定生成的可执行文件, 此处直接将生成的二进制文件输出到docker的上下文目录中
# -v                 打印编译时的包名
# ./path/to/main.go  填上mian.go的路径
# 更多用法请在本地执行\`go help build\`查看
go build -x -i -o ./docker/app -v ./path/to/main.go`,
      maven_deploy: `
# 以下的两个变量 \${CHOERODON_MAVEN_REPOSITORY_ID} \${CHOERODON_MAVEN_REPO_URL} 会在选择制品库后替换为相应的值, 如果没有特别需求, 不建议更改
mvn clean install -Dmaven.springboot.skip=true -DskipTests=true deploy -DaltDeploymentRepository=\${CHOERODON_MAVEN_REPOSITORY_ID}::default::\${CHOERODON_MAVEN_REPO_URL} -s settings.xml
      `,
      upload_jar: `
# 此命令用于将之前构建的jar包发布至制品库, 并不会重新进行构建, 所以请确保使用此命令时, 此前打包的jar的结构是符合预期的
# -Dfile参数指定了之前打包出的jar包的路径
# -DpomFile指定此次发布的jar包使用的pom文件
# 以下的两个变量 \${CHOERODON_MAVEN_REPOSITORY_ID} \${CHOERODON_MAVEN_REPO_URL} 会在选择制品库后替换为相应的值, 如果没有特别需求, 不建议更改
mvn deploy:deploy-file -Dfile=target/app.jar -DpomFile=pom.xml -Durl=\${CHOERODON_MAVEN_REPO_URL} -DrepositoryId=\${CHOERODON_MAVEN_REPOSITORY_ID} -DrepositoryLayout=default
      `,
    },

    hasDefaultSonar: false,

    get getHasDefaultSonar() {
      return this.hasDefaultSonar;
    },

    setHasDefaultSonar(data) {
      this.hasDefaultSonar = data;
    },

    get getYaml() {
      return this.yaml;
    },

    axiosConnectTest(data, projectId) {
      return axios.post(`/devops/v1/projects/${projectId}/ci_jobs/sonar/connect`, data);
    },

    defaultImage: '',

    axiosGetDefaultImage() {
      return axios.get('/devops/ci/default_image');
    },

    setDefaultImage(data) {
      this.defaultImage = data;
    },

    get getDefaultImage() {
      return this.defaultImage;
    },

    axiosGetHasDefaultSonar() {
      return new Promise((resolve) => {
        axios.get('/devops/ci/has_default_sonar').then((res) => {
          this.setHasDefaultSonar(res);
          resolve();
        });
      });
    },
  }));
}
