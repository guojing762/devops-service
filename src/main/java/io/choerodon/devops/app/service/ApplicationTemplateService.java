package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.devops.api.vo.ApplicationTemplateRespVO;
import io.choerodon.devops.api.vo.ApplicationTemplateUpdateDTO;
import io.choerodon.devops.api.vo.ApplicationTemplateVO;
import io.choerodon.devops.app.eventhandler.payload.GitlabProjectPayload;

/**
 * Created by younger on 2018/3/27.
 */
public interface ApplicationTemplateService {

    /**
     * 组织下创建应用模板
     *
     * @param applicationTemplateVO 模板信息
     * @param organizationId        组织Id
     * @return ApplicationTemplateDTO
     */
    ApplicationTemplateRespVO create(ApplicationTemplateVO applicationTemplateVO, Long organizationId);

    /**
     * 组织下更新应用模板
     *
     * @param applicationTemplateUpdateDTO 模板信息
     * @param organizationId               组织Id
     * @return ApplicationTemplateDTO
     */
    ApplicationTemplateRespVO update(ApplicationTemplateUpdateDTO applicationTemplateUpdateDTO, Long organizationId);

    /**
     * 组织下删除应用模板
     *
     * @param appTemplateId 模板id
     */
    void delete(Long appTemplateId);

    /**
     * 组织下查询单个应用模板
     *
     * @param appTemplateId 模板id
     * @return ApplicationTemplateDTO
     */
    ApplicationTemplateRespVO queryByTemplateId(Long appTemplateId);

    /**
     * 组织下分页查询应用模板
     *
     * @param pageRequest    分页参数
     * @param organizationId 组织Id
     * @param searchParam    模糊查询参数
     * @return Page
     */
    PageInfo<ApplicationTemplateRespVO> listByOptions(PageRequest pageRequest, Long organizationId, String searchParam);

    /**
     * 处理模板创建逻辑
     *
     * @param gitlabProjectEventDTO 模板信息
     */
    void operationApplicationTemplate(GitlabProjectPayload gitlabProjectEventDTO);

    /**
     * 组织下查询应用模板
     *
     * @param organizationId 组织Id
     * @return List
     */
    List<ApplicationTemplateRespVO> listAllByOrganizationId(Long organizationId);

    /**
     * 创建模板校验名称是否存在
     *
     * @param organizationId 组织id
     * @param name           模板name
     */
    void checkName(Long organizationId, String name);

    /**
     * 创建模板校验编码是否存在
     *
     * @param organizationId 组织id
     * @param code           模板code
     */
    void checkCode(Long organizationId, String code);

    /**
     * 判断模板是否存在
     *
     * @param uuid 模板uuid
     * @return boolean
     */
    Boolean applicationTemplateExist(String uuid);

    /**
     * 设置应用应用模板创建失败状态
     *
     * @param gitlabProjectEventDTO 应用信息
     * @param organizationId        可为空
     */
    void setAppTemplateErrStatus(String gitlabProjectEventDTO, Long organizationId);

//    void initMockService(SagaClient sagaClient);

    /**
     * 根据模板code查询模板
     *
     * @param organizationId 组织id
     * @param code           模板code
     */
    ApplicationTemplateRespVO queryByCode(Long organizationId, String code);
}
