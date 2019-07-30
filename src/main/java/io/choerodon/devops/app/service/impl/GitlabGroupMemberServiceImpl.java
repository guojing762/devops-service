package io.choerodon.devops.app.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import io.choerodon.core.exception.CommonException;
import io.choerodon.devops.api.vo.GitlabGroupMemberVO;
import io.choerodon.devops.api.vo.kubernetes.MemberHelper;
import io.choerodon.devops.app.service.*;
import io.choerodon.devops.infra.dto.ApplicationServiceDTO;
import io.choerodon.devops.infra.dto.DevopsEnvironmentDTO;
import io.choerodon.devops.infra.dto.DevopsProjectDTO;
import io.choerodon.devops.infra.dto.UserAttrDTO;
import io.choerodon.devops.infra.dto.gitlab.GitLabUserDTO;
import io.choerodon.devops.infra.dto.gitlab.GitlabProjectDTO;
import io.choerodon.devops.infra.dto.gitlab.GroupDTO;
import io.choerodon.devops.infra.dto.gitlab.MemberDTO;
import io.choerodon.devops.infra.dto.iam.OrganizationDTO;
import io.choerodon.devops.infra.enums.AccessLevel;
import io.choerodon.devops.infra.feign.operator.GitlabServiceClientOperator;
import io.choerodon.devops.infra.util.TypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Created Zenger qs on 2018/3/28.
 */
@Service
public class GitlabGroupMemberServiceImpl implements GitlabGroupMemberService {
    public static final String ERROR_GITLAB_GROUP_ID_SELECT = "error.gitlab.groupId.select";
    private static final String PROJECT = "project";
    private static final String TEMPLATE = "template";
    private static final String SITE = "site";

    private static final Logger LOGGER = LoggerFactory.getLogger(GitlabGroupMemberServiceImpl.class);

    @Autowired
    private DevopsProjectService devopsProjectService;
    @Autowired
    private UserAttrService userAttrService;
    @Autowired
    private IamService iamService;
    @Autowired
    private ApplicationSevriceService applicationService;
    @Autowired
    private ApplicationUserPermissionService applicationUserPermissionService;
    @Autowired
    private GitlabServiceClientOperator gitlabServiceClientOperator;


    @Override
    public void createGitlabGroupMemberRole(List<GitlabGroupMemberVO> gitlabGroupMemberVOList) {
        gitlabGroupMemberVOList.stream()
                .filter(gitlabGroupMemberVO -> !gitlabGroupMemberVO.getResourceType().equals(SITE))
                .forEach(gitlabGroupMemberVO -> {
                    try {
                        List<String> userMemberRoleList = gitlabGroupMemberVO.getRoleLabels();
                        if (userMemberRoleList == null) {
                            userMemberRoleList = new ArrayList<>();
                            LOGGER.info("user member role is empty");
                        }
                        MemberHelper memberHelper = getGitlabGroupMemberRole(userMemberRoleList);
                        operation(gitlabGroupMemberVO.getResourceId(),
                                gitlabGroupMemberVO.getResourceType(),
                                memberHelper,
                                gitlabGroupMemberVO.getUserId());
                    } catch (Exception e) {
                        if (e.getMessage().equals(ERROR_GITLAB_GROUP_ID_SELECT)) {
                            LOGGER.info(ERROR_GITLAB_GROUP_ID_SELECT);
                            return;
                        }
                        throw new CommonException(e);
                    }
                });
    }

    @Override
    public void deleteGitlabGroupMemberRole(List<GitlabGroupMemberVO> gitlabGroupMemberVOList) {
        gitlabGroupMemberVOList.stream()
                .filter(gitlabGroupMemberVO -> !gitlabGroupMemberVO.getResourceType().equals(SITE))
                .forEach(gitlabGroupMemberVO -> {
                    UserAttrDTO userAttrDTO = userAttrService.baseQueryById(gitlabGroupMemberVO.getUserId());
                    Integer gitlabUserId = TypeUtil.objToInteger(userAttrDTO.getGitlabUserId());
                    GitLabUserDTO gitlabUserDTO = gitlabServiceClientOperator.queryUserById(
                            TypeUtil.objToInteger(gitlabUserId));
                    if (gitlabUserDTO == null) {
                        LOGGER.error("error.gitlab.username.select");
                        return;
                    }
                    DevopsProjectDTO devopsProjectDTO;
                    MemberDTO memberDTO;
                    if (PROJECT.equals(gitlabGroupMemberVO.getResourceType())) {
                        devopsProjectDTO = devopsProjectService.baseQueryByProjectId(gitlabGroupMemberVO.getResourceId());
                        memberDTO = gitlabServiceClientOperator.queryGroupMember(
                                TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()), gitlabUserId);
                        if (memberDTO != null && memberDTO.getUserId() != null) {
                            deleteGilabRole(memberDTO, devopsProjectDTO, gitlabUserId, false);
                        }
                        memberDTO = gitlabServiceClientOperator.queryGroupMember(
                                TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()), gitlabUserId);
                        if (memberDTO != null && memberDTO.getUserId() != null) {
                            deleteGilabRole(memberDTO, devopsProjectDTO, gitlabUserId, true);
                        }
                        // 删除用户时同时清除gitlab的权限
                        List<Integer> gitlabProjectIds = applicationService
                                .baseListByProjectId(gitlabGroupMemberVO.getResourceId()).stream()
                                .filter(e -> e.getGitlabProjectId() != null)
                                .map(ApplicationServiceDTO::getGitlabProjectId).map(TypeUtil::objToInteger)
                                .collect(Collectors.toList());
                        // gitlab

                        gitlabProjectIds.forEach(e -> {
                            MemberDTO projectMember = gitlabServiceClientOperator.getProjectMember(e, gitlabUserId);
                            if (projectMember != null && projectMember.getUserId() != null) {
                                gitlabServiceClientOperator.deleteProjectMember(e, gitlabUserId);
                            }
                        });
                        // devops
                        applicationUserPermissionService.baseDeleteByUserIdAndAppIds(
                                applicationService.baseListByProjectId(gitlabGroupMemberVO.getResourceId()).stream()
                                        .filter(applicationE -> applicationE.getGitlabProjectId() != null)
                                        .map(ApplicationServiceDTO::getId).collect(Collectors.toList()),
                                userAttrDTO.getIamUserId());
                    } else {
                        OrganizationDTO organizationDTO =
                                iamService.queryOrganizationById(gitlabGroupMemberVO.getResourceId());
                        GroupDTO groupDTO = gitlabServiceClientOperator.queryGroupByName(
                                organizationDTO.getCode() + "_" + TEMPLATE,
                                TypeUtil.objToInteger(gitlabUserId));
                        if (groupDTO == null) {
                            LOGGER.error(ERROR_GITLAB_GROUP_ID_SELECT);
                            return;
                        }
                        memberDTO = gitlabServiceClientOperator.queryGroupMember(
                                TypeUtil.objToInteger(groupDTO.getId()), gitlabUserId);
                        gitlabServiceClientOperator.deleteGroupMember(
                                groupDTO.getId(), gitlabUserId);
                    }
                });
    }

    @Override
    public void checkEnvProject(DevopsEnvironmentDTO devopsEnvironmentDTO, UserAttrDTO userAttrDTO) {
        DevopsProjectDTO devopsProjectDTO = devopsProjectService
                .baseQueryByProjectId(devopsEnvironmentDTO.getProjectId());
        if (devopsEnvironmentDTO.getGitlabEnvProjectId() == null) {
            throw new CommonException("error.env.project.not.exist");
        }
        MemberDTO memberDTO = gitlabServiceClientOperator
                .queryGroupMember(TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()),
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (memberDTO != null && memberDTO.getAccessLevel().equals(AccessLevel.OWNER.toValue())) {
            return;
        }
        MemberDTO newGroupMemberDTO = gitlabServiceClientOperator.getProjectMember(
                TypeUtil.objToInteger(devopsEnvironmentDTO.getGitlabEnvProjectId()),
                TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
        if (newGroupMemberDTO == null || (newGroupMemberDTO.getAccessLevel().equals(AccessLevel.MASTER.toValue()))) {
            throw new CommonException("error.user.not.env.pro.owner");
        }
    }

    @Override
    public MemberDTO queryByUserId(Integer groupId, Integer userId) {
        return gitlabServiceClientOperator.queryGroupMember(groupId, userId);
    }

    @Override
    public void delete(Integer groupId, Integer userId) {
        gitlabServiceClientOperator.deleteGroupMember(groupId, userId);
    }

    @Override
    public int create(Integer groupId, MemberDTO memberDTO) {
        return gitlabServiceClientOperator.createGroupMember(groupId, memberDTO);
    }

    @Override
    public void update(Integer groupId, MemberDTO memberDTO) {
        gitlabServiceClientOperator.updateGroupMember(groupId, memberDTO);
    }

    /**
     * get AccessLevel
     *
     * @param userMemberRoleList userMemberRoleList
     */
    private MemberHelper getGitlabGroupMemberRole(List<String> userMemberRoleList) {
        MemberHelper memberHelper = new MemberHelper();
        List<Integer> accessLevelList = new ArrayList<>();
        accessLevelList.add(0);
        userMemberRoleList.forEach(level -> {
            AccessLevel levels = AccessLevel.forString(level.toUpperCase(), memberHelper);
            switch (levels) {
                case OWNER:
                    accessLevelList.add(levels.toValue());
                    break;
                case MASTER:
                    accessLevelList.add(levels.toValue());
                    break;
                case DEVELOPER:
                    accessLevelList.add(levels.toValue());
                    break;
                default:
                    break;
            }
        });
        return memberHelper;
    }

    /**
     * The user action
     *
     * @param resourceId   资源Id
     * @param resourceType 资源type
     * @param memberHelper memberHelper
     * @param userId       userId
     */
    private void operation(Long resourceId, String resourceType, MemberHelper memberHelper, Long userId) {
        UserAttrDTO userAttrDTO = userAttrService.baseQueryById(userId);
        if (userAttrDTO == null) {
            throw new CommonException("The user you want to assign a role to is not created successfully!");
        }
        Integer gitlabUserId = TypeUtil.objToInteger(userAttrDTO.getGitlabUserId());
        DevopsProjectDTO devopsProjectDTO;
        MemberDTO memberDTO;
        Integer[] roles = {
                memberHelper.getProjectDevelopAccessLevel().toValue(),
                memberHelper.getProjectOwnerAccessLevel().toValue(),
                memberHelper.getOrganizationAccessLevel().toValue()};
        AccessLevel accessLevel = AccessLevel.forValue(Collections.max(Arrays.asList(roles)));
        // 如果当前iam用户只有项目成员的权限
        if (AccessLevel.DEVELOPER.equals(accessLevel)) {
            // 查看是不是由项目所有者改为项目成员
            devopsProjectDTO = devopsProjectService.baseQueryByProjectId(resourceId);
            memberDTO = gitlabServiceClientOperator.queryGroupMember(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()),
                    (TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
            if (memberDTO != null && AccessLevel.OWNER.toValue().equals(memberDTO.getAccessLevel())) {
                deleteGilabRole(memberDTO, devopsProjectDTO, gitlabUserId, false);
            }
            memberDTO = gitlabServiceClientOperator.queryGroupMember(
                    TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()),
                    (TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
            if (memberDTO != null && AccessLevel.OWNER.toValue().equals(memberDTO.getAccessLevel())) {
                deleteGilabRole(memberDTO, devopsProjectDTO, gitlabUserId, true);
            }
            // 为当前项目下所有跳过权限检查的应用加上gitlab用户权限
            List<Integer> gitlabProjectIds = applicationService.baseListByProjectIdAndSkipCheck(resourceId).stream()
                    .filter(e -> e.getGitlabProjectId() != null)
                    .map(ApplicationServiceDTO::getGitlabProjectId).collect(Collectors.toList());
            gitlabProjectIds.forEach(e -> {
                GitlabProjectDTO gitlabProjectDO = new GitlabProjectDTO();
                try {
                    gitlabProjectDO = gitlabServiceClientOperator.queryProjectById(e);
                } catch (CommonException exception) {
                    LOGGER.info("project not found");
                }
                if (gitlabProjectDO.getId() != null) {
                    MemberDTO groupMember = gitlabServiceClientOperator.queryGroupMember(e, gitlabUserId);
                    if (groupMember == null || groupMember.getUserId() == null) {
                        gitlabServiceClientOperator.createGroupMember(e, new MemberDTO(gitlabUserId, 30, ""));
                    }
                }
            });
        } else if (AccessLevel.OWNER.equals(accessLevel)) {
            if (resourceType.equals(PROJECT)) {
                try {
                    // 删除用户时同时清除gitlab的权限
                    List<Integer> gitlabProjectIds = applicationService
                            .baseListByProjectId(resourceId).stream().filter(e -> e.getGitlabProjectId() != null)
                            .map(ApplicationServiceDTO::getGitlabProjectId).map(TypeUtil::objToInteger)
                            .collect(Collectors.toList());
                    gitlabProjectIds.forEach(e -> {
                        MemberDTO projectMember = gitlabServiceClientOperator.getProjectMember(e, gitlabUserId);
                        if (projectMember != null && projectMember.getUserId() != null) {
                            gitlabServiceClientOperator.deleteProjectMember(e, gitlabUserId);
                        }
                    });
                    // 给gitlab应用组分配owner角色
                    devopsProjectDTO = devopsProjectService.baseQueryByProjectId(resourceId);
                    memberDTO = gitlabServiceClientOperator.queryGroupMember(
                            TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()),
                            (TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
                    addOrUpdateGilabRole(accessLevel, memberDTO,
                            TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()), userAttrDTO);

                    //给gitlab环境组分配owner角色
                    memberDTO = gitlabServiceClientOperator.queryGroupMember(
                            TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()),
                            (TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
                    addOrUpdateGilabRole(accessLevel, memberDTO,
                            TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId()), userAttrDTO);

                } catch (Exception e) {
                    LOGGER.info(ERROR_GITLAB_GROUP_ID_SELECT);
                }
            } else {
                //给组织对应的模板库分配owner角色
                OrganizationDTO organizationDTO = iamService.queryOrganizationById(resourceId);
                GroupDTO groupDTO = gitlabServiceClientOperator.queryGroupByName(
                        organizationDTO.getCode() + "_" + TEMPLATE,
                        TypeUtil.objToInteger(userAttrDTO.getGitlabUserId()));
                if (groupDTO == null) {
                    LOGGER.info(ERROR_GITLAB_GROUP_ID_SELECT);
                    return;
                }
                memberDTO = gitlabServiceClientOperator.queryGroupMember(
                        TypeUtil.objToInteger(groupDTO.getId()),
                        (TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
                addOrUpdateGilabRole(accessLevel, memberDTO,
                        TypeUtil.objToInteger(groupDTO.getId()), userAttrDTO);
            }
        }
    }

    private void addOrUpdateGilabRole(AccessLevel level, MemberDTO memberDTO, Integer groupId,
                                      UserAttrDTO userAttrDTO) {
        // 增删改用户
        switch (level) {
            case NONE:
                if (memberDTO != null) {
                    gitlabServiceClientOperator
                            .deleteGroupMember(groupId, (TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
                }
                break;
            case DEVELOPER:
            case MASTER:
            case OWNER:
                MemberDTO requestMember = new MemberDTO();
                requestMember.setUserId((TypeUtil.objToInteger(userAttrDTO.getGitlabUserId())));
                requestMember.setAccessLevel(level.toValue());
                requestMember.setExpiresAt("");
                if (memberDTO == null) {
                    gitlabServiceClientOperator.createGroupMember(groupId, requestMember);
                } else {
                    if (!Objects.equals(requestMember.getAccessLevel(), memberDTO.getAccessLevel())) {
                        gitlabServiceClientOperator.updateGroupMember(groupId, requestMember);
                    }
                }
                break;
            default:
                LOGGER.error("error.gitlab.member.level");
                break;
        }
    }

    private void deleteGilabRole(MemberDTO memberDTO, DevopsProjectDTO devopsProjectDTO,
                                 Integer userId, Boolean isEnvDelete) {
        if (memberDTO != null) {
            gitlabServiceClientOperator.deleteGroupMember(
                    isEnvDelete ? TypeUtil.objToInteger(devopsProjectDTO.getDevopsEnvGroupId())
                            : TypeUtil.objToInteger(devopsProjectDTO.getDevopsAppGroupId()), userId);
        }
    }
}
