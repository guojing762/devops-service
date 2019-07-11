package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.pagehelper.PageInfo;
import com.google.gson.Gson;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.asgard.saga.producer.StartSagaBuilder;
import io.choerodon.asgard.saga.producer.TransactionalProducer;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
<<<<<<< HEAD
=======
import io.choerodon.devops.api.vo.AppUserPermissionRepDTO;
import io.choerodon.devops.api.vo.ApplicationCodeDTO;
import io.choerodon.devops.api.vo.ApplicationImportDTO;
import io.choerodon.devops.api.vo.ApplicationRepVO;
import io.choerodon.devops.api.vo.ApplicationReqVO;
import io.choerodon.devops.api.vo.ApplicationTemplateRepVO;
import io.choerodon.devops.api.vo.ApplicationUpdateVO;
import io.choerodon.devops.api.vo.ProjectConfigDTO;
import io.choerodon.devops.api.vo.SonarContentDTO;
import io.choerodon.devops.api.vo.SonarContentsDTO;
import io.choerodon.devops.api.vo.SonarTableDTO;
import io.choerodon.devops.api.vo.UserAttrVO;
import io.choerodon.devops.api.vo.gitlab.MemberDTO;
import io.choerodon.devops.api.vo.sonar.Bug;
import io.choerodon.devops.api.vo.sonar.Component;
import io.choerodon.devops.api.vo.sonar.Facet;
import io.choerodon.devops.api.vo.sonar.Projects;
import io.choerodon.devops.api.vo.sonar.Quality;
import io.choerodon.devops.api.vo.sonar.SonarAnalyses;
import io.choerodon.devops.api.vo.sonar.SonarComponent;
import io.choerodon.devops.api.vo.sonar.SonarHistroy;
import io.choerodon.devops.api.vo.sonar.SonarTables;
import io.choerodon.devops.api.vo.sonar.Vulnerability;
>>>>>>> [IMP] applicationController重构
import io.choerodon.devops.api.validator.ApplicationValidator;
import io.choerodon.devops.api.vo.*;
import io.choerodon.devops.api.vo.gitlab.MemberDTO;
import io.choerodon.devops.api.vo.gitlab.VariableDTO;
import io.choerodon.devops.api.vo.sonar.*;
import io.choerodon.devops.app.eventhandler.payload.*;
import io.choerodon.devops.app.service.ApplicationService;
<<<<<<< HEAD
import io.choerodon.devops.domain.application.entity.*;
=======
import io.choerodon.devops.app.service.UserAttrService;
import io.choerodon.devops.domain.application.entity.AppUserPermissionE;
import io.choerodon.devops.domain.application.entity.ApplicationE;
import io.choerodon.devops.domain.application.entity.ApplicationTemplateE;
import io.choerodon.devops.domain.application.entity.DevopsAppShareE;
import io.choerodon.devops.domain.application.entity.DevopsBranchE;
import io.choerodon.devops.domain.application.entity.DevopsProjectE;
import io.choerodon.devops.domain.application.entity.ProjectE;
import io.choerodon.devops.domain.application.entity.UserAttrE;
>>>>>>> [IMP] applicationController重构
import io.choerodon.devops.domain.application.entity.gitlab.CommitE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabMemberE;
import io.choerodon.devops.domain.application.entity.gitlab.GitlabUserE;
import io.choerodon.devops.domain.application.entity.iam.UserE;
<<<<<<< HEAD
import io.choerodon.devops.domain.application.repository.*;
=======
import io.choerodon.devops.domain.application.event.DevOpsAppImportPayload;
import io.choerodon.devops.domain.application.event.DevOpsAppPayload;
import io.choerodon.devops.domain.application.event.DevOpsAppSyncPayload;
import io.choerodon.devops.domain.application.event.DevOpsUserPayload;
import io.choerodon.devops.domain.application.event.IamAppPayLoad;
import io.choerodon.devops.domain.application.factory.ApplicationFactory;
import io.choerodon.devops.domain.application.repository.AppShareRepository;
import io.choerodon.devops.domain.application.repository.AppUserPermissionRepository;
import io.choerodon.devops.domain.application.repository.ApplicationRepository;
import io.choerodon.devops.domain.application.repository.ApplicationTemplateRepository;
import io.choerodon.devops.domain.application.repository.DevopsGitRepository;
import io.choerodon.devops.domain.application.repository.DevopsProjectConfigRepository;
import io.choerodon.devops.domain.application.repository.DevopsProjectRepository;
import io.choerodon.devops.domain.application.repository.GitlabGroupMemberRepository;
import io.choerodon.devops.domain.application.repository.GitlabProjectRepository;
import io.choerodon.devops.domain.application.repository.GitlabRepository;
import io.choerodon.devops.domain.application.repository.GitlabUserRepository;
import io.choerodon.devops.domain.application.repository.IamRepository;
import io.choerodon.devops.domain.application.repository.UserAttrRepository;
>>>>>>> [IMP] applicationController重构
import io.choerodon.devops.domain.application.valueobject.Organization;
import io.choerodon.devops.domain.application.valueobject.ProjectHook;
import io.choerodon.devops.domain.application.valueobject.Variable;
import io.choerodon.devops.infra.config.ConfigurationProperties;
import io.choerodon.devops.infra.config.HarborConfigurationProperties;
import io.choerodon.devops.infra.config.RetrofitHandler;
import io.choerodon.devops.infra.dataobject.DevopsProjectDTO;
import io.choerodon.devops.infra.dataobject.UserAttrDTO;
import io.choerodon.devops.infra.dataobject.gitlab.BranchDO;
import io.choerodon.devops.infra.dataobject.gitlab.GitlabProjectDO;
import io.choerodon.devops.infra.dataobject.harbor.ProjectDetail;
import io.choerodon.devops.infra.dataobject.harbor.User;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.feign.ChartClient;
import io.choerodon.devops.infra.feign.HarborClient;
import io.choerodon.devops.infra.feign.SonarClient;
<<<<<<< HEAD
import io.choerodon.devops.infra.util.*;
=======
import io.choerodon.devops.infra.mapper.DevopsProjectMapper;
import io.choerodon.devops.infra.mapper.UserAttrMapper;
>>>>>>> [IMP] applicationController重构
import io.choerodon.websocket.tool.UUIDTool;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by younger on 2018/3/28.
 */
@Service
@EnableConfigurationProperties(HarborConfigurationProperties.class)
public class ApplicationServiceImpl implements ApplicationService {
    public static final String SEVERITIES = "severities";
    public static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    private static final Pattern REPOSITORY_URL_PATTERN = Pattern.compile("^http.*\\.git");
    private static final String GITLAB_CI_FILE = ".gitlab-ci.yml";
    private static final String DOCKER_FILE_NAME = "Dockerfile";
    private static final String ISSUE = "issue";
    private static final String COVERAGE = "coverage";
    private static final String CHART_DIR = "charts";
    private static final String SONAR = "sonar";
    private static final ConcurrentMap<Long, String> templateDockerfileMap = new ConcurrentHashMap<>();
    private static final IOFileFilter filenameFilter = new IOFileFilter() {
        @Override
        public boolean accept(File file) {
            return accept(null, file.getName());
        }

        @Override
        public boolean accept(File dir, String name) {
            return DOCKER_FILE_NAME.equals(name);
        }
    };
    private static final String MASTER = "master";
    private static final String APPLICATION = "application";
    private static final String ERROR_UPDATE_APP = "error.application.update";
    private static final String TEST = "test-application";
    private static final String DUPLICATE = "duplicate";

    private Gson gson = new Gson();

    @Value("${services.gitlab.url}")
    private String gitlabUrl;
    @Value("${spring.application.name}")
    private String applicationName;
    @Value("${services.sonarqube.url:}")
    private String sonarqubeUrl;
    @Value("${services.gateway.url}")
    private String gatewayUrl;
    @Value("${services.sonarqube.username:}")
    private String userName;
    @Value("${services.sonarqube.password:}")
    private String password;


    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private ApplicationTemplateRepository applicationTemplateRepository;
    @Autowired
    private DevopsProjectRepository devopsProjectRepository;
    @Autowired
    private GitUtil gitUtil;
    @Autowired
    private GitlabUserRepository gitlabUserRepository;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private GitlabGroupMemberRepository gitlabGroupMemberRepository;
    @Autowired
    private DevopsGitRepository devopsGitRepository;
    @Autowired
    private SagaClient sagaClient;
    @Autowired
    private AppShareRepository applicationMarketRepository;
    @Autowired
    private AppUserPermissionRepository appUserPermissionRepository;
    @Autowired
    private GitlabProjectRepository gitlabProjectRepository;
    @Autowired
    private DevopsProjectConfigRepository devopsProjectConfigRepository;
    @Autowired
    private TransactionalProducer producer;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private DevopsProjectMapper devopsProjectMapper;


    @Override
    @Saga(code = "devops-create-application",
            description = "Devops创建应用", inputSchema = "{}")
    @Transactional
    public ApplicationRepVO create(Long projectId, ApplicationReqVO applicationReqVO) {
        UserAttrVO userAttrVO = userAttrService.queryByUserId(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
//        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ApplicationValidator.checkApplication(applicationReqVO);
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        // 查询创建应用所在的gitlab应用组
        DevopsProjectDTO devopsProjectE = queryDevopsProject(projectId);
        GitlabMemberE gitlabMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()),
                TypeUtil.objToInteger(userAttrVO.getGitlabUserId()));
        if (gitlabMemberE == null || gitlabMemberE.getAccessLevel() != AccessLevel.OWNER.toValue()) {
            throw new CommonException("error.user.not.owner");
        }

        ApplicationE applicationE = getApplicationE(projectId, applicationReqVO);
        applicationE = applicationRepository.create(applicationE);

        Long appId = applicationE.getId();
        if (appId == null) {
            throw new CommonException("error.application.create.insert");
        }
        // 如果不跳过权限检查
        List<Long> userIds = applicationReqVO.getUserIds();
        if (!applicationReqVO.getIsSkipCheckPermission() && userIds != null && !userIds.isEmpty()) {
            userIds.forEach(e -> appUserPermissionRepository.create(e, appId));
        }

        IamAppPayLoad iamAppPayLoad = new IamAppPayLoad();
        iamAppPayLoad.setApplicationCategory(APPLICATION);
        iamAppPayLoad.setApplicationType(applicationReqVO.getType());
        iamAppPayLoad.setCode(applicationReqVO.getCode());
        iamAppPayLoad.setName(applicationReqVO.getName());
        iamAppPayLoad.setEnabled(true);
        iamAppPayLoad.setOrganizationId(organization.getId());
        iamAppPayLoad.setProjectId(projectId);
        iamAppPayLoad.setFrom(applicationName);

        iamRepository.createIamApp(organization.getId(), iamAppPayLoad);
        return ConvertHelper.convert(applicationRepository.queryByCode(applicationE.getCode(),
                applicationE.getProjectE().getId()), ApplicationRepVO.class);
    }

    public DevopsProjectDTO queryDevopsProject(Long projectId) {
        DevopsProjectDTO devopsProjectDTO = devopsProjectMapper.selectByPrimaryKey(projectId);
        if (devopsProjectDTO == null) {
            throw new CommonException("error.group.not.sync");
        }
        if (devopsProjectDTO.getDevopsAppGroupId() == null || devopsProjectDTO.getDevopsEnvGroupId() == null) {
            throw new CommonException("error.gitlab.groupId.select");
        }
        return devopsProjectDTO;
    }

    @Override
    public ApplicationRepVO query(Long projectId, Long applicationId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        ApplicationE applicationE = applicationRepository.query(applicationId);
        //url地址拼接
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        if (applicationE.getGitlabProjectE() != null && applicationE.getGitlabProjectE().getId() != null) {
            applicationE.initGitlabProjectEByUrl(gitlabUrl + urlSlash
                    + organization.getCode() + "-" + projectE.getCode() + "/"
                    + applicationE.getCode() + ".git");
        }

        ApplicationRepVO applicationRepVO = ConvertHelper.convert(applicationE, ApplicationRepVO.class);
        if (applicationE.getIsSkipCheckPermission()) {
            applicationRepVO.setPermission(true);
        } else {
            applicationRepVO.setPermission(false);
        }
        return applicationRepVO;
    }

    @Override
    @Saga(code = "devops-app-delete", description = "Devops删除失败应用", inputSchema = "{}")
    public void delete(Long projectId, Long appId) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        //删除应用权限
        appUserPermissionRepository.deleteByAppId(appId);
        //删除gitlab project
        ApplicationE applicationE = applicationRepository.query(appId);
        if (applicationE.getGitlabProjectE() != null) {
            Integer gitlabProjectId = applicationE.getGitlabProjectE().getId();
            GitlabProjectDO gitlabProjectDO = gitlabRepository.getProjectById(gitlabProjectId);
            if (gitlabProjectDO != null && gitlabProjectDO.getId() != null) {
                UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
                Integer gitlabUserId = TypeUtil.objToInt(userAttrE.getGitlabUserId());
                gitlabRepository.deleteProject(gitlabProjectId, gitlabUserId);
            }
        }
        applicationRepository.delete(appId);
        //删除iam应用
        DevOpsAppSyncPayload appSyncPayload = new DevOpsAppSyncPayload();
        appSyncPayload.setProjectId(projectId);
        appSyncPayload.setOrganizationId(projectE.getOrganization().getId());
        appSyncPayload.setCode(applicationE.getCode());
        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("app")
                        .withSagaCode("devops-app-delete"),
                builder -> builder
                        .withPayloadAndSerialize(appSyncPayload)
                        .withRefId(String.valueOf(appId))
                        .withSourceId(projectId));
    }

    @Saga(code = "devops-update-gitlab-users",
            description = "Devops更新gitlab用户", inputSchema = "{}")
    @Override
    public Boolean update(Long projectId, ApplicationUpdateVO applicationUpdateVO) {

        ApplicationE applicationE = ConvertHelper.convert(applicationUpdateVO, ApplicationE.class);
        applicationE.setIsSkipCheckPermission(applicationUpdateVO.getIsSkipCheckPermission());
        applicationE.initProjectE(projectId);
        applicationE.initHarborConfig(applicationUpdateVO.getHarborConfigId());
        applicationE.initChartConfig(applicationUpdateVO.getChartConfigId());

        Long appId = applicationUpdateVO.getId();
        ApplicationE oldApplicationE = applicationRepository.query(appId);

        if (!oldApplicationE.getName().equals(applicationUpdateVO.getName())) {
            applicationRepository.checkName(applicationE.getProjectE().getId(), applicationE.getName());
        }
        if (applicationRepository.update(applicationE) != 1) {
            throw new CommonException(ERROR_UPDATE_APP);
        }

<<<<<<< HEAD
        if (!oldApplicationE.getName().equals(applicationUpdateDTO.getName())) {
            updateIamApp(projectId, applicationE, oldApplicationE.getCode());
=======
        if (!oldApplicationE.getName().equals(applicationUpdateVO.getName())) {
            ProjectE projectE = iamRepository.queryIamProject(oldApplicationE.getProjectE().getId());
            Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
            IamAppPayLoad iamAppPayLoad = iamRepository.queryIamAppByCode(organization.getId(), applicationE.getCode());
            iamAppPayLoad.setName(applicationUpdateVO.getName());
            iamRepository.updateIamApp(organization.getId(), iamAppPayLoad.getId(), iamAppPayLoad);
>>>>>>> [IMP] applicationController重构
        }

        // 创建gitlabUserPayload
        DevOpsUserPayload devOpsUserPayload = new DevOpsUserPayload();
        devOpsUserPayload.setIamProjectId(projectId);
        devOpsUserPayload.setAppId(appId);
        devOpsUserPayload.setGitlabProjectId(oldApplicationE.getGitlabProjectE().getId());
        devOpsUserPayload.setIamUserIds(applicationUpdateVO.getUserIds());

        if (oldApplicationE.getIsSkipCheckPermission() && applicationUpdateVO.getIsSkipCheckPermission()) {
            return false;
        } else if (oldApplicationE.getIsSkipCheckPermission() && !applicationUpdateVO.getIsSkipCheckPermission()) {
            applicationUpdateVO.getUserIds().forEach(e -> appUserPermissionRepository.create(e, appId));
            devOpsUserPayload.setOption(1);
        } else if (!oldApplicationE.getIsSkipCheckPermission() && applicationUpdateVO.getIsSkipCheckPermission()) {
            appUserPermissionRepository.deleteByAppId(appId);
            devOpsUserPayload.setOption(2);
        } else {
            appUserPermissionRepository.deleteByAppId(appId);
            applicationUpdateVO.getUserIds().forEach(e -> appUserPermissionRepository.create(e, appId));
            devOpsUserPayload.setOption(3);
        }
        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("app")
                        .withSagaCode("evops-update-gitlab-users"),
                builder -> builder
                        .withPayloadAndSerialize(devOpsUserPayload)
                        .withRefId(String.valueOf(appId))
                        .withSourceId(projectId));
        return true;
    }

    @Saga(code = "devops-update-iam-app",
            description = "Devops同步更新iam应用", inputSchema = "{}")
    private void updateIamApp(Long projectId, ApplicationE applicationE, String code) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        DevOpsAppSyncPayload devOpsAppSyncPayload = new DevOpsAppSyncPayload();
        devOpsAppSyncPayload.setName(applicationE.getName());
        devOpsAppSyncPayload.setCode(code);
        devOpsAppSyncPayload.setProjectId(projectId);
        devOpsAppSyncPayload.setOrganizationId(organization.getId());
        devOpsAppSyncPayload.setAppId(applicationE.getId());
        String input = gson.toJson(devOpsAppSyncPayload);
        sagaClient.startSaga("devops-update-iam-app", new StartInstanceDTO(input, "app", applicationE.getId().toString(), ResourceLevel.PROJECT.value(), projectId));
    }


    @Saga(code = "devops-sync-app-active",
            description = "同步iam应用状态", inputSchema = "{}")
    @Override
    public Boolean updateActive(Long appId, Boolean active) {
        ApplicationE applicationE = applicationRepository.query(appId);
        applicationE.initActive(active);
        if (applicationRepository.update(applicationE) != 1) {
            throw new CommonException("error.application.active");
        }
        ProjectE projectE = iamRepository.queryIamProject(applicationE.getProjectE().getId());
        DevOpsAppSyncPayload opsAppSyncPayload = new DevOpsAppSyncPayload();
        opsAppSyncPayload.setActive(active);
        opsAppSyncPayload.setOrganizationId(projectE.getOrganization().getId());
        opsAppSyncPayload.setProjectId(applicationE.getProjectE().getId());
        opsAppSyncPayload.setCode(applicationE.getCode());
        producer.applyAndReturn(
                StartSagaBuilder
                        .newBuilder()
                        .withLevel(ResourceLevel.PROJECT)
                        .withRefType("app")
                        .withSagaCode("devops-sync-app-active"),
                builder -> builder
                        .withPayloadAndSerialize(opsAppSyncPayload)
                        .withRefId(String.valueOf(appId))
                        .withSourceId(applicationE.getProjectE().getId());
        return true;
    }

    @Override
    public PageInfo<ApplicationRepVO> pageByOptions(Long projectId, Boolean isActive, Boolean hasVersion,
                                                    Boolean appMarket,
                                                    String type, Boolean doPage,
                                                    PageRequest pageRequest, String params) {
        PageInfo<ApplicationE> applicationES =
                applicationRepository.listByOptions(projectId, isActive, hasVersion, appMarket, type, doPage, pageRequest, params);
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";

        initApplicationParams(projectE, organization, applicationES.getList(), urlSlash);

        PageInfo<ApplicationRepVO> resultDTOPage = ConvertPageHelper.convertPageInfo(applicationES, ApplicationRepVO.class);
        resultDTOPage.setList(setApplicationRepVOPermission(applicationES.getList(), userAttrE, projectE));
        return resultDTOPage;
    }

<<<<<<< HEAD
    @Override
    public PageInfo<ApplicationRepDTO> listByOptions(Long projectId, Boolean isActive, Boolean hasVersion, Boolean doPage, PageRequest pageRequest, String params) {
        PageInfo<ApplicationE> applicationES = applicationRepository.listByOptions(projectId, isActive, hasVersion, null, null, doPage, pageRequest, params);
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";
        initApplicationParams(projectE, organization, applicationES.getList(), urlSlash);
        return ConvertPageHelper.convertPageInfo(applicationES, ApplicationRepDTO.class);
    }

    private void getSonarUrl(ProjectE projectE, Organization organization, ApplicationE t) {
        if (!sonarqubeUrl.equals("")) {
            SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, "sonar", userName, password);
            String key = String.format("%s-%s:%s", organization.getCode(), projectE.getCode(), t.getCode());

            Map<String, String> queryContentMap = new HashMap<>();
            queryContentMap.put("additionalFields", "metrics,periods");
            queryContentMap.put("componentKey", key);
            queryContentMap.put("metricKeys", "quality_gate_details,bugs,vulnerabilities,new_bugs,new_vulnerabilities,sqale_index,code_smells,new_technical_debt,new_code_smells,coverage,tests,new_coverage,duplicated_lines_density,duplicated_blocks,new_duplicated_lines_density,ncloc,ncloc_language_distribution");
            Response<SonarComponent> sonarComponentResponse = null;
            try {
                sonarComponentResponse = sonarClient.getSonarComponet(queryContentMap).execute();
            } catch (IOException e) {
                t.initSonarUrl(null);
                return;
            }
            if (sonarComponentResponse.raw().code() != 200) {
                t.initSonarUrl(null);
                return;
            } else {
                t.initSonarUrl(sonarqubeUrl);
                return;
            }
        } else {
            t.initSonarUrl(null);
            return;
        }
    }


=======
>>>>>>> [IMP] applicationController重构
    @Override
    public PageInfo<ApplicationRepVO> pageCodeRepository(Long projectId, PageRequest pageRequest, String params) {

        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Boolean isProjectOwner = iamRepository.isProjectOwner(userAttrE.getIamUserId(), projectE);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());

        PageInfo<ApplicationE> applicationES = applicationRepository
                .listCodeRepository(projectId, pageRequest, params, isProjectOwner, userAttrE.getIamUserId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";

        initApplicationParams(projectE, organization, applicationES.getList(), urlSlash);

        return ConvertPageHelper.convertPageInfo(applicationES, ApplicationRepVO.class);
    }

    private void initApplicationParams(ProjectE projectE, Organization organization, List<ApplicationE> applicationES, String urlSlash) {
        List<String> projectKeys = new ArrayList<>();
        if (!sonarqubeUrl.equals("")) {
            SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, "sonar", userName, password);
            try {
                Response<Projects> projectsResponse = sonarClient.listProject().execute();
                if (projectsResponse != null && projectsResponse.raw().code() == 200) {
                    projectKeys = projectsResponse.body().getComponents().stream().map(Component::getKey).collect(Collectors.toList());
                }
            } catch (IOException e) {
                LOGGER.info(e.getMessage(), e);
            }
        }

        for (ApplicationE t : applicationES) {
            if (t.getGitlabProjectE() != null && t.getGitlabProjectE().getId() != null) {
                t.initGitlabProjectEByUrl(
                        gitlabUrl + urlSlash + organization.getCode() + "-" + projectE.getCode() + "/" +
                                t.getCode() + ".git");
                String key = String.format("%s-%s:%s", organization.getCode(), projectE.getCode(), t.getCode());
                if (!projectKeys.isEmpty() && projectKeys.contains(key)) {
                    t.initSonarUrl(sonarqubeUrl);
                }
            }
        }
    }

    @Override
    public List<ApplicationRepVO> listByActive(Long projectId) {
        List<ApplicationE> applicationEList = applicationRepository.listByActive(projectId);
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        String urlSlash = gitlabUrl.endsWith("/") ? "" : "/";

        initApplicationParams(projectE, organization, applicationEList, urlSlash);

        return setApplicationRepVOPermission(applicationEList, userAttrE, projectE);
    }

    private List<ApplicationRepVO> setApplicationRepVOPermission(List<ApplicationE> applicationEList,
                                                                 UserAttrE userAttrE, ProjectE projectE) {
        List<ApplicationRepVO> resultDTOList = ConvertHelper.convertList(applicationEList, ApplicationRepVO.class);
        if (userAttrE == null) {
            throw new CommonException("error.gitlab.user.sync.failed");
        }
        if (!iamRepository.isProjectOwner(userAttrE.getIamUserId(), projectE)) {
            List<Long> appIds = appUserPermissionRepository.listByUserId(userAttrE.getIamUserId()).stream()
                    .map(AppUserPermissionE::getAppId).collect(Collectors.toList());
            resultDTOList.stream().filter(e -> e != null && !e.getPermission()).forEach(e -> {
                if (appIds.contains(e.getId())) {
                    e.setPermission(true);
                }
            });
        } else {
            resultDTOList.stream().filter(Objects::nonNull).forEach(e -> e.setPermission(true));
        }
        return resultDTOList;
    }

    @Override
    public List<ApplicationRepVO> listAll(Long projectId) {
        return ConvertHelper.convertList(applicationRepository.listAll(projectId), ApplicationRepVO.class);
    }

    @Override
    public void checkName(Long projectId, String name) {
        applicationRepository.checkName(projectId, name);
    }

    @Override
    public void checkCode(Long projectId, String code) {
        ApplicationE applicationE = ApplicationFactory.createApplicationE();
        applicationE.initProjectE(projectId);
        applicationE.setCode(code);
        applicationRepository.checkCode(applicationE);
    }

    @Override
    public List<ApplicationTemplateRepVO> listTemplate(Long projectId, Boolean isPredefined) {
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        List<ApplicationTemplateE> applicationTemplateES = applicationTemplateRepository.list(projectE.getOrganization().getId())
                .stream()
                .filter(ApplicationTemplateE::getSynchro).collect(Collectors.toList());
        if (isPredefined != null && isPredefined) {
            applicationTemplateES = applicationTemplateES.stream().filter(applicationTemplateE -> applicationTemplateE.getOrganization().getId() == null).collect(Collectors.toList());
        }
        return ConvertHelper.convertList(applicationTemplateES, ApplicationTemplateRepVO.class);
    }

    /**
     * analyze location of the dockerfile in the template
     *
     * @param templateWorkDir       template work dir
     * @param applicationTemplateId application template id
     */
    private void analyzeDockerfileToMap(File templateWorkDir, Long applicationTemplateId) {
        Collection<File> dockerfile = FileUtils.listFiles(templateWorkDir, filenameFilter, TrueFileFilter.INSTANCE);
        Optional<File> df = dockerfile.stream().findFirst();
        templateDockerfileMap.putIfAbsent(applicationTemplateId, df.map(f -> f.getAbsolutePath().replace(templateWorkDir.getAbsolutePath() + System.getProperty("file.separator"), "")).orElse(DOCKER_FILE_NAME));
    }

    @Override
    public void operationApplication(DevOpsAppPayload gitlabProjectPayload) {
        DevopsProjectE devopsProjectE = devopsProjectRepository.queryByGitlabGroupId(
                TypeUtil.objToInteger(gitlabProjectPayload.getGroupId()));
        ApplicationE applicationE = applicationRepository.queryByCode(gitlabProjectPayload.getPath(),
                devopsProjectE.getProjectE().getId());
        ProjectE projectE = iamRepository.queryIamProject(devopsProjectE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        GitlabProjectDO gitlabProjectDO = gitlabRepository
                .getProjectByName(organization.getCode() + "-" + projectE.getCode(), applicationE.getCode(),
                        gitlabProjectPayload.getUserId());
        Integer gitlabProjectId = gitlabProjectDO.getId();
        if (gitlabProjectId == null) {
            gitlabProjectDO = gitlabRepository.createProject(gitlabProjectPayload.getGroupId(),
                    gitlabProjectPayload.getPath(),
                    gitlabProjectPayload.getUserId(), false);
        }
        gitlabProjectPayload.setGitlabProjectId(gitlabProjectDO.getId());

        // 为项目下的成员分配对于此gitlab项目的权限
        operateGitlabMemberPermission(gitlabProjectPayload);

        if (applicationE.getApplicationTemplateE() != null) {
            ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.query(
                    applicationE.getApplicationTemplateE().getId());
            //拉取模板
            String applicationDir = APPLICATION + System.currentTimeMillis();
            Git git = cloneTemplate(applicationTemplateE, applicationDir);
            //渲染模板里面的参数
            replaceParams(applicationE, projectE, organization, applicationDir);

            UserAttrE userAttrE = userAttrRepository.queryByGitlabUserId(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));

            // 获取push代码所需的access token
            String accessToken = getToken(gitlabProjectPayload, applicationDir, userAttrE);

            String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
            applicationE.initGitlabProjectEByUrl(repoUrl + organization.getCode()
                    + "-" + projectE.getCode() + "/" + applicationE.getCode() + ".git");
            GitlabUserE gitlabUserE = gitlabUserRepository.getGitlabUserByUserId(gitlabProjectPayload.getUserId());

            BranchDO branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), MASTER);
            if (branchDO.getName() == null) {
                gitUtil.push(git, applicationDir, applicationE.getGitlabProjectE().getRepoURL(),
                        gitlabUserE.getUsername(), accessToken);
                branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), MASTER);
                //解决push代码之后gitlab给master分支设置保护分支速度和程序运行速度不一致
                if (!branchDO.getProtected()) {
                    try {
                        gitlabRepository.createProtectBranch(gitlabProjectPayload.getGitlabProjectId(), MASTER,
                                AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(),
                                gitlabProjectPayload.getUserId());
                    } catch (CommonException e) {
                        branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), MASTER);
                        if (!branchDO.getProtected()) {
                            throw new CommonException(e);
                        }
                    }
                }
            } else {
                if (!branchDO.getProtected()) {
                    gitlabRepository.createProtectBranch(gitlabProjectPayload.getGitlabProjectId(), MASTER,
                            AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(),
                            gitlabProjectPayload.getUserId());
                }
            }
            initBranch(gitlabProjectPayload, applicationE, MASTER);
        }
        try {
            String applicationToken = getApplicationToken(gitlabProjectDO.getId(), gitlabProjectPayload.getUserId());
            applicationE.setToken(applicationToken);
            applicationE.initGitlabProjectE(TypeUtil.objToInteger(gitlabProjectPayload.getGitlabProjectId()));
            applicationE.initSynchro(true);
            applicationE.setFailed(false);
            // set project hook id for application
            setProjectHook(applicationE, gitlabProjectDO.getId(), applicationToken, gitlabProjectPayload.getUserId());
            // 更新并校验
            applicationRepository.updateSql(applicationE);
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
    }

    /**
     * get application token (set a token if there is not one in gitlab)
     *
     * @param projectId gitlab project id
     * @param userId    gitlab user id
     * @return the application token that is stored in gitlab variables
     */
    private String getApplicationToken(Integer projectId, Integer userId) {
        List<Variable> variables = gitlabRepository.getVariable(projectId, userId);
        if (variables.isEmpty()) {
            String token = GenerateUUID.generateUUID();
            gitlabRepository.addVariable(projectId, "Token", token, false, userId);
            return token;
        } else {
            return variables.get(0).getValue();
        }
    }

    /**
     * 处理当前项目成员对于此gitlab应用的权限
     *
     * @param devOpsAppPayload 此次操作相关信息
     */
    private void operateGitlabMemberPermission(DevOpsAppPayload devOpsAppPayload) {
        // 不跳过权限检查，则为gitlab项目分配项目成员权限
        if (!devOpsAppPayload.getSkipCheckPermission()) {
            if (!devOpsAppPayload.getUserIds().isEmpty()) {
                List<Long> gitlabUserIds = userAttrRepository.listByUserIds(devOpsAppPayload.getUserIds()).stream()
                        .map(UserAttrE::getGitlabUserId).collect(Collectors.toList());
                gitlabUserIds.forEach(e -> {
                    GitlabMemberE gitlabGroupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(e));
                    if (gitlabGroupMemberE != null) {
                        gitlabGroupMemberRepository.deleteMember(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(e));
                    }
                    GitlabMemberE gitlabProjectMemberE = gitlabProjectRepository.getProjectMember(devOpsAppPayload.getGitlabProjectId(), TypeUtil.objToInteger(e));
                    if (gitlabProjectMemberE == null || gitlabProjectMemberE.getId() == null) {
                        gitlabRepository.addMemberIntoProject(devOpsAppPayload.getGitlabProjectId(),
                                new MemberDTO(TypeUtil.objToInteger(e), 30, ""));
                    }
                });
            }
        }
        // 跳过权限检查，项目下所有成员自动分配权限
        else {
            List<Long> iamUserIds = iamRepository.getAllMemberIdsWithoutOwner(devOpsAppPayload.getIamProjectId());
            List<Integer> gitlabUserIds = userAttrRepository.listByUserIds(iamUserIds).stream()
                    .map(UserAttrE::getGitlabUserId).map(TypeUtil::objToInteger).collect(Collectors.toList());

            gitlabUserIds.forEach(e ->
                    updateGitlabMemberPermission(devOpsAppPayload, e));
        }
    }

    private void updateGitlabMemberPermission(DevOpsAppPayload devOpsAppPayload, Integer gitlabUserId) {
        GitlabMemberE gitlabGroupMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(gitlabUserId));
        if (gitlabGroupMemberE != null) {
            gitlabGroupMemberRepository.deleteMember(devOpsAppPayload.getGroupId(), TypeUtil.objToInteger(gitlabUserId));
        }
        GitlabMemberE gitlabProjectMemberE = gitlabProjectRepository.getProjectMember(devOpsAppPayload.getGitlabProjectId(), TypeUtil.objToInteger(gitlabUserId));
        if (gitlabProjectMemberE == null || gitlabProjectMemberE.getId() == null) {
            gitlabRepository.addMemberIntoProject(devOpsAppPayload.getGitlabProjectId(),
                    new MemberDTO(TypeUtil.objToInteger(gitlabUserId), 30, ""));
        }
    }

    /**
     * 拉取模板库到本地
     *
     * @param applicationTemplateE 模板库的信息
     * @param applicationDir       本地库地址
     * @return 本地库的git实例
     */
    private Git cloneTemplate(ApplicationTemplateE applicationTemplateE, String applicationDir) {
        String repoUrl = applicationTemplateE.getRepoUrl();
        String type = applicationTemplateE.getCode();
        if (applicationTemplateE.getOrganization().getId() != null) {
            repoUrl = repoUrl.startsWith("/") ? repoUrl.substring(1) : repoUrl;
            repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" + repoUrl : gitlabUrl + repoUrl;
            type = MASTER;
        }
        return gitUtil.clone(applicationDir, type, repoUrl);
    }

    /**
     * set project hook id for application
     *
     * @param applicationE the application entity
     * @param projectId    the gitlab project id
     * @param token        the token for project hook
     * @param userId       the gitlab user id
     */
    private void setProjectHook(ApplicationE applicationE, Integer projectId, String token, Integer userId) {
        ProjectHook projectHook = ProjectHook.allHook();
        projectHook.setEnableSslVerification(true);
        projectHook.setProjectId(projectId);
        projectHook.setToken(token);
        String uri = !gatewayUrl.endsWith("/") ? gatewayUrl + "/" : gatewayUrl;
        uri += "devops/webhook";
        projectHook.setUrl(uri);
        List<ProjectHook> projectHooks = gitlabRepository
                .getHooks(projectId, userId);
        if (projectHooks.isEmpty()) {
            applicationE.initHookId(TypeUtil.objToLong(gitlabRepository.createWebHook(
                    projectId, userId, projectHook)
                    .getId()));
        } else {
            applicationE.initHookId(TypeUtil.objToLong(projectHooks.get(0).getId()));
        }
    }

    @Override
    public void operationApplicationImport(DevOpsAppImportPayload devOpsAppImportPayload) {
        // 准备相关的数据
        DevopsProjectE devopsProjectE = devopsProjectRepository.queryByGitlabGroupId(
                TypeUtil.objToInteger(devOpsAppImportPayload.getGroupId()));
        ApplicationE applicationE = applicationRepository.queryByCode(devOpsAppImportPayload.getPath(),
                devopsProjectE.getProjectE().getId());
        ProjectE projectE = iamRepository.queryIamProject(devopsProjectE.getProjectE().getId());
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        GitlabProjectDO gitlabProjectDO = gitlabRepository
                .getProjectByName(organization.getCode() + "-" + projectE.getCode(), applicationE.getCode(),
                        devOpsAppImportPayload.getUserId());
        if (gitlabProjectDO.getId() == null) {
            gitlabProjectDO = gitlabRepository.createProject(devOpsAppImportPayload.getGroupId(),
                    devOpsAppImportPayload.getPath(),
                    devOpsAppImportPayload.getUserId(), false);
        }
        devOpsAppImportPayload.setGitlabProjectId(gitlabProjectDO.getId());

        // 为项目下的成员分配对于此gitlab项目的权限
        operateGitlabMemberPermission(devOpsAppImportPayload);

        if (applicationE.getApplicationTemplateE() != null) {
            UserAttrE userAttrE = userAttrRepository.queryByGitlabUserId(TypeUtil.objToLong(devOpsAppImportPayload.getUserId()));
            ApplicationTemplateE applicationTemplateE = applicationTemplateRepository.query(
                    applicationE.getApplicationTemplateE().getId());
            // 拉取模板
            String templateDir = APPLICATION + UUIDTool.genUuid();
            Git templateGit = cloneTemplate(applicationTemplateE, templateDir);
            // 渲染模板里面的参数
            replaceParams(applicationE, projectE, organization, templateDir);

            // clone外部代码仓库
            String applicationDir = APPLICATION + UUIDTool.genUuid();
            Git repositoryGit = gitUtil.cloneRepository(applicationDir, devOpsAppImportPayload.getRepositoryUrl(), devOpsAppImportPayload.getAccessToken());


            // 设置Application对应的gitlab项目的仓库地址
            String repoUrl = !gitlabUrl.endsWith("/") ? gitlabUrl + "/" : gitlabUrl;
            applicationE.initGitlabProjectEByUrl(repoUrl + organization.getCode()
                    + "-" + projectE.getCode() + "/" + applicationE.getCode() + ".git");

            File templateWorkDir = new File(gitUtil.getWorkingDirectory(templateDir));
            File applicationWorkDir = new File(gitUtil.getWorkingDirectory(applicationDir));

            try {
                List<Ref> refs = repositoryGit.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
                for (Ref ref : refs) {
                    String branchName;
                    if (ref.getName().equals("refs/remotes/origin/master")) {
                        continue;
                    }
                    if (ref.getName().equals("refs/heads/master")) {
                        branchName = MASTER;
                    } else {
                        branchName = ref.getName().split("/")[3];
                    }
                    repositoryGit.checkout().setName(ref.getName()).call();
                    if (!branchName.equals(MASTER)) {
                        repositoryGit.checkout().setCreateBranch(true).setName(branchName).call();
                    }


                    // 将模板库中文件复制到代码库中
                    mergeTemplateToApplication(templateWorkDir, applicationWorkDir, applicationTemplateE.getId());

                    // 获取push代码所需的access token
                    String accessToken = getToken(devOpsAppImportPayload, applicationDir, userAttrE);

                    BranchDO branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), branchName);
                    if (branchDO.getName() == null) {
                        try {
                            // 提交并推代码
                            gitUtil.commitAndPush(repositoryGit, applicationE.getGitlabProjectE().getRepoURL(), accessToken, ref.getName());
                        } catch (CommonException e) {
                            releaseResources(templateWorkDir, applicationWorkDir, templateGit, repositoryGit);
                            throw e;
                        }

                        branchDO = devopsGitRepository.getBranch(gitlabProjectDO.getId(), branchName);
                        //解决push代码之后gitlab给master分支设置保护分支速度和程序运行速度不一致
                        if (branchName.equals(MASTER)) {
                            if (!branchDO.getProtected()) {
                                try {
                                    gitlabRepository.createProtectBranch(devOpsAppImportPayload.getGitlabProjectId(), MASTER, AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(), devOpsAppImportPayload.getUserId());
                                } catch (CommonException e) {
                                    if (!devopsGitRepository.getBranch(gitlabProjectDO.getId(), MASTER).getProtected()) {
                                        throw new CommonException(e);
                                    }
                                }
                            }
                        }
                    } else {
                        if (branchName.equals(MASTER)) {
                            if (!branchDO.getProtected()) {
                                gitlabRepository.createProtectBranch(devOpsAppImportPayload.getGitlabProjectId(), MASTER,
                                        AccessLevel.MASTER.toString(), AccessLevel.MASTER.toString(),
                                        devOpsAppImportPayload.getUserId());
                            }
                        }
                    }
                    initBranch(devOpsAppImportPayload, applicationE, branchName);
                }
            } catch (GitAPIException e) {
                e.printStackTrace();
            }

            releaseResources(templateWorkDir, applicationWorkDir, templateGit, repositoryGit);
        }


        try {
            // 设置appliation的属性
            String applicationToken = getApplicationToken(gitlabProjectDO.getId(), devOpsAppImportPayload.getUserId());
            applicationE.initGitlabProjectE(TypeUtil.objToInteger(devOpsAppImportPayload.getGitlabProjectId()));
            applicationE.setToken(applicationToken);
            applicationE.setSynchro(true);

            // set project hook id for application
            setProjectHook(applicationE, gitlabProjectDO.getId(), applicationToken, devOpsAppImportPayload.getUserId());

            // 更新并校验
            if (applicationRepository.update(applicationE) != 1) {
                throw new CommonException(ERROR_UPDATE_APP);
            }
        } catch (Exception e) {
            throw new CommonException(e.getMessage(), e);
        }
    }


    /**
     * 释放资源
     */
    private void releaseResources(File templateWorkDir, File applicationWorkDir, Git templateGit, Git repositoryGit) {
        if (templateGit != null) {
            templateGit.close();
        }
        if (repositoryGit != null) {
            repositoryGit.close();
        }
        FileUtil.deleteDirectory(templateWorkDir);
        FileUtil.deleteDirectory(applicationWorkDir);
    }

    /**
     * 将模板库中的chart包，dockerfile，gitlab-ci文件复制到导入的代码仓库中
     * 复制文件前会判断文件是否存在，如果存在则不复制
     *
     * @param templateWorkDir       模板库工作目录
     * @param applicationWorkDir    应用库工作目录
     * @param applicationTemplateId application template id
     */
    private void mergeTemplateToApplication(File templateWorkDir, File applicationWorkDir, Long applicationTemplateId) {
        // ci 文件
        File appGitlabCiFile = new File(applicationWorkDir, GITLAB_CI_FILE);
        File templateGitlabCiFile = new File(templateWorkDir, GITLAB_CI_FILE);
        if (!appGitlabCiFile.exists() && templateGitlabCiFile.exists()) {
            FileUtil.copyFile(templateGitlabCiFile, appGitlabCiFile);
        }

        // Dockerfile 文件
        if (!templateDockerfileMap.containsKey(applicationTemplateId)) {
            analyzeDockerfileToMap(templateWorkDir, applicationTemplateId);
        }
        File appDockerFile = new File(applicationWorkDir, templateDockerfileMap.get(applicationTemplateId));
        File templateDockerFile = new File(templateWorkDir, templateDockerfileMap.get(applicationTemplateId));
        if (!appDockerFile.exists() && templateDockerFile.exists()) {
            FileUtil.copyFile(templateDockerFile, appDockerFile);
        }

        // chart文件夹
        File appChartDir = new File(applicationWorkDir, CHART_DIR);
        File templateChartDir = new File(templateWorkDir, CHART_DIR);
        if (!appChartDir.exists() && templateChartDir.exists()) {
            FileUtil.copyDir(templateChartDir, appChartDir);
        }
    }

    @Override
    @Saga(code = "devops-create-app-fail",
            description = "Devops设置application状态为创建失败(devops set app status create err)", inputSchema = "{}")
    public void setAppErrStatus(String input, Long projectId) {
        sagaClient.startSaga("devops-create-app-fail", new StartInstanceDTO(input, "", "", ResourceLevel.PROJECT.value(), projectId));
    }

    private void initBranch(DevOpsAppPayload gitlabProjectPayload, ApplicationE applicationE, String branchName) {
        CommitE commitE;
        try {
            commitE = devopsGitRepository.getCommit(
                    gitlabProjectPayload.getGitlabProjectId(), branchName, gitlabProjectPayload.getUserId());
        } catch (Exception e) {
            commitE = new CommitE();
        }
        DevopsBranchE devopsBranchE = new DevopsBranchE();
        devopsBranchE.setUserId(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
        devopsBranchE.setApplicationE(applicationE);
        devopsBranchE.setBranchName(branchName);
        devopsBranchE.setCheckoutCommit(commitE.getId());
        devopsBranchE.setCheckoutDate(commitE.getCommittedDate());
        devopsBranchE.setLastCommitUser(TypeUtil.objToLong(gitlabProjectPayload.getUserId()));
        devopsBranchE.setLastCommitMsg(commitE.getMessage());
        devopsBranchE.setLastCommitDate(commitE.getCommittedDate());
        devopsBranchE.setLastCommit(commitE.getId());
        devopsGitRepository.createDevopsBranch(devopsBranchE);
    }

    private void replaceParams(ApplicationE applicationE, ProjectE projectE, Organization organization,
                               String applicationDir) {
        try {
            File file = new File(gitUtil.getWorkingDirectory(applicationDir));
            Map<String, String> params = new HashMap<>();
            params.put("{{group.name}}", organization.getCode() + "-" + projectE.getCode());
            params.put("{{service.code}}", applicationE.getCode());
            FileUtil.replaceReturnFile(file, params);
        } catch (Exception e) {
            //删除模板
            gitUtil.deleteWorkingDirectory(applicationDir);
            throw new CommonException(e.getMessage(), e);
        }
    }

    private String getToken(DevOpsAppPayload gitlabProjectPayload, String applicationDir, UserAttrE userAttrE) {
        String accessToken = userAttrE.getGitlabToken();
        if (accessToken == null) {
            accessToken = gitlabRepository.createToken(gitlabProjectPayload.getGitlabProjectId(),
                    applicationDir, gitlabProjectPayload.getUserId());
            userAttrE.setGitlabToken(accessToken);
            userAttrRepository.update(userAttrE);
        }
        return accessToken;
    }


    @Override
    public String queryFile(String token, String type) {
        ApplicationE applicationE = applicationRepository.queryByToken(token);
        if (applicationE == null) {
            return null;
        }
        try {
            ProjectE projectE = iamRepository.queryIamProject(applicationE.getProjectE().getId());
            Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
            InputStream inputStream;
            ProjectConfigDTO harborProjectConfig;
            ProjectConfigDTO chartProjectConfig;
            if (applicationE.getHarborConfigE() != null) {
                harborProjectConfig = devopsProjectConfigRepository.queryByPrimaryKey(applicationE.getHarborConfigE().getId()).getConfig();
            } else {
                harborProjectConfig = devopsProjectConfigRepository.queryByIdAndType(null, ProjectConfigType.HARBOR.getType()).get(0).getConfig();
            }
            if (applicationE.getChartConfigE() != null) {
                chartProjectConfig = devopsProjectConfigRepository.queryByPrimaryKey(applicationE.getChartConfigE().getId()).getConfig();
            } else {
                chartProjectConfig = devopsProjectConfigRepository.queryByIdAndType(null, ProjectConfigType.CHART.getType()).get(0).getConfig();
            }
            if (type == null) {
                inputStream = this.getClass().getResourceAsStream("/shell/ci.sh");
            } else {
                inputStream = this.getClass().getResourceAsStream("/shell/" + type + ".sh");
            }
            Map<String, String> params = new HashMap<>();
            String groupName = organization.getCode() + "-" + projectE.getCode();
            if (harborProjectConfig.getProject() != null) {
                groupName = harborProjectConfig.getProject();
            }
            String dockerUrl = harborProjectConfig.getUrl().replace("http://", "").replace("https://", "");
            dockerUrl = dockerUrl.endsWith("/") ? dockerUrl.substring(0, dockerUrl.length() - 1) : dockerUrl;

            params.put("{{ GROUP_NAME }}", groupName);
            params.put("{{ PROJECT_NAME }}", applicationE.getCode());
            params.put("{{ PRO_CODE }}", projectE.getCode());
            params.put("{{ ORG_CODE }}", organization.getCode());
            params.put("{{ DOCKER_REGISTRY }}", dockerUrl);
            params.put("{{ DOCKER_USERNAME }}", harborProjectConfig.getUserName());
            params.put("{{ DOCKER_PASSWORD }}", harborProjectConfig.getPassword());
            params.put("{{ CHART_REGISTRY }}", chartProjectConfig.getUrl().endsWith("/") ? chartProjectConfig.getUrl().substring(0, chartProjectConfig.getUrl().length() - 1) : chartProjectConfig.getUrl());
            return FileUtil.replaceReturnString(inputStream, params);
        } catch (CommonException e) {
            return null;
        }
    }

    @Override
    public List<ApplicationCodeDTO> listByEnvId(Long projectId, Long envId, String status, Long appId) {
        List<ApplicationCodeDTO> applicationCodeDTOS = ConvertHelper
                .convertList(applicationRepository.listByEnvId(projectId, envId, status),
                        ApplicationCodeDTO.class);
        if (appId != null) {
            ApplicationE applicationE = applicationRepository.query(appId);
            ApplicationCodeDTO applicationCodeDTO = new ApplicationCodeDTO();
            BeanUtils.copyProperties(applicationE, applicationCodeDTO);
            DevopsAppShareE applicationMarketE = applicationMarketRepository.queryByAppId(appId);
            if (applicationMarketE != null) {
                applicationCodeDTO.setPublishLevel(applicationMarketE.getPublishLevel());
                applicationCodeDTO.setContributor(applicationMarketE.getContributor());
                applicationCodeDTO.setDescription(applicationMarketE.getDescription());
            }
            for (int i = 0; i < applicationCodeDTOS.size(); i++) {
                if (applicationCodeDTOS.get(i).getId().equals(applicationE.getId())) {
                    applicationCodeDTOS.remove(applicationCodeDTOS.get(i));
                }
            }
            applicationCodeDTOS.add(0, applicationCodeDTO);
        }
        return applicationCodeDTOS;
    }

    @Override
    public PageInfo<ApplicationCodeDTO> pageByIds(Long projectId, Long envId, Long appId, PageRequest pageRequest) {
        return ConvertPageHelper.convertPageInfo(applicationRepository.pageByEnvId(projectId, envId, appId, pageRequest),
                ApplicationCodeDTO.class);
    }

    @Override
    public PageInfo<ApplicationReqVO> pageByActiveAndPubAndVersion(Long projectId, PageRequest pageRequest,
                                                                   String params) {
        return ConvertPageHelper.convertPageInfo(applicationRepository
                        .listByActiveAndPubAndVersion(projectId, true, pageRequest, params),
                ApplicationReqVO.class);
    }

    @Override
    public List<AppUserPermissionRepDTO> listAllUserPermission(Long appId) {
        List<Long> userIds = appUserPermissionRepository.listAll(appId).stream().map(AppUserPermissionE::getIamUserId)
                .collect(Collectors.toList());
        List<UserE> userEList = iamRepository.listUsersByIds(userIds);
        List<AppUserPermissionRepDTO> resultList = new ArrayList<>();
        userEList.forEach(
                e -> resultList.add(new AppUserPermissionRepDTO(e.getId(), e.getLoginName(), e.getRealName())));
        return resultList;
    }

    @Override
    public Boolean validateRepositoryUrlAndToken(GitPlatformType gitPlatformType, String repositoryUrl, String accessToken) {
        if (!REPOSITORY_URL_PATTERN.matcher(repositoryUrl).matches()) {
            return Boolean.FALSE;
        }

        // 当不存在access_token时，默认将仓库识别为公开的
        return GitUtil.validRepositoryUrl(repositoryUrl, accessToken);
    }

    /**
     * ensure the repository url and access token are valid.
     *
     * @param gitPlatformType git platform type
     * @param repositoryUrl   repository url
     * @param accessToken     access token (Nullable)
     */
    private void checkRepositoryUrlAndToken(GitPlatformType gitPlatformType, String repositoryUrl, String accessToken) {
        Boolean validationResult = validateRepositoryUrlAndToken(gitPlatformType, repositoryUrl, accessToken);
        if (Boolean.FALSE.equals(validationResult)) {
            throw new CommonException("error.repository.token.invalid");
        } else if (validationResult == null) {
            throw new CommonException("error.repository.empty");
        }
    }

    @Override
    @Saga(code = "devops-import-gitlab-project", description = "Devops从外部代码平台导入到gitlab项目", inputSchema = "{}")
    public ApplicationRepVO importApp(Long projectId, ApplicationImportDTO applicationImportDTO) {
        // 获取当前操作的用户的信息
        UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        // 校验application信息的格式
        ApplicationValidator.checkApplication(applicationImportDTO);

        // 校验名称唯一性
        applicationRepository.checkName(projectId, applicationImportDTO.getName());

        // 校验code唯一性
        applicationRepository.checkCode(projectId, applicationImportDTO.getCode());

        // 校验repository（和token） 地址是否有效
        GitPlatformType gitPlatformType = GitPlatformType.from(applicationImportDTO.getPlatformType());
        checkRepositoryUrlAndToken(gitPlatformType, applicationImportDTO.getRepositoryUrl(), applicationImportDTO.getAccessToken());

        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());

        ApplicationE applicationE = fromImportDtoToEntity(applicationImportDTO);

        applicationE.initProjectE(projectId);


        applicationE.initActive(true);
        applicationE.initSynchro(false);
        applicationE.setIsSkipCheckPermission(applicationImportDTO.getIsSkipCheckPermission());
        applicationE.initHarborConfig(applicationImportDTO.getHarborConfigId());
        applicationE.initChartConfig(applicationImportDTO.getChartConfigId());

        // 查询创建应用所在的gitlab应用组
        DevopsProjectE devopsProjectE = devopsProjectRepository.queryDevopsProject(applicationE.getProjectE().getId());
        GitlabMemberE gitlabMemberE = gitlabGroupMemberRepository.getUserMemberByUserId(
                TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()),
                TypeUtil.objToInteger(userAttrE.getGitlabUserId()));

        // 校验用户的gitlab权限
        if (gitlabMemberE == null || gitlabMemberE.getAccessLevel() != AccessLevel.OWNER.toValue()) {
            throw new CommonException("error.user.not.owner");
        }

        // 创建应用
        applicationE = applicationRepository.create(applicationE);
        Long appId = applicationE.getId();

        IamAppPayLoad iamAppPayLoad = new IamAppPayLoad();
        iamAppPayLoad.setApplicationCategory(APPLICATION);
        iamAppPayLoad.setApplicationType(applicationImportDTO.getType());
        iamAppPayLoad.setCode(applicationImportDTO.getCode());
        iamAppPayLoad.setName(applicationImportDTO.getName());
        iamAppPayLoad.setEnabled(true);
        iamAppPayLoad.setOrganizationId(organization.getId());
        iamAppPayLoad.setProjectId(projectId);
        iamAppPayLoad.setFrom(applicationName);
        //iam创建应用
        iamRepository.createIamApp(organization.getId(), iamAppPayLoad);

        // 创建saga payload
        DevOpsAppImportPayload devOpsAppImportPayload = new DevOpsAppImportPayload();
        devOpsAppImportPayload.setType(APPLICATION);
        devOpsAppImportPayload.setPath(applicationImportDTO.getCode());
        devOpsAppImportPayload.setOrganizationId(organization.getId());
        devOpsAppImportPayload.setUserId(TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
        devOpsAppImportPayload.setGroupId(TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()));
        devOpsAppImportPayload.setUserIds(applicationImportDTO.getUserIds());
        devOpsAppImportPayload.setSkipCheckPermission(applicationImportDTO.getIsSkipCheckPermission());
        devOpsAppImportPayload.setAppId(appId);
        devOpsAppImportPayload.setIamProjectId(projectId);
        devOpsAppImportPayload.setPlatformType(gitPlatformType);
        devOpsAppImportPayload.setRepositoryUrl(applicationImportDTO.getRepositoryUrl());
        devOpsAppImportPayload.setAccessToken(applicationImportDTO.getAccessToken());
        devOpsAppImportPayload.setGitlabUserId(userAttrE.getGitlabUserId());

        // 如果不跳过权限检查
        List<Long> userIds = applicationImportDTO.getUserIds();
        if (!applicationImportDTO.getIsSkipCheckPermission() && userIds != null && !userIds.isEmpty()) {
            userIds.forEach(e -> appUserPermissionRepository.create(e, appId));
        }

        String input = gson.toJson(devOpsAppImportPayload);
        sagaClient.startSaga("devops-import-gitlab-project", new StartInstanceDTO(input, "", "", ResourceLevel.PROJECT.value(), projectId));

        return ConvertHelper.convert(applicationRepository.query(appId), ApplicationRepVO.class);
    }

    @Override
    public ApplicationRepVO queryByCode(Long projectId, String code) {
        return ConvertHelper.convert(applicationRepository.queryByCode(code, projectId), ApplicationRepVO.class);
    }


    @Override
    @Saga(code = "devops-create-gitlab-project",
            description = "Devops创建gitlab项目", inputSchema = "{}")
    public void createIamApplication(IamAppPayLoad iamAppPayLoad) {

        List<Long> userIds = new ArrayList<>();
        ApplicationE applicationE = applicationRepository.queryByCode(iamAppPayLoad.getCode(), iamAppPayLoad.getProjectId());
        if (applicationE == null) {
            applicationE = new ApplicationE();
            applicationE.setIsSkipCheckPermission(true);
            applicationE.setName(iamAppPayLoad.getName());
            applicationE.setCode(iamAppPayLoad.getCode());
            applicationE.initActive(true);
            applicationE.initSynchro(false);
            applicationE.initProjectE(iamAppPayLoad.getProjectId());
            applicationE.setType("normal");
            if (iamAppPayLoad.getApplicationType().equals(TEST)) {
                applicationE.setType("test");
            }
            applicationE = applicationRepository.create(applicationE);
        } else {
            //创建iam入口过来的应用直接跳过权限校验，从devops入口过来的应用选择了特定用户权限，需要给特定用户分配该用户权限
            if (!applicationE.getIsSkipCheckPermission()) {
                userIds = appUserPermissionRepository.listAll(applicationE.getId()).stream().map(AppUserPermissionE::getIamUserId).collect(Collectors.toList());
            }
        }

        //创建iam入口过来的应用直接用管理员去gitlab创建对应的project,避免没有对应项目的权限导致创建失败
        Long gitlabUserId = 1L;
        if (applicationName.equals(iamAppPayLoad.getFrom())) {
            UserAttrE userAttrE = userAttrRepository.queryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
            gitlabUserId = userAttrE.getGitlabUserId();
        }

        DevopsProjectE devopsProjectE = devopsProjectRepository.queryDevopsProject(iamAppPayLoad.getProjectId());

        //创建saga payload
        DevOpsAppPayload devOpsAppPayload = new DevOpsAppPayload();
        devOpsAppPayload.setType(APPLICATION);
        devOpsAppPayload.setPath(iamAppPayLoad.getCode());
        devOpsAppPayload.setOrganizationId(iamAppPayLoad.getOrganizationId());
        devOpsAppPayload.setUserId(TypeUtil.objToInteger(gitlabUserId));
        devOpsAppPayload.setGroupId(TypeUtil.objToInteger(devopsProjectE.getDevopsAppGroupId()));
        devOpsAppPayload.setUserIds(userIds);
        devOpsAppPayload.setSkipCheckPermission(applicationE.getIsSkipCheckPermission());
        devOpsAppPayload.setAppId(applicationE.getId());
        devOpsAppPayload.setIamProjectId(iamAppPayLoad.getProjectId());
        //0.14.0-0.15.0的时候，同步已有的app到iam，此时app已经存在gitlab project,不需要再创建
        if (applicationE.getGitlabProjectE() == null) {
            String input = gson.toJson(devOpsAppPayload);
            sagaClient.startSaga("devops-create-gitlab-project", new StartInstanceDTO(input, "", "", ResourceLevel.PROJECT.value(), iamAppPayLoad.getProjectId()));
        }
    }

    @Override
    public void updateIamApplication(IamAppPayLoad iamAppPayLoad) {
        ApplicationE applicationE = applicationRepository.queryByCode(iamAppPayLoad.getCode(), iamAppPayLoad.getProjectId());
        applicationE.setName(iamAppPayLoad.getName());
        applicationRepository.update(applicationE);
    }

    @Override
    public void deleteIamApplication(IamAppPayLoad iamAppPayLoad) {
        ApplicationE applicationE = applicationRepository.queryByCode(iamAppPayLoad.getCode(), iamAppPayLoad.getProjectId());
        if (applicationE.getGitlabProjectE() != null) {
            gitlabRepository.deleteProject(applicationE.getGitlabProjectE().getId(), 1);
        }
        applicationRepository.delete(applicationE.getId());
    }

    @Override
    public Boolean checkHarbor(String url, String userName, String password, String project, String email) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setBaseUrl(url);
        configurationProperties.setUsername(userName);
        configurationProperties.setPassword(password);
        configurationProperties.setInsecureSkipTlsVerify(false);
        configurationProperties.setProject(project);
        configurationProperties.setType("harbor");
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        HarborClient harborClient = retrofit.create(HarborClient.class);
        Call<User> getUser = harborClient.getCurrentUser();
        Response<User> userResponse = null;
        try {
            userResponse = getUser.execute();
            if (userResponse.raw().code() != 200) {
                if (userResponse.raw().code() == 401) {
                    throw new CommonException("error.harbor.user.password");
                } else {
                    throw new CommonException(userResponse.errorBody().string());
                }
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }
        //校验用户的邮箱是否匹配
        if (!email.equals(userResponse.body().getEmail())) {
            throw new CommonException("error.user.email.not.equal");
        }

        //如果传入了project,校验用户是否有project的权限
        Call<List<ProjectDetail>> listProject = harborClient.listProject(project);
        Response<List<ProjectDetail>> projectResponse = null;
        try {
            projectResponse = listProject.execute();
            if (projectResponse.body() == null) {
                throw new CommonException("error.harbor.project.permission");
            } else {
                if (project != null) {
                    List<ProjectDetail> projects = (projectResponse.body()).stream().filter(a -> (a.getName().equals(configurationProperties.getProject()))).collect(Collectors.toList());
                    if (projects.isEmpty()) {
                        throw new CommonException("error.harbor.project.permission");
                    }
                }
            }
        } catch (IOException e) {
            throw new CommonException(e);
        }
        return true;
    }


    @Override
    public Boolean checkChart(String url) {
        ConfigurationProperties configurationProperties = new ConfigurationProperties();
        configurationProperties.setBaseUrl(url);
        configurationProperties.setType("chart");
        Retrofit retrofit = RetrofitHandler.initRetrofit(configurationProperties);
        ChartClient chartClient = retrofit.create(ChartClient.class);
        chartClient.getHealth();
        Call<Object> getHealth = chartClient.getHealth();
        try {
            getHealth.execute();
        } catch (IOException e) {
            throw new CommonException(e);
        }
        return true;
    }


    private ApplicationE fromImportDtoToEntity(ApplicationImportDTO applicationImportDTO) {
        ApplicationE applicationE = ApplicationFactory.createApplicationE();
        applicationE.initProjectE(applicationImportDTO.getProjectId());
        BeanUtils.copyProperties(applicationImportDTO, applicationE);
        if (applicationImportDTO.getApplicationTemplateId() != null) {
            applicationE.initApplicationTemplateE(applicationImportDTO.getApplicationTemplateId());
        }
        applicationE.initHarborConfig(applicationImportDTO.getHarborConfigId());
        applicationE.initChartConfig(applicationImportDTO.getChartConfigId());
        return applicationE;
    }


    @Override
    public SonarContentsDTO getSonarContent(Long projectId, Long appId) {

        //没有使用sonarqube直接返回空对象
        if (sonarqubeUrl.equals("")) {
            return new SonarContentsDTO();
        }
        SonarContentsDTO sonarContentsDTO = new SonarContentsDTO();
        List<SonarContentDTO> sonarContentDTOS = new ArrayList<>();
        ApplicationE applicationE = applicationRepository.query(appId);
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());


        //初始化sonarClient
        SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);
        String key = String.format("%s-%s:%s", organization.getCode(), projectE.getCode(), applicationE.getCode());
        sonarqubeUrl = sonarqubeUrl.endsWith("/") ? sonarqubeUrl : sonarqubeUrl + "/";
        try {

            //初始化查询参数
            Map<String, String> queryContentMap = new HashMap<>();
            queryContentMap.put("additionalFields", "metrics,periods");
            queryContentMap.put("componentKey", key);
            queryContentMap.put("metricKeys", "quality_gate_details,bugs,vulnerabilities,new_bugs,new_vulnerabilities,sqale_index,code_smells,new_technical_debt,new_code_smells,coverage,tests,new_coverage,duplicated_lines_density,duplicated_blocks,new_duplicated_lines_density,ncloc,ncloc_language_distribution");

            //根据project-key查询sonarqube项目内容
            Response<SonarComponent> sonarComponentResponse = sonarClient.getSonarComponet(queryContentMap).execute();
            if (sonarComponentResponse.raw().code() != 200) {
                if (sonarComponentResponse.raw().code() == 404) {
                    return new SonarContentsDTO();
                }
                if (sonarComponentResponse.raw().code() == 401) {
                    throw new CommonException("error.sonarqube.user");
                }
                throw new CommonException(sonarComponentResponse.errorBody().string());
            }
            if (sonarComponentResponse.body() == null) {
                return new SonarContentsDTO();
            }
            if (sonarComponentResponse.body().getPeriods() != null && sonarComponentResponse.body().getPeriods().size() > 0) {
                sonarContentsDTO.setDate(sonarComponentResponse.body().getPeriods().get(0).getDate());
                sonarContentsDTO.setMode(sonarComponentResponse.body().getPeriods().get(0).getMode());
                sonarContentsDTO.setParameter(sonarComponentResponse.body().getPeriods().get(0).getParameter());
            } else {
                Map<String, String> analyseMap = new HashMap<>();
                analyseMap.put("project", key);
                analyseMap.put("ps", "3");

                //查询上一次的分析时间
                Response<SonarAnalyses> sonarAnalyses = sonarClient.getAnalyses(analyseMap).execute();
                if (sonarAnalyses.raw().code() == 200 && sonarAnalyses.body().getAnalyses() != null && sonarAnalyses.body().getAnalyses().size() > 0) {
                    sonarContentsDTO.setDate(sonarAnalyses.body().getAnalyses().get(0).getDate());
                }
            }

            //分类型对sonarqube project查询返回的结果进行处理
            sonarComponentResponse.body().getComponent().getMeasures().stream().forEach(measure -> {
                SonarQubeType sonarQubeType = SonarQubeType.forValue(String.valueOf(measure.getMetric()));
                switch (sonarQubeType) {
                    case BUGS:
                        SonarContentDTO bug = new SonarContentDTO();
                        bug.setKey(measure.getMetric());
                        bug.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        bug.setUrl(String.format("%sproject/issues?id=%s&resolved=false&types=BUG", sonarqubeUrl, key));
                        try {
                            Map<String, String> queryBugMap = getQueryMap(key, "BUG", false);
                            Response<Bug> bugResponse = sonarClient.getBugs(queryBugMap).execute();
                            if (bugResponse.raw().code() != 200) {
                                throw new CommonException(bugResponse.errorBody().string());
                            }
                            List<Facet> facets = bugResponse.body().getFacets();
                            getRate(bug, facets);
                        } catch (IOException e) {
                            throw new CommonException(e);
                        }
                        sonarContentDTOS.add(bug);
                        break;
                    case VULNERABILITIES:
                        SonarContentDTO vulnerabilities = new SonarContentDTO();
                        vulnerabilities.setKey(measure.getMetric());
                        vulnerabilities.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        vulnerabilities.setUrl(String.format("%sproject/issues?id=%s&resolved=false&types=VULNERABILITY", sonarqubeUrl, key));
                        try {
                            Map<String, String> queryVulnerabilitiesMap = getQueryMap(key, "VULNERABILITY", false);
                            Response<Vulnerability> vulnerabilityResponse = sonarClient.getVulnerability(queryVulnerabilitiesMap).execute();
                            if (vulnerabilityResponse.raw().code() != 200) {
                                throw new CommonException(vulnerabilityResponse.errorBody().string());
                            }
                            List<Facet> facets = vulnerabilityResponse.body().getFacets();
                            getRate(vulnerabilities, facets);
                        } catch (IOException e) {
                            throw new CommonException(e);
                        }
                        sonarContentDTOS.add(vulnerabilities);
                        break;
                    case NEW_BUGS:
                        SonarContentDTO newBug = new SonarContentDTO();
                        newBug.setKey(measure.getMetric());
                        newBug.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        newBug.setUrl(String.format("%sproject/issues?id=%s&resolved=false&sinceLeakPeriod=true&types=BUG", sonarqubeUrl, key));
                        try {
                            Map<String, String> queryNewBugMap = getQueryMap(key, "BUG", true);

                            Response<Bug> newBugResponse = sonarClient.getNewBugs(queryNewBugMap).execute();
                            if (newBugResponse.raw().code() != 200) {
                                throw new CommonException(newBugResponse.errorBody().string());
                            }
                            List<Facet> facets = newBugResponse.body().getFacets();
                            getRate(newBug, facets);
                        } catch (IOException e) {
                            throw new CommonException(e);
                        }
                        sonarContentDTOS.add(newBug);
                        break;
                    case NEW_VULNERABILITIES:
                        SonarContentDTO newVulnerabilities = new SonarContentDTO();
                        newVulnerabilities.setKey(measure.getMetric());
                        newVulnerabilities.setValue(measure.getPeriods().get(0).getValue());
                        newVulnerabilities.setUrl(String.format("%sproject/issues?id=%s&resolved=false&sinceLeakPeriod=true&types=VULNERABILITY", sonarqubeUrl, key));
                        try {
                            Map<String, String> queryNewVulnerabilitiesMap = getQueryMap(key, "VULNERABILITY", true);
                            Response<Vulnerability> newVulnerabilityResponse = sonarClient.getNewVulnerability(queryNewVulnerabilitiesMap).execute();
                            if (newVulnerabilityResponse.raw().code() != 200) {
                                throw new CommonException(newVulnerabilityResponse.errorBody().string());
                            }
                            List<Facet> facets = newVulnerabilityResponse.body().getFacets();
                            getRate(newVulnerabilities, facets);
                        } catch (IOException e) {
                            throw new CommonException(e);
                        }
                        sonarContentDTOS.add(newVulnerabilities);
                        break;
                    case SQALE_INDEX:
                        SonarContentDTO debt = new SonarContentDTO();
                        debt.setKey(measure.getMetric());
                        debt.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        double day = measure.getValue() == null ? 0 : TypeUtil.objTodouble(measure.getValue()) / 480;
                        double hour = measure.getValue() == null ? 0 : TypeUtil.objTodouble(measure.getValue()) / 60;
                        if (day >= 1) {
                            debt.setValue(String.format("%sd", Math.round(day)));
                        } else if (hour >= 1) {
                            debt.setValue(String.format("%sh", Math.round(hour)));
                        } else {
                            debt.setValue(String.format("%s%s", Math.round(TypeUtil.objTodouble(measure.getValue() == null ? 0 : measure.getValue())), measure.getValue() == null ? "" : "min"));
                        }
                        debt.setUrl(String.format("%sproject/issues?facetMode=effort&id=%s&resolved=false&types=CODE_SMELL", sonarqubeUrl, key));
                        sonarContentDTOS.add(debt);
                        break;
                    case CODE_SMELLS:
                        SonarContentDTO codeSmells = new SonarContentDTO();
                        codeSmells.setKey(measure.getMetric());
                        double result = measure.getValue() == null ? 0 : TypeUtil.objToLong(measure.getValue()) / 1000;
                        if (result > 0) {
                            if (TypeUtil.objToLong(measure.getValue()) % 1000 == 0) {
                                codeSmells.setValue(String.format("%sK", result));
                            } else {
                                BigDecimal codeSmellDecimal = new BigDecimal(result);
                                codeSmells.setValue(String.format("%sK", codeSmellDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
                            }
                        } else {
                            codeSmells.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        }
                        codeSmells.setUrl(String.format("%sproject/issues?id=%s&resolved=false&types=CODE_SMELL", sonarqubeUrl, key));
                        sonarContentDTOS.add(codeSmells);
                        break;
                    case NEW_TECHNICAL_DEBT:
                        SonarContentDTO newDebt = new SonarContentDTO();
                        newDebt.setKey(measure.getMetric());
                        double newDay = TypeUtil.objTodouble(measure.getPeriods().get(0).getValue()) / 480;
                        double newHour = TypeUtil.objTodouble(measure.getPeriods().get(0).getValue()) / 60;
                        if (newDay >= 1) {
                            newDebt.setValue(String.format("%sd", Math.round(newDay)));
                        } else if (newHour >= 1) {
                            newDebt.setValue(String.format("%sh", Math.round(newHour)));
                        } else {
                            newDebt.setValue(String.format("%s%s", measure.getPeriods().get(0).getValue(), measure.getPeriods().get(0).getValue().equals("0") ? "" : "min"));
                        }
                        newDebt.setUrl(String.format("%sproject/issues?facetMode=effort&id=%s&resolved=false&sinceLeakPeriod=true&types=CODE_SMELL", sonarqubeUrl, key));
                        sonarContentDTOS.add(newDebt);
                        break;
                    case NEW_CODE_SMELLS:
                        SonarContentDTO newCodeSmells = new SonarContentDTO();
                        newCodeSmells.setKey(measure.getMetric());
                        double newResult = TypeUtil.objToLong(measure.getPeriods().get(0).getValue()) / 1000;
                        if (newResult > 0) {
                            if (TypeUtil.objToLong(measure.getPeriods().get(0).getValue()) % 1000 == 0) {
                                newCodeSmells.setValue(String.format("%sK", newResult));
                            } else {
                                BigDecimal codeSmellDecimal = new BigDecimal(newResult);
                                newCodeSmells.setValue(String.format("%sK", codeSmellDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
                            }
                        } else {
                            newCodeSmells.setValue(measure.getPeriods().get(0).getValue());
                        }
                        newCodeSmells.setUrl(String.format("%sproject/issues?id=%s&resolved=false&sinceLeakPeriod=true&types=CODE_SMELL", sonarqubeUrl, key));
                        sonarContentDTOS.add(newCodeSmells);
                        break;
                    case COVERAGE:
                        SonarContentDTO coverage = new SonarContentDTO();
                        coverage.setKey(measure.getMetric());
                        coverage.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        coverage.setUrl(String.format("%scomponent_measures?id=%s&metric=coverage", sonarqubeUrl, key));
                        sonarContentDTOS.add(coverage);
                        break;
                    case NEW_COVERAGE:
                        SonarContentDTO newCoverage = new SonarContentDTO();
                        newCoverage.setKey(measure.getMetric());
                        BigDecimal codeSmellDecimal = new BigDecimal(measure.getPeriods().get(0).getValue());
                        newCoverage.setValue(String.format("%s", codeSmellDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
                        newCoverage.setUrl(String.format("%scomponent_measures?id=%s&metric=new_coverage", sonarqubeUrl, key));
                        sonarContentDTOS.add(newCoverage);
                        break;
                    case DUPLICATED_LINES_DENSITY:
                        SonarContentDTO duplicated = new SonarContentDTO();
                        duplicated.setKey(measure.getMetric());
                        duplicated.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        duplicated.setUrl(String.format("%scomponent_measures?id=%s&metric=duplicated_lines_density", sonarqubeUrl, key));
                        if (TypeUtil.objTodouble(measure.getValue()) >= 0 && TypeUtil.objTodouble(measure.getValue()) < 3) {
                            duplicated.setRate("A");
                        } else if (TypeUtil.objTodouble(measure.getValue()) >= 3 && TypeUtil.objTodouble(measure.getValue()) < 10) {
                            duplicated.setRate("B");
                        } else if (TypeUtil.objTodouble(measure.getValue()) >= 10 && TypeUtil.objTodouble(measure.getValue()) < 20) {
                            duplicated.setRate("C");
                        } else {
                            duplicated.setRate("D");
                        }
                        sonarContentDTOS.add(duplicated);
                        break;
                    case DUPLICATED_BLOCKS:
                        SonarContentDTO duplicatedBlocks = new SonarContentDTO();
                        duplicatedBlocks.setKey(measure.getMetric());
                        duplicatedBlocks.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        duplicatedBlocks.setUrl(String.format("%scomponent_measures?id=%s&metric=duplicated_blocks", sonarqubeUrl, key));
                        sonarContentDTOS.add(duplicatedBlocks);
                        break;
                    case NEW_DUPLICATED_LINES_DENSITY:
                        SonarContentDTO newDuplicated = new SonarContentDTO();
                        newDuplicated.setKey(measure.getMetric());
                        if (TypeUtil.objTodouble(measure.getPeriods().get(0).getValue()) == 0) {
                            newDuplicated.setValue("0");
                        } else {
                            BigDecimal b = new BigDecimal(TypeUtil.objTodouble(measure.getPeriods().get(0).getValue()));
                            newDuplicated.setValue(TypeUtil.objToString(b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
                        }
                        newDuplicated.setUrl(String.format("%scomponent_measures?id=%s&metric=new_duplicated_lines_density", sonarqubeUrl, key));
                        sonarContentDTOS.add(newDuplicated);
                        break;
                    case NCLOC:
                        SonarContentDTO ncloc = new SonarContentDTO();
                        ncloc.setKey(measure.getMetric());
                        double nclocResult = TypeUtil.objTodouble(measure.getValue()) / 1000;
                        if (nclocResult >= 0) {
                            if (TypeUtil.objToLong(measure.getValue()) % 1000 == 0) {
                                ncloc.setValue(String.format("%sK", nclocResult));
                            } else {
                                BigDecimal nclocDecimal = new BigDecimal(nclocResult);
                                ncloc.setValue(String.format("%sK", nclocDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()));
                            }
                        } else {
                            ncloc.setValue(measure.getValue());
                        }
                        if (TypeUtil.objToLong(measure.getValue()) > 0 && TypeUtil.objToLong(measure.getValue()) < 1000) {
                            ncloc.setRate("XS");
                        } else if (TypeUtil.objToLong(measure.getValue()) >= 1000 && TypeUtil.objToLong(measure.getValue()) < 10000) {
                            ncloc.setRate("S");
                        } else if (TypeUtil.objToLong(measure.getValue()) >= 10000 && TypeUtil.objToLong(measure.getValue()) < 100000) {
                            ncloc.setRate("M");
                        } else if (TypeUtil.objToLong(measure.getValue()) >= 100000 && TypeUtil.objToLong(measure.getValue()) < 500000) {
                            ncloc.setRate("L");
                        } else {
                            ncloc.setRate("XL");
                        }
                        sonarContentDTOS.add(ncloc);
                        break;
                    case TESTS:
                        SonarContentDTO test = new SonarContentDTO();
                        test.setKey(measure.getMetric());
                        test.setValue(measure.getValue() == null ? "0" : measure.getValue());
                        test.setUrl(String.format("%scomponent_measures?id=%s&metric=tests", sonarqubeUrl, key));
                        sonarContentDTOS.add(test);
                        break;
                    case NCLOC_LANGUAGE_DISTRIBUTION:
                        SonarContentDTO nclocLanguage = new SonarContentDTO();
                        nclocLanguage.setKey(measure.getMetric());
                        nclocLanguage.setValue(measure.getValue());
                        sonarContentDTOS.add(nclocLanguage);
                        break;
                    case QUALITY_GATE_DETAILS:
                        Quality quality = gson.fromJson(measure.getValue(), Quality.class);
                        sonarContentsDTO.setStatus(quality.getLevel());
                        break;
                    default:
                        break;
                }
            });
            sonarContentsDTO.setSonarContents(sonarContentDTOS);
        } catch (IOException e) {
            throw new CommonException(e);
        }
        return sonarContentsDTO;
    }

    @Override
    public SonarTableDTO getSonarTable(Long projectId, Long appId, String type, Date startTime, Date endTime) {
        if (sonarqubeUrl.equals("")) {
            return new SonarTableDTO();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(endTime);
        c.add(Calendar.DAY_OF_MONTH, 1);
        Date tomorrow = c.getTime();
        SonarTableDTO sonarTableDTO = new SonarTableDTO();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0000");
        ApplicationE applicationE = applicationRepository.query(appId);
        ProjectE projectE = iamRepository.queryIamProject(projectId);
        Organization organization = iamRepository.queryOrganizationById(projectE.getOrganization().getId());
        SonarClient sonarClient = RetrofitHandler.getSonarClient(sonarqubeUrl, SONAR, userName, password);
        String key = String.format("%s-%s:%s", organization.getCode(), projectE.getCode(), applicationE.getCode());
        sonarqubeUrl = sonarqubeUrl.endsWith("/") ? sonarqubeUrl : sonarqubeUrl + "/";
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("component", key);
        queryMap.put("ps", "1000");
        if (ISSUE.equals(type)) {
            queryMap.put("metrics", "bugs,code_smells,vulnerabilities");
            try {
                Response<SonarTables> sonarTablesResponse = sonarClient.getSonarTables(queryMap).execute();
                if (sonarTablesResponse.raw().code() != 200) {
                    if (sonarTablesResponse.raw().code() == 404) {
                        return new SonarTableDTO();
                    }
                    if (sonarTablesResponse.raw().code() == 401) {
                        throw new CommonException("error.sonarqube.user");
                    }
                    throw new CommonException(sonarTablesResponse.errorBody().string());
                }
                List<String> bugs = new ArrayList<>();
                List<String> dates = new ArrayList<>();
                List<String> codeSmells = new ArrayList<>();
                List<String> vulnerabilities = new ArrayList<>();
                sonarTablesResponse.body().getMeasures().stream().forEach(sonarTableMeasure -> {
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.BUGS.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            bugs.add(sonarHistroy.getValue());
                            dates.add(sonarHistroy.getDate());
                        });
                        sonarTableDTO.setDates(dates);
                        sonarTableDTO.setBugs(bugs);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.CODE_SMELLS.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            codeSmells.add(sonarHistroy.getValue());
                        });
                        sonarTableDTO.setCodeSmells(codeSmells);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.VULNERABILITIES.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            vulnerabilities.add(sonarHistroy.getValue());
                        });
                        sonarTableDTO.setVulnerabilities(vulnerabilities);
                    }
                });
            } catch (IOException e) {
                throw new CommonException(e);
            }
        }
        if (COVERAGE.equals(type)) {
            queryMap.put("metrics", "lines_to_cover,uncovered_lines,coverage");
            try {
                Response<SonarTables> sonarTablesResponse = sonarClient.getSonarTables(queryMap).execute();
                if (sonarTablesResponse.raw().code() != 200) {
                    if (sonarTablesResponse.raw().code() == 404) {
                        return new SonarTableDTO();
                    }
                    throw new CommonException(sonarTablesResponse.errorBody().string());
                }
                List<String> linesToCover = new ArrayList<>();
                List<String> dates = new ArrayList<>();
                List<String> unCoverLines = new ArrayList<>();
                List<String> coverLines = new ArrayList<>();
                List<String> coverage = new ArrayList<>();
                sonarTablesResponse.body().getMeasures().stream().forEach(sonarTableMeasure -> {
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.COVERAGE.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            coverage.add(sonarHistroy.getValue());
                        });
                        sonarTableDTO.setCoverage(coverage);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.LINES_TO_COVER.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            linesToCover.add(sonarHistroy.getValue());
                            dates.add(sonarHistroy.getDate());
                        });
                        sonarTableDTO.setDates(dates);
                        sonarTableDTO.setLinesToCover(linesToCover);
                    }

                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.UNCOVERED_LINES.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            unCoverLines.add(sonarHistroy.getValue());
                        });
                    }
                });
                for (int i = 0; i < linesToCover.size(); i++) {
                    coverLines.add(TypeUtil.objToString(TypeUtil.objToLong(linesToCover.get(i)) - TypeUtil.objToLong(unCoverLines.get(i))));
                }
                sonarTableDTO.setCoverLines(coverLines);
            } catch (IOException e) {
                throw new CommonException(e);
            }
        }
        if (DUPLICATE.equals(type)) {
            queryMap.put("metrics", "ncloc,duplicated_lines,duplicated_lines_density");
            try {
                Response<SonarTables> sonarTablesResponse = sonarClient.getSonarTables(queryMap).execute();
                if (sonarTablesResponse.raw().code() != 200) {
                    if (sonarTablesResponse.raw().code() == 404) {
                        return new SonarTableDTO();
                    }
                    throw new CommonException(sonarTablesResponse.errorBody().string());
                }
                List<String> nclocs = new ArrayList<>();
                List<String> dates = new ArrayList<>();
                List<String> duplicatedLines = new ArrayList<>();
                List<String> duplicatedLinesRate = new ArrayList<>();
                sonarTablesResponse.body().getMeasures().stream().forEach(sonarTableMeasure -> {
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.NCLOC.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            nclocs.add(sonarHistroy.getValue());
                            dates.add(sonarHistroy.getDate());
                        });
                        sonarTableDTO.setNclocs(nclocs);
                        sonarTableDTO.setDates(dates);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.DUPLICATED_LINES.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy ->
                                duplicatedLines.add(sonarHistroy.getValue())
                        );
                        sonarTableDTO.setDuplicatedLines(duplicatedLines);
                    }
                    if (sonarTableMeasure.getMetric().equals(SonarQubeType.DUPLICATED_LINES_DENSITY.getType())) {
                        sonarTableMeasure.getHistory().stream().filter(sonarHistroy ->
                                getHistory(startTime, tomorrow, sdf, sonarHistroy)
                        ).forEach(sonarHistroy -> {
                            duplicatedLinesRate.add(sonarHistroy.getValue());
                        });
                        sonarTableDTO.setDuplicatedLinesRate(duplicatedLinesRate);
                    }
                });
            } catch (IOException e) {
                throw new CommonException(e);
            }
        }
        return sonarTableDTO;
    }

    private boolean getHistory(Date startTime, Date endTime, SimpleDateFormat sdf, SonarHistroy sonarHistroy) {
        try {
            return sdf.parse(sonarHistroy.getDate()).compareTo(startTime) >= 0 && sdf.parse(sonarHistroy.getDate()).compareTo(endTime) <= 0;
        } catch (ParseException e) {
            throw new CommonException(e);
        }
    }


    private void getRate(SonarContentDTO sonarContentDTO, List<Facet> facets) {
        sonarContentDTO.setRate("A");
        facets.stream().filter(facet -> facet.getProperty().equals(SEVERITIES)).forEach(facet -> {
            facet.getValues().stream().forEach(value -> {
                if (value.getVal().equals(Rate.MINOR.getRate()) && value.getCount() >= 1) {
                    if (sonarContentDTO.getRate().equals("A")) {
                        sonarContentDTO.setRate("B");
                    }
                }
                if (value.getVal().equals(Rate.MAJOR.getRate()) && value.getCount() >= 1) {
                    if (!sonarContentDTO.getRate().equals("D") && !sonarContentDTO.getRate().equals("E")) {
                        sonarContentDTO.setRate("C");
                    }
                }
                if (value.getVal().equals(Rate.CRITICAL.getRate()) && value.getCount() >= 1) {
                    if (!sonarContentDTO.getRate().equals("E")) {
                        sonarContentDTO.setRate("D");
                    }
                }
                if (value.getVal().equals(Rate.BLOCKER.getRate()) && value.getCount() >= 1) {
                    sonarContentDTO.setRate("E");
                }
            });
        });
    }


    private Map<String, String> getQueryMap(String key, String type, Boolean newAdd) {
        Map<String, String> map = new HashMap<>();
        map.put("componentKeys", key);
        map.put("s", "FILE_LINE");
        map.put("resolved", "false");
        map.put("types", type);
        if (newAdd) {
            map.put("sinceLeakPeriod", "true");
        }
        map.put("ps", "100");
        map.put("facets", "severities,types");
        map.put("additionalFields", "_all");
        return map;
    }


    private ApplicationE getApplicationE(Long projectId, ApplicationReqVO applicationReqDTO) {
        ApplicationE applicationE = ConvertHelper.convert(applicationReqDTO, ApplicationE.class);
        applicationE.initProjectE(projectId);
        applicationRepository.checkName(applicationE.getProjectE().getId(), applicationE.getName());
        applicationRepository.checkCode(applicationE);
        applicationE.initActive(true);
        applicationE.initSynchro(false);
        applicationE.setIsSkipCheckPermission(applicationReqDTO.getIsSkipCheckPermission());
        applicationE.initHarborConfig(applicationReqDTO.getHarborConfigId());
        applicationE.initChartConfig(applicationReqDTO.getChartConfigId());
        return applicationE;
    }

}
