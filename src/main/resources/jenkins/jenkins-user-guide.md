# Pipeline集成

## 一、声明猪齿鱼相关变量

使用猪齿鱼扩展步骤时需要提前声明以下环境变量，否则相关步骤无法正常执行

```
environment { 
    // 猪齿鱼项目id,根据实际项目id替换
    C7N_PROJECT_ID={{C7N_PROJECT_ID}}
    // 猪齿鱼应用服务编码
    C7N_APP_SERVICE_CODE='hzero-gateway'
    // 镜像、chart版本号
    C7N_VERSION="1.0.0-${BUILD_ID}"
}
```

变量说明：

* C7N_PROJECT_ID：猪齿鱼项目id。注意：需要确保安装插件时配置的认证用户拥有此项目权限，否则无法正常使用猪齿鱼相关步骤。
* C7N_APP_SERVICE_CODE：猪齿鱼应用服务编码，如`hzero-gateway`
* C7N_VERSION: 构建制品时使用的版本号，后续`C7nImageBuild`、`C7nChartBuild`步骤中会使用此变量构建对应版本制品。

## 二、编写构建脚本

以常用的Maven项目为例，通常包含以下步骤：Maven构建 -> 镜像构建 -> 推送镜像到制品库。

```
stage('Maven构建') {
    steps {
        sh "mvn clean package spring-boot:repackage -U"
    }
}
```

猪齿鱼插件中提供了自定义步骤`C7nImageBuild`用以构建、上传镜像。

```
stage('构建镜像并推送到猪齿鱼制品库') {
    steps {
        C7nImageBuild(dockerfilePath: 'Dockerfile', \
            contextPath: '.', \
            useCustomRepo: false, \
            repoCode: '')
    }
}
```

对于使用helm部署的项目，则还需要进行chart包的构建。猪齿鱼插件中提供了自定义步骤`C7nChartBuild`用以构建、上传chart包。

```
stage('构建Chart并推送到猪齿鱼制品库') {
    environment {
        // 自动寻找代码根目录下chart包路径 
        CHART_PATH="""${sh(
                returnStdout: true,
                script: 'CHART_PATH=$(find . -maxdepth 3 -name Chart.yaml) && echo ${CHART_PATH%/*} | tr -d "\n"'
        )}"""
    }
    steps { 
        C7nChartBuild("$CHART_PATH")
    }
}
```

一份基础的Maven项目Jenkinsfile如下：

```
pipeline {
    agent any
    environment { 
        // 猪齿鱼项目id
        C7N_PROJECT_ID=1
        // 猪齿鱼应用服务编码
        C7N_APP_SERVICE_CODE='hzero-gateway'
        // 镜像、chart版本号
        C7N_VERSION="1.0.0-${BUILD_ID}"
    }
    stages {
        stage('构建') {
            steps {
                sh "mvn clean package spring-boot:repackage -U"
            }
        }
        stage('构建镜像并推送到猪齿鱼制品库') {
            steps { 
                C7nImageBuild(dockerfilePath: 'Dockerfile', \
                    contextPath: '.', \
                    useCustomRepo: false, \
                    repoCode: '')
            }
        }
        stage('构建Chart并推送到猪齿鱼制品库') {
            environment {
                // 自动寻找代码根目录下chart包路径 
                CHART_PATH="""${sh(
                        returnStdout: true,
                        script: 'CHART_PATH=$(find . -maxdepth 3 -name Chart.yaml) && echo ${CHART_PATH%/*} | tr -d "\n"'
                )}"""
            }
            steps { 
                C7nChartBuild("$CHART_PATH")
            }
        }
    }
}

```

如果需要生成带有分支名、时间戳的版本号，可以通过以下方式声明

```
// 代码提交时间戳
C7N_COMMIT_TIMESTAMP="""${sh(
               returnStdout: true,
               script: 'git log -1 --date=format-local:%Y-%m-%d-%H-%M-%S --pretty=format:"%cd"'
)}"""
// 分支名
C7N_GIT_SLUG="""${sh(
       returnStdout: true,
       script: "git branch --show-current | sed 's/[^a-z0-9]/-/g' | tr -d '\n'"
)}"""
C7N_VERSION="1.0.0-${C7N_GIT_SLUG}-${C7N_COMMIT_TIMESTAMP}-${BUILD_ID}"
```

修改后的JenkinsFile如下：

```
pipeline {
    agent any
    environment { 
        // 猪齿鱼项目id
        C7N_PROJECT_ID=1
        // 猪齿鱼应用服务编码
        C7N_APP_SERVICE_CODE='hzero-gateway'
        // 代码提交时间戳
        C7N_COMMIT_TIMESTAMP="""${sh(
               returnStdout: true,
               script: 'git log -1 --date=format-local:%Y-%m-%d-%H-%M-%S --pretty=format:"%cd"'
        )}"""
        // 分支名
        C7N_GIT_SLUG="""${sh(
               returnStdout: true,
               script: "git branch --show-current | sed 's/[^a-z0-9]/-/g' | tr -d '\n'"
        )}"""
        // 镜像、chart版本号
        C7N_VERSION="1.0.0-${C7N_GIT_SLUG}-${C7N_COMMIT_TIMESTAMP}-${BUILD_ID}"
    }
    stages {
        stage('构建') {
            steps {
                sh "mvn clean package spring-boot:repackage -U"
            }
        }
        stage('构建镜像并推送到猪齿鱼制品库') {
            steps { 
                C7nImageBuild(dockerfilePath: 'Dockerfile', \
                    contextPath: '.', \
                    useCustomRepo: false, \
                    repoCode: '')
            }
        }
        stage('构建Chart并推送到猪齿鱼制品库') {
            environment {
                // 自动寻找代码根目录下chart包路径 
                CHART_PATH="""${sh(
                        returnStdout: true,
                        script: 'CHART_PATH=$(find . -maxdepth 3 -name Chart.yaml) && echo ${CHART_PATH%/*} | tr -d "\n"'
                )}"""
            }
            steps { 
                C7nChartBuild("$CHART_PATH")
            }
        }
    }
}
```

## Pipeline常用步骤

### 单元测试

使用junit收集测试报告，需要安装插件：<https://plugins.jenkins.io/junit/>

#### maven单元测试

```
stage('单元测试') {
	agent {
        docker {
          image 'registry.cn-shanghai.aliyuncs.com/c7n/cibase:1.0.0-jdk8u282-b08'
          args '-v /root/.m2:/root/.m2'
        }
    }
    steps { 
        sh "mvn clean test -Dmaven.test.failure.ignore=true -DskipTests=false -U"
    }
    post {
        always {
            // 收集测试报告
            junit 'target/surefire-reports/*.xml'
        }
    }
}
```

#### Go单元测试

```
stage('go单元测试') {
  agent {
    docker {
      image 'registry.cn-shanghai.aliyuncs.com/c7n/cibase:1.0.0-golang1.17'
    }
  }
  steps {
    sh "go test -v | go-junit-report > result.xml"
  }
  post {
    always {
      // 收集测试报告
      junit '*.xml'
    }
  }
}
```

#### Node.js单元测试

```
stage('nodejs单元测试') {
  agent {
    docker {
      image 'registry.cn-shanghai.aliyuncs.com/c7n/cibase:1.0.1-nodejs-v14.19.0'
    }
  }
  steps {
    // 安装依赖
    sh 'npm install'
    // 执行单元测试
    sh 'npm test'
  }
  post {
      always {
          // 收集测试报告
          junit 'reports/**/*.xml'
      }
  }
}
```

### 代码检查

#### Maven代码检查

Java 11项目

```
stage('代码检查') {
    agent {
      docker {
        image 'registry.hand-china.com/library/sonar-scanner:4.6'
        args '-v /root/.m2:/root/.m2'
      }
    }
    steps {
        withChoerodonEnv('sonar'){
           sh 'mvn clean verify sonar:sonar -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_LOGIN}  -Dsonar.projectKey=${SONAR_KEY_PREFIX}:${C7N_APP_SERVICE_CODE} -Dsonar.qualitygate.wait=false -Dmaven.test.failure.ignore=true -DskipTests=true' 
        }
    }
}
```

步骤`withChoerodonEnv`用于注入扫描时需要的变量，包括：

* SONAR_URL：猪齿鱼平台sonarqueb地址
* SONAR_LOGIN: 猪齿鱼平台用于执行代码扫描的用户token
* SONAR_KEY_PREFIX: 创建sonarqube项目时使用的ProjectKey前缀
  ==注意：使用此步骤前，需要正确配置环境变量`C7N_PROJECT_ID`，`C7N_APP_SERVICE_CODE`.==

参数sonar.qualitygate.wait=false表示执行sonar扫描时不等待质量门状态。如果需要在质量门失败时终止此次构建，则可以设置sonar.qualitygate.wait=true强制扫描步骤轮询等待质量门结果。

```
stage('代码检查') {
  agent {
    docker {
      image 'registry.hand-china.com/library/sonar-scanner:4.6'
      args '-v /root/.m2:/root/.m2'
    }
  }
  steps {
    withChoerodonEnv('sonar'){
       sh 'mvn clean verify sonar:sonar -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_LOGIN}  -Dsonar.projectKey=${SONAR_KEY_PREFIX}:${C7N_APP_SERVICE_CODE} -Dsonar.qualitygate.wait=true -Dmaven.test.failure.ignore=true -DskipTests=true' 
    }
  }
}
```

Java 8项目

sonarqube 9.0版本后，SonarMaven插件使用Java 11 来扫描代码。如果当前项目使用Java 8来进行构建，则需要区分构建和扫描时的Java版本。

```
stage('代码检查') {
  agent {
    docker {
      image 'registry.hand-china.com/library/sonar-scanner:4.6'
      args '-v /root/.m2:/root/.m2'
    }
  }
  steps {
	// 使用JDK8进行编译
    sh 'export JAVA_HOME=/opt/java/openjdk8 && mvn clean verify -Dmaven.test.failure.ignore=true -DskipTests=true'
	// 使用JDK11进行扫描
	withChoerodonEnv('sonar'){
       sh 'export JAVA_HOME=/opt/java/openjdk && mvn sonar:sonar -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_LOGIN}  -Dsonar.projectKey=${SONAR_KEY_PREFIX}:${C7N_APP_SERVICE_CODE} -Dsonar.qualitygate.wait=false'
    }
  }
}
```

#### SonarScanner扫描

```
stage('代码检查') {
  agent {
    docker {
      image 'registry.hand-china.com/library/sonar-scanner:4.6'
    }
  }
  steps {
	withChoerodonEnv('sonar'){
       sh 'sonar-scanner -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_LOGIN}  -Dsonar.projectKey=${SONAR_KEY_PREFIX}:${C7N_APP_SERVICE_CODE -Dsonar.sourceEncoding=UTF-8 -Dsonar.sources=./'
    }
  }
}
```

### 构建

#### Maven构建

```
stage('maven构建') {
	agent {
        docker {
          image 'registry.cn-shanghai.aliyuncs.com/c7n/cibase:1.0.0-jdk8u282-b08'
          args '-v /root/.m2:/root/.m2'
        }
    }
    steps { 
        sh "mvn clean package -U"
    }
}
```

#### Npm构建

```
stage('构建') {
	agent {
        docker {
          image 'registry.cn-shanghai.aliyuncs.com/c7n/cibase:1.0.0-jdk8u282-b08'
          args '-v /root/.m2:/root/.m2'
        }
    }
    steps { 
		// 安装依赖
        sh "npm install"
		// 分配权限
		sh "chmod -R 755 node_modules"
		// 构建
		sh "npm run build"
    }
}
```

#### 构建镜像并推送到猪齿鱼制品库(猪齿鱼插件提供)

构建镜像，并自动将镜像推送到猪齿鱼制品库，生成镜像版本。
配置参数说明：

* DockerfilePath： Dockerfile文件路径

* ContextPath：构建镜像时的上下文路径

* 使用自定仓库：推送镜像时默认将镜像推送到应用服务关联的默认仓库。配置此选项后可更改目标仓库

* 自定义仓库编码：配置使用自定义仓库后才需要填写，指定推送镜像的仓库编码

```
stage('构建镜像并推送到猪齿鱼制品库') {
    agent {
        docker {
            image 'registry.cn-shanghai.aliyuncs.com/c7n/cibase:1.0.0-jdk8u282-b08'
            args '-v /root/.m2:/root/.m2 -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }
    steps {
        C7nImageBuild(dockerfilePath: 'Dockerfile', \
        contextPath: '.', \
        useCustomRepo: false, \
        repoCode: '')
    }
}
```

参数`-v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock` 用于挂载主机docker程序用于镜像构建、上传

如果需要推送镜像到自定义仓库，则将参数`useCustomRepo`的值改为true,`repoCode`指定项目下的目标仓库

```
stage('构建镜像并推送到猪齿鱼制品库') {
	agent {
        docker {
          image 'registry.cn-shanghai.aliyuncs.com/c7n/cibase:1.0.0-jdk8u282-b08'
          args '-v /root/.m2:/root/.m2 -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }
    steps { 
        C7nImageBuild(dockerfilePath: 'Dockerfile', \
            contextPath: '.', \
            useCustomRepo: true, \
            repoCode: 'devops-test-repo')
    }
}
```

#### 构建Chart并推送到猪齿鱼制品库(猪齿鱼插件提供)

构建chart包，并自动将chart包推送到猪齿鱼制品库，生成chart版本。
配置参数说明：

* ChartPath: chart包路径，用于打包chart包

```
stage('构建Chart并推送到猪齿鱼制品库') {
    agent {
        docker {
            image 'registry.cn-shanghai.aliyuncs.com/c7n/cibase:1.0.0-jdk8u282-b08'
        }
    }
    environment {
        // 自动寻找代码根目录下chart包路径
        CHART_PATH="""`${sh(
                        returnStdout: true,
                        script: 'CHART_PATH=$`(find . -maxdepth 3 -name Chart.yaml) && echo `${CHART_PATH%/*} | tr -d "\n"'
        )}"""
    }
    steps { 
        C7nChartBuild("$`CHART_PATH")
    }
}
```

CHART_PATH声明了当前项目chart包路径，也手动指定。如果当前chart包路径为`charts/springboot`,则以下声明也可。

```
stage('构建Chart并推送到猪齿鱼制品库') {
	agent {
        docker {
          image 'registry.cn-shanghai.aliyuncs.com/c7n/cibase:1.0.0-jdk8u282-b08'
        }
    }
    steps { 
        C7nChartBuild("charts/springboot")
    }
}
```

# FreeStyleProject集成

## 一、声明环境变量

声明猪齿鱼相关环境变量供后续步骤使用。环境变量注入文档参考插件：<https://plugins.jenkins.io/envinject/>

```
// 猪齿鱼项目id
C7N_PROJECT_ID={{C7N_PROJECT_ID}}
// 猪齿鱼应用服务编码
C7N_APP_SERVICE_CODE='hzero-gateway'
// 镜像、chart版本号
C7N_VERSION="1.0.0-${BUILD_ID}"
```

## 二、编排构建步骤

以常用的Maven项目为例，通常包含以下步骤：Maven构建 -> 镜像构建 -> 推送镜像到制品库。
添加shell步骤，添加maven构建指令
![image](https://zkc7n-knowledgebase-service.obs.cn-east-3.myhuaweicloud.com:443/0/CHOERODON-HUAWEI/7eeb4d97c660444f8109e3fe641ba1e7@blob.png)

```
mvn clean package spring-boot:repackage -U
```

添加`构建镜像并推送到猪齿鱼制品库`,填写`DockerfilePath`、`ContextPath`。
![image](https://zkc7n-knowledgebase-service.obs.cn-east-3.myhuaweicloud.com:443/0/CHOERODON-HUAWEI/14352408921045af82dccd012b5e51f6@blob.png)
如果需要推送镜像到自定义仓库，则选中`使用自定义仓库`同时填写下方`自定义仓库编码`字段。
![image](https://zkc7n-knowledgebase-service.obs.cn-east-3.myhuaweicloud.com:443/0/CHOERODON-HUAWEI/76b5964a8fcd46aba6076514ffe44655@blob.png)
如果是helm应用，还需要进行chart包构建。则继续添加`构建chart并推送到猪齿鱼制品库`步骤，填写`ChartPath`来构建chart包。
![image](https://zkc7n-knowledgebase-service.obs.cn-east-3.myhuaweicloud.com:443/0/CHOERODON-HUAWEI/f653019bee2a4f7f89cd0943b25ee958@blob.png)
