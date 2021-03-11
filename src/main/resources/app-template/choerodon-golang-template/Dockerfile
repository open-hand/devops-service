FROM  registry.cn-hangzhou.aliyuncs.com/choerodon-tools/golang-ci:0.8.0 AS build

COPY . /go/src/github.com/{{service.code}}

WORKDIR /go/src/github.com/{{service.code}}

RUN GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build .

FROM scratch
COPY --from=build go/src/github.com/{{service.code}}/{{service.code}} .
CMD ["./{{service.code}}"]