package io.choerodon.devops.app.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.base.domain.Sort;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.validator.DevopsCertificationValidator;
import io.choerodon.devops.api.vo.C7nCertificationDTO;
import io.choerodon.devops.api.vo.CertificationVO;
import io.choerodon.devops.api.vo.OrgCertificationDTO;
import io.choerodon.devops.api.vo.ProjectVO;
import io.choerodon.devops.api.vo.iam.entity.*;
import io.choerodon.devops.app.service.CertificationService;
import io.choerodon.devops.app.service.DevopsEnvironmentService;
import io.choerodon.devops.app.service.GitlabGroupMemberService;
import io.choerodon.devops.domain.application.repository.*;
import io.choerodon.devops.domain.application.valueobject.C7nCertification;
import io.choerodon.devops.domain.application.valueobject.certification.*;
import io.choerodon.devops.infra.dto.CertificationDTO;
import io.choerodon.devops.infra.dto.CertificationFileDO;
import io.choerodon.devops.infra.enums.*;
import io.choerodon.devops.infra.handler.ClusterConnectionHandler;
import io.choerodon.devops.infra.mapper.DevopsCertificationFileMapper;
import io.choerodon.devops.infra.mapper.DevopsCertificationMapper;
import io.choerodon.devops.infra.util.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by n!Ck
 * Date: 2018/8/20
 * Time: 17:47
 * Description:
 */
@Service
public class CertificationServiceImpl implements CertificationService {

    private static final String CERT_PREFIX = "cert-";
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String UPLOAD = "upload";


    @Autowired
    private DevopsEnvironmentRepository devopsEnvironmentRepository;
    @Autowired
    private IamRepository iamRepository;
    @Autowired
    private DevopsCertificationValidator devopsCertificationValidator;
    @Autowired
    private UserAttrRepository userAttrRepository;
    @Autowired
    private GitlabGroupMemberService gitlabGroupMemberService;
    @Autowired
    private ClusterConnectionHandler clusterConnectionHandler;
    @Autowired
    private DevopsEnvFileResourceRepository devopsEnvFileResourceRepository;
    @Autowired
    private GitlabRepository gitlabRepository;
    @Autowired
    private DevopsEnvCommandRepository devopsEnvCommandRepository;
    @Autowired
    private DevopsEnvUserPermissionRepository devopsEnvUserPermissionRepository;
    @Autowired
    private DevopsEnvironmentService devopsEnvironmentService;

    @Autowired
    private DevopsCertificationMapper devopsCertificationMapper;
    @Autowired
    private DevopsCertificationFileMapper devopsCertificationFileMapper;

    private Gson gson = new Gson();


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void baseCreate(Long projectId, C7nCertificationDTO certificationDTO,
                           MultipartFile key, MultipartFile cert, Boolean isGitOps) {

        Long envId = certificationDTO.getEnvId();

        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);

        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);


        ProjectVO projectE = iamRepository.queryIamProject(projectId);
        String path = String.format("tmp%s%s%s%s", FILE_SEPARATOR, projectE.getCode(), FILE_SEPARATOR, devopsEnvironmentE.getCode());

        String certFileName;
        String keyFileName;

        //如果是选择上传文件方式
        if (certificationDTO.getType().equals(UPLOAD)) {
            if (key != null && cert != null) {
                certFileName = cert.getOriginalFilename();
                keyFileName = key.getOriginalFilename();
                certificationDTO.setKeyValue(FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, key))));
                certificationDTO.setCertValue(FileUtil.getFileContent(new File(FileUtil.multipartFileToFile(path, cert))));
            } else {
                certFileName = String.format("%s.%s", GenerateUUID.generateUUID().substring(0, 5), "crt");
                keyFileName = String.format("%s.%s", GenerateUUID.generateUUID().substring(0, 5), "key");
                FileUtil.saveDataToFile(path, certFileName, certificationDTO.getCertValue());
                FileUtil.saveDataToFile(path, keyFileName, certificationDTO.getKeyValue());
            }
            File certPath = new File(path + FILE_SEPARATOR + certFileName);
            File keyPath = new File(path + FILE_SEPARATOR + keyFileName);
            try {
                SslUtil.validate(certPath, keyPath);
            } catch (CommonException e) {
                FileUtil.deleteFile(certPath);
                FileUtil.deleteFile(keyPath);
                throw e;
            }
            FileUtil.deleteFile(certPath);
            FileUtil.deleteFile(keyPath);
        }

        String certName = certificationDTO.getCertName();
        String type = certificationDTO.getType();
        List<String> domains = certificationDTO.getDomains();


        CertificationFileDO certificationFileDO = null;
        //如果创建的时候选择证书
        if (certificationDTO.getCertId() != null) {
            certificationDTO.setType(UPLOAD);
            certificationFileDO = baseQueryCertFile(baseQueryById(certificationDTO.getCertId()).getId());
        }

        devopsCertificationValidator.checkCertification(envId, certName);


        // status operating
        CertificationDTO newCertificationDTO = new CertificationDTO(null,
                certName, devopsEnvironmentE.getId(), gson.toJson(domains), CertificationStatus.OPERATING.getStatus(), certificationDTO.getCertId());

        C7nCertification c7nCertification = null;

        if (!isGitOps) {
            String envCode = devopsEnvironmentE.getCode();

            c7nCertification = getC7nCertification(
                    certName, type, domains, certificationFileDO == null ? certificationDTO.getKeyValue() : certificationFileDO.getKeyFile(), certificationFileDO == null ? certificationDTO.getCertValue() : certificationFileDO.getCertFile(), envCode);

            createAndStore(newCertificationDTO, c7nCertification);

            // sent certification to agent
            ResourceConvertToYamlHandler<C7nCertification> certificationOperation = new ResourceConvertToYamlHandler<>();
            certificationOperation.setType(c7nCertification);
            operateEnvGitLabFile(certName, devopsEnvironmentE, c7nCertification);

        } else {
            createAndStore(newCertificationDTO, c7nCertification);
        }

    }

    /**
     * create certification, command and store cert file
     *
     * @param certificationDTO the information of certification
     * @param c7nCertification the certification (null_able)
     */
    private void createAndStore(CertificationDTO certificationDTO, C7nCertification c7nCertification) {
        // create
        certificationDTO = baseCreate(certificationDTO);
        Long certId = certificationDTO.getId();

        CertificationDTO updateCertificationDTO = new CertificationDTO();
        updateCertificationDTO.setId(certificationDTO.getId());
        updateCertificationDTO.setCommandId(createCertCommandE(CommandType.CREATE.getType(), certId, null));
        // cert command
        baseUpdateCommandId(updateCertificationDTO);
        // store crt & key if type is upload
        storeCertFile(c7nCertification, certId);
    }


    private void storeCertFile(C7nCertification c7nCertification, Long certId) {
        if (c7nCertification != null) {
            CertificationExistCert existCert = c7nCertification.getSpec().getExistCert();
            if (existCert != null) {
                CertificationDTO certificationDTO = new CertificationDTO();
                certificationDTO.setCertificationFileId(baseStoreCertFile(
                        new CertificationFileDO(existCert.getCert(), existCert.getKey())));
                certificationDTO.setId(certId);
                baseUpdateCertFileId(certificationDTO);
            }
        }
    }

    @Override
    public C7nCertification getC7nCertification(String name, String type, List<String> domains,
                                                String keyContent, String certContent, String envCode) {
        C7nCertification c7nCertification = new C7nCertification();

        c7nCertification.setMetadata(new CertificationMetadata(name,
                envCode));
        CertificationSpec spec = new CertificationSpec(type);
        if (type.equals(CertificationType.REQUEST.getType())) {
            CertificationAcme acme = new CertificationAcme();
            acme.initConfig(new CertificationConfig(domains));
            spec.setAcme(acme);
        } else if (type.equals(CertificationType.UPLOAD.getType())) {
            CertificationExistCert existCert = new CertificationExistCert(keyContent, certContent);
            spec.setExistCert(existCert);
        }
        spec.setCommonName(domains.get(0));
        spec.setDnsNames(domains.size() > 1 ? domains.stream().skip(1).collect(Collectors.toList()) : null);
        c7nCertification.setSpec(spec);
        return c7nCertification;
    }

    private void operateEnvGitLabFile(String certName,
                                      DevopsEnvironmentE devopsEnvironmentE,
                                      C7nCertification c7nCertification) {
        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));
        gitlabGroupMemberService.checkEnvProject(devopsEnvironmentE, userAttrE);
        clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentE.getProjectE().getId(), devopsEnvironmentE.getCode(), devopsEnvironmentE.getEnvIdRsa());

        ResourceConvertToYamlHandler<C7nCertification> resourceConvertToYamlHandler = new ResourceConvertToYamlHandler<>();
        resourceConvertToYamlHandler.setType(c7nCertification);
        resourceConvertToYamlHandler.operationEnvGitlabFile(CERT_PREFIX + certName,
                TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "create",
                userAttrE.getGitlabUserId(), null, null, null, false, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long certId) {
        CertificationDTO certificationDTO = baseQueryById(certId);
        Long certEnvId = certificationDTO.getEnvId();
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(certEnvId);

        UserAttrE userAttrE = userAttrRepository.baseQueryById(TypeUtil.objToLong(GitUserNameUtil.getUserId()));

        //校验环境相关信息
        devopsEnvironmentService.checkEnv(devopsEnvironmentE, userAttrE);

        Integer gitLabEnvProjectId = TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId());
        String certificateType = ObjectType.CERTIFICATE.getType();
        String certName = certificationDTO.getName();
        DevopsEnvFileResourceE devopsEnvFileResourceE = devopsEnvFileResourceRepository
                .queryByEnvIdAndResource(certEnvId, certId, certificateType);

        if (devopsEnvFileResourceE == null) {
            baseDeleteById(certId);
            if (gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    CERT_PREFIX + certificationDTO.getName() + ".yaml")) {
                gitlabRepository.deleteFile(
                        TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()),
                        CERT_PREFIX + certificationDTO.getName() + ".yaml",
                        "DELETE FILE",
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
            }
            return;
        } else {
            if (!gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceE.getFilePath())) {
                baseDeleteById(certId);
                devopsEnvFileResourceRepository.deleteFileResource(devopsEnvFileResourceE.getId());
                return;
            }
        }
        certificationDTO.setCommandId(createCertCommandE(CommandType.DELETE.getType(), certId, null));
        baseUpdateCommandId(certificationDTO);
        certificationDTO.setStatus(CertificationStatus.DELETING.getStatus());
        baseUpdateStatus(certificationDTO);

        if (devopsEnvFileResourceE.getFilePath() != null
                && devopsEnvFileResourceRepository
                .queryByEnvIdAndPath(certEnvId, devopsEnvFileResourceE.getFilePath()).size() == 1) {
            if (gitlabRepository.getFile(TypeUtil.objToInteger(devopsEnvironmentE.getGitlabEnvProjectId()), "master",
                    devopsEnvFileResourceE.getFilePath())) {
                gitlabRepository.deleteFile(
                        gitLabEnvProjectId,
                        devopsEnvFileResourceE.getFilePath(),
                        "DELETE FILE " + certName,
                        TypeUtil.objToInteger(userAttrE.getGitlabUserId()));
            }
        } else {
            ResourceConvertToYamlHandler<C7nCertification> certificationOperation = new ResourceConvertToYamlHandler<>();
            C7nCertification c7nCertification = new C7nCertification();
            CertificationMetadata certificationMetadata = new CertificationMetadata();
            certificationMetadata.setName(certName);
            c7nCertification.setMetadata(certificationMetadata);
            certificationOperation.setType(c7nCertification);
            certificationOperation.operationEnvGitlabFile(
                    null, gitLabEnvProjectId,
                    "delete", userAttrE.getGitlabUserId(), certId, certificateType, null, false, certEnvId,
                    clusterConnectionHandler.handDevopsEnvGitRepository(devopsEnvironmentE.getProjectE().getId(), devopsEnvironmentE.getCode(), devopsEnvironmentE.getEnvIdRsa()));
        }
    }

    @Override
    public List<OrgCertificationDTO> baseListByProject(Long projectId) {
        ProjectVO projectE = iamRepository.queryIamProject(projectId);
        List<OrgCertificationDTO> orgCertificationDTOS = new ArrayList<>();
        baseListByProject(projectId, projectE.getOrganization().getId()).forEach(certificationDTO -> {
            OrgCertificationDTO orgCertificationDTO = new OrgCertificationDTO();
            orgCertificationDTO.setName(certificationDTO.getCertName());
            orgCertificationDTO.setId(certificationDTO.getId());
            orgCertificationDTO.setDomain(certificationDTO.getDomains().get(0));
            orgCertificationDTOS.add(orgCertificationDTO);
        });
        return orgCertificationDTOS;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void certDeleteByGitOps(Long certId) {
        CertificationDTO certificationDTO = baseQueryById(certId);

        //校验环境是否连接
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(certificationDTO.getEnvId());

        clusterConnectionHandler.checkEnvConnection(devopsEnvironmentE.getClusterE().getId());

        //实例相关对象数据库操作
        devopsEnvCommandRepository.baseListByObjectAll(HelmObjectKind.CERTIFICATE.toValue(), certificationDTO.getId()).forEach(t -> devopsEnvCommandRepository.baseDeleteCommandById(t));
        baseDeleteById(certId);
    }

    @Override
    public PageInfo<CertificationVO> basePage(Long projectId, Long envId, PageRequest pageRequest, String params) {
        if (params == null) {
            params = "{}";
        }

        PageInfo<CertificationVO> certificationDTOPage = basePage(projectId, null, envId, pageRequest, params);
        List<Long> connectedEnvList = clusterConnectionHandler.getConnectedEnvList();
        List<Long> updatedEnvList = clusterConnectionHandler.getUpdatedEnvList();
        certificationDTOPage.getList().stream()
                .filter(certificationDTO -> certificationDTO.getOrganizationId() == null)
                .forEach(certificationDTO -> {
                    DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(certificationDTO.getEnvId());
                    certificationDTO.setEnvConnected(
                            connectedEnvList.contains(devopsEnvironmentE.getClusterE().getId())
                                    && updatedEnvList.contains(devopsEnvironmentE.getClusterE().getId()));
                });
        return certificationDTOPage;
    }

    @Override
    public List<CertificationVO> getActiveByDomain(Long projectId, Long envId, String domain) {
        DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(envId);
        return baseQueryActiveByDomain(projectId, devopsEnvironmentE.getClusterE().getId(), domain);
    }

    @Override
    public Boolean checkCertNameUniqueInEnv(Long envId, String certName) {
        return baseCheckCertNameUniqueInEnv(envId, certName);
    }

    @Override
    public CertificationVO queryByName(Long envId, String certName) {
        return ConvertHelper.convert(baseQueryByEnvAndName(envId, certName), CertificationVO.class);
    }


    public CertificationDTO voToDTO(CertificationVO certificationVO) {
        CertificationDTO certificationDTO = new CertificationDTO();
        BeanUtils.copyProperties(certificationVO, certificationDTO);
        certificationDTO.setDomains(gson.toJson(certificationVO.getDomains()));
        return certificationDTO;
    }

    public CertificationVO dtoToVo(CertificationDTO certificationDTO) {
        CertificationVO certificationVO = new CertificationVO();
        BeanUtils.copyProperties(certificationDTO, certificationVO);
        certificationVO.setCertName(certificationDTO.getName());
        certificationVO.setDomains(gson.fromJson(certificationDTO.getDomains(), new TypeToken<List<String>>() {
        }.getType()));
        certificationVO.setCommonName(certificationVO.getDomains().get(0));
        if (certificationDTO.getEnvId() != null) {
            DevopsEnvironmentE devopsEnvironmentE = devopsEnvironmentRepository.queryById(certificationDTO.getEnvId());
            certificationVO.setEnvName(devopsEnvironmentE.getName());
        }
        return certificationVO;
    }


    @Override
    public Long createCertCommandE(String type, Long certId, Long userId) {
        DevopsEnvCommandVO devopsEnvCommandE = new DevopsEnvCommandVO();
        devopsEnvCommandE.setCommandType(type);
        devopsEnvCommandE.setCreatedBy(userId);
        devopsEnvCommandE.setObject(ObjectType.CERTIFICATE.getType());
        devopsEnvCommandE.setStatus(CommandStatus.OPERATING.getStatus());
        devopsEnvCommandE.setObjectId(certId);
        return devopsEnvCommandRepository.create(devopsEnvCommandE).getId();
    }


    @Override
    public CertificationVO baseQueryByEnvAndName(Long envId, String name) {
        CertificationDTO certificationDTO = new CertificationDTO();
        certificationDTO.setEnvId(envId);
        certificationDTO.setName(name);
        return dtoToVo(devopsCertificationMapper.selectOne(certificationDTO));
    }

    @Override
    public CertificationDTO baseCreate(CertificationDTO certificationDTO) {
        devopsCertificationMapper.insert(certificationDTO);
        return certificationDTO;
    }

    @Override
    public CertificationDTO baseQueryById(Long certId) {
        return devopsCertificationMapper.selectByPrimaryKey(certId);
    }

    @Override
    public PageInfo<CertificationVO> basePage(Long projectId, Long organizationId, Long envId, PageRequest pageRequest, String params) {
        Map<String, Object> maps = gson.fromJson(params, new TypeToken<Map<String, Object>>() {
        }.getType());

        Sort sort = pageRequest.getSort();
        String sortResult = "";
        if (sort != null) {
            sortResult = Lists.newArrayList(pageRequest.getSort().iterator()).stream()
                    .map(t -> {
                        String property = t.getProperty();
                        if (property.equals("envName")) {
                            property = "de.name";
                        } else if (property.equals("envCode")) {
                            property = "de.code";
                        } else if (property.equals("certName")) {
                            property = "dc.`name`";
                        } else if (property.equals("commonName")) {
                            property = "dc.domains";
                        }
                        return property + " " + t.getDirection();
                    })
                    .collect(Collectors.joining(","));
        }

        Map<String, Object> searchParamMap = TypeUtil.cast(maps.get(TypeUtil.SEARCH_PARAM));
        String param = TypeUtil.cast(maps.get(TypeUtil.PARAM));
        PageInfo<CertificationVO> certificationDTOPage = ConvertPageHelper.convertPageInfo(
                PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), sortResult).doSelectPageInfo(() -> devopsCertificationMapper
                        .selectCertification(projectId, organizationId, envId, searchParamMap, param)),
                CertificationVO.class);

        // check if cert is overdue
        certificationDTOPage.getList().forEach(dto -> {
            if (CertificationStatus.ACTIVE.getStatus().equals(dto.getStatus())) {
                CertificationE certificationE = ConvertHelper.convert(dto, CertificationE.class);
                if (!certificationE.checkValidity()) {
                    dto.setStatus(CertificationStatus.OVERDUE.getStatus());
                    CertificationDTO certificationDTO = new CertificationDTO();
                    certificationDTO.setId(dto.getId());
                    certificationDTO.setStatus(CertificationStatus.OVERDUE.getStatus());
                    devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
                }
            }
        });

        return certificationDTOPage;
    }

    @Override
    public List<CertificationVO> baseQueryActiveByDomain(Long projectId, Long clusterId, String domain) {
        return ConvertHelper.convertList(devopsCertificationMapper.getActiveByDomain(projectId, clusterId, domain),
                CertificationVO.class);
    }

    @Override
    public void baseUpdateStatus(CertificationDTO inputCertificationDTO) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(inputCertificationDTO.getId());
        certificationDTO.setStatus(inputCertificationDTO.getStatus());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
    }

    @Override
    public void baseUpdateCommandId(CertificationDTO certificationDTO) {
        CertificationDTO certificationDTOInDb = devopsCertificationMapper.selectByPrimaryKey(certificationDTO.getId());
        certificationDTO.setCommandId(certificationDTO.getCommandId());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
    }

    /**
     * check weather cert is active on date
     *
     * @param date       checkDate
     * @param validFrom  valid date from
     * @param validUntil valid date until
     * @return true if cert is active, else false
     */
    public Boolean checkValidity(Date date, Date validFrom, Date validUntil) {
        return validFrom != null && validUntil != null
                && date.after(validFrom) && date.before(validUntil);
    }

    @Override
    public void baseUpdateValidField(CertificationDTO inputCertificationDTO) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(inputCertificationDTO.getId());
        if (checkValidity(new Date(), inputCertificationDTO.getValidFrom(), inputCertificationDTO.getValidUntil())) {
            certificationDTO.setStatus(CertificationStatus.ACTIVE.getStatus());
        } else {
            certificationDTO.setStatus(CertificationStatus.OVERDUE.getStatus());
        }
        certificationDTO.setValid(inputCertificationDTO.getValidFrom(), inputCertificationDTO.getValidUntil());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
    }

    @Override
    public void baseUpdateCertFileId(CertificationDTO inputCertificationDTO) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(inputCertificationDTO.getId());
        certificationDTO.setCertificationFileId(inputCertificationDTO.getCertificationFileId());
        devopsCertificationMapper.updateByPrimaryKeySelective(certificationDTO);
    }

    @Override
    public void baseClearValidField(Long certId) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(certId);
        if (certificationDTO != null
                && (certificationDTO.getValidFrom() != null || certificationDTO.getValidUntil() != null)) {
            certificationDTO.setValid(null, null);
            devopsCertificationMapper.updateByPrimaryKey(certificationDTO);
        }
    }

    @Override
    public void baseDeleteById(Long id) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(id);
        if (certificationDTO.getOrgCertId() == null) {
            deleteCertFile(id);
        }
        devopsCertificationMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Boolean baseCheckCertNameUniqueInEnv(Long envId, String certName) {
        return devopsCertificationMapper.select(new CertificationDTO(certName, envId)).isEmpty();
    }

    @Override
    public Long baseStoreCertFile(CertificationFileDO certificationFileDO) {
        devopsCertificationFileMapper.insert(certificationFileDO);
        return certificationFileDO.getId();
    }

    @Override
    public CertificationFileDO baseQueryCertFile(Long certId) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(certId);
        return devopsCertificationFileMapper.selectByPrimaryKey(certificationDTO.getCertificationFileId());
    }

    @Override
    public List<CertificationDTO> baseListByEnvId(Long envId) {
        CertificationDTO certificationDTO = new CertificationDTO();
        certificationDTO.setEnvId(envId);
        return devopsCertificationMapper.select(certificationDTO);
    }

    @Override
    public void baseUpdateSkipProjectPermission(CertificationDTO certificationDTO) {
        devopsCertificationMapper.updateSkipCheckPro(certificationDTO.getId(), certificationDTO.getSkipCheckProjectPermission());
    }

    @Override
    public CertificationVO baseQueryByOrgAndName(Long orgId, String name) {
        CertificationDTO certificationDTO = new CertificationDTO();
        certificationDTO.setName(name);
        certificationDTO.setOrganizationId(orgId);
        return dtoToVo(devopsCertificationMapper.selectOne(certificationDTO));
    }

    @Override
    public List<CertificationDTO> baseListByOrgCertId(Long orgCertId) {
        CertificationDTO certificationDTO = new CertificationDTO();
        certificationDTO.setOrgCertId(orgCertId);
        return devopsCertificationMapper.select(certificationDTO);
    }

    @Override
    public List<CertificationVO> baseListByProject(Long projectId, Long organizationId) {
        return devopsCertificationMapper.listByProjectId(projectId, organizationId).stream().map(this::dtoToVo).collect(Collectors.toList());
    }

    private void deleteCertFile(Long certId) {
        CertificationDTO certificationDTO = devopsCertificationMapper.selectByPrimaryKey(certId);
        if (devopsCertificationFileMapper.selectByPrimaryKey(certificationDTO.getCertificationFileId()) != null) {
            devopsCertificationFileMapper.deleteByPrimaryKey(certificationDTO.getCertificationFileId());
        }
    }
}
