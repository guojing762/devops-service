package io.choerodon.devops.infra.constant;

import java.io.File;
import java.util.regex.Pattern;

/**
 * 和GitOps有关的常量
 *
 * @author zmf
 * @since 10/28/19
 */
public class GitOpsConstants {
    private GitOpsConstants() {
    }

    /**
     * GitLab组的名称和path格式
     */
    public static final String GITLAB_GROUP_NAME_FORMAT = "%s-%s%s";
    public static final String APP_SERVICE_SUFFIX = "";
    public static final String ENV_GROUP_SUFFIX = "-gitops";
    /**
     * 集群环境库的组 ${orgCode}_${projectCode}-cluster_gitops
     * 这是集群环境库组的后缀
     */
    public static final String CLUSTER_ENV_GROUP_SUFFIX = "-cluster_gitops";

    /**
     * gitlab环境库的webhook url相对路径
     */
    public static final String GITOPS_WEBHOOK_RELATIVE_URL = "devops/webhook/git_ops";

    /**
     * choerodon系统配置库的项目名格式为: clusterCode-envCode
     */
    public static final String SYSTEM_ENV_GITLAB_PROJECT_CODE_FORMAT = "%s-%s";

    public static final String MASTER = "master";

    /**
     * local path to store env
     * gitops/${orgCode}/${proCode}/${clusterCode}/${envCode}/${envId}
     */
    public static final String LOCAL_ENV_PATH = "gitops" + File.separator + "%s" + File.separator + "%s" + File.separator + "%s" + File.separator + "%s" + File.separator + "%s";

    public static final String YAML_FILE_SUFFIX = ".yaml";

    /**
     * release文件对应的gitlab文件前缀
     */
    public static final String RELEASE_PREFIX = "release-";

    /**
     * service文件对应的gitlab文件前缀
     */
    public static final String SERVICE_PREFIX = "svc-";

    /**
     * ingress文件对应的gitlab文件前缀
     */
    public static final String INGRESS_PREFIX = "ing-";

    /**
     * 系统环境code
     */
    public static final String SYSTEM_NAMESPACE = "choerodon";

    /**
     * 0.20版本之前用于实现实例类型网络的注解键值
     */
    public static final String SERVICE_INSTANCE_ANNOTATION_KEY = "choerodon.io/network-service-instances";

    /**
     * 分支删除时的after字段会是这个值
     */
    public static final String NO_COMMIT_SHA = "0000000000000000000000000000000000000000";

    public static final String MASTER_REF = "refs/heads/master";

    public static final String BATCH_DEPLOYMENT_COMMIT_MESSAGE = "[ADD] batch deployment";

    public static final String GITLAB_CI_FILE_NAME = ".gitlab-ci.yml";

    public static final String CI_FILE_COMMIT_MESSAGE = "[UPD] update .gitlab-ci.yml";

    /**
     * 换行符
     */
    public static final String NEW_LINE = System.getProperty("line.separator", "\n");

    /**
     * 匹配换行符的字符串
     */
    public static final String NEWLINE_REGEX = "\r\n|\n|\r";
    /**
     * 用于注释shell的字符
     */
    public static final String COMMENT_STRING = "#";

    /**
     * 猪齿鱼框架的应用服务跑CI前要执行的脚本
     */
    public static final String CHOERODON_BEFORE_SCRIPT = "http_status_code=`curl -o .auto_devops.sh -s -m 10 --connect-timeout 10 -w %{http_code} \"${CHOERODON_URL}/devops/ci?token=${Token}&type=microservice\"`\n" +
            "if [ \"$http_status_code\" != \"200\" ]; then\n" +
            "  cat ./.auto_devops.sh\n" +
            "  exit 1\n" +
            "fi\n" +
            "source ./.auto_devops.sh\n";

    /**
     * 使用Token认证的sonar命令
     * SonarUrl
     * Token
     */
    public static final String SONAR_TOKEN_TEMPLATE = "mvn --batch-mode verify sonar:sonar -Dsonar.host.url=%s -Dsonar.login=%s -Dsonar.gitlab.project_id=$CI_PROJECT_PATH -Dsonar.gitlab.commit_sha=$CI_COMMIT_REF_NAME -Dsonar.gitlab.ref_name=$CI_COMMIT_REF_NAME -Dsonar.analysis.serviceGroup=$GROUP_NAME -Dsonar.analysis.commitId=$CI_COMMIT_SHA -Dsonar.projectKey=${GROUP_NAME}:${PROJECT_NAME}";

    /**
     * 使用用户名密码认证的sonar命令
     * SonarUrl
     * SonarUsername sonar的用户名
     * SonarPassword
     */
    public static final String SONAR_USER_PASSWORD_TEMPLATE = "mvn --batch-mode verify sonar:sonar -Dsonar.host.url=%s -Dsonar.login=%s -Dsonar.password=%s -Dsonar.gitlab.project_id=$CI_PROJECT_PATH -Dsonar.gitlab.commit_sha=$CI_COMMIT_REF_NAME -Dsonar.gitlab.ref_name=$CI_COMMIT_REF_NAME -Dsonar.analysis.serviceGroup=$GROUP_NAME -Dsonar.analysis.commitId=$CI_COMMIT_SHA -Dsonar.projectKey=${GROUP_NAME}:${PROJECT_NAME}";

    public static final String COMMA = ",";

    public static final String RELEASE = "release";

    public static final String SNAPSHOT = "snapshot";

    public static final String CHART_BUILD = "chart_build";

    public static final String DEV_OPS_CI_ARTIFACT_FILE_BUCKET = "devops-service-ci-artifacts";

    /**
     * ci生成的软件包的名称的模板, ${gitlabPipelineId}-${artifactName}
     */
    public static final String CI_JOB_ARTIFACT_NAME_TEMPLATE = "%s-%s.tgz";

    public static final Pattern ARTIFACT_NAME_PATTERN = Pattern.compile("[0-9a-zA-Z._-]{6,30}");


    /**
     * http或者https的地址正则表达式
     */
    public static final Pattern HTTP_URL_PATTERN = Pattern.compile("^https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)$");

    /**
     * 获取流水线ci内容的url，有三个参数：
     * 网关地址
     * 项目id
     * 流水线token
     */
    public static final String CI_CONTENT_URL_TEMPLATE = "%s/devops/v1/projects/%s/ci_contents/pipelines/%s/content.yaml";

    /**
     * 镜像的地址正则
     * 如： registry.gitlab.com/gitlab-org/gitlab-docs:11.6
     */
    public static final Pattern IMAGE_REGISTRY = Pattern.compile("^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}(/.+)*:.+$");
}
