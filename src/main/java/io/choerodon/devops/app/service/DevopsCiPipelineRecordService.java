package io.choerodon.devops.app.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.Pageable;

import io.choerodon.devops.api.vo.DevopsCiPipelineRecordVO;
import io.choerodon.devops.api.vo.PipelineWebHookVO;
import io.choerodon.devops.infra.dto.DevopsCiPipelineRecordDTO;
import io.choerodon.devops.infra.dto.gitlab.ci.Pipeline;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @Date 2020/4/3 9:26
 */
public interface DevopsCiPipelineRecordService {
    void create(PipelineWebHookVO pipelineWebHookVO, String token);

    void handleCreate(PipelineWebHookVO pipelineWebHookVO);

    /**
     * 分页查询流水线记录
     * @param projectId
     * @param ciPipelineId
     * @param pageable
     * @return
     */
    PageInfo<DevopsCiPipelineRecordVO> pagingPipelineRecord(Long projectId, Long ciPipelineId, Pageable pageable);

    DevopsCiPipelineRecordVO queryPipelineRecordDetails(Long projectId, Long gitlabPipelineId);

    /**
     * 删除流水线的执行记录
     * @param ciPipelineId
     */
    void deleteByPipelineId(Long ciPipelineId);

    /**
     * 查询流水线执行记录
     * @param ciPipelineId
     * @return
     */
    List<DevopsCiPipelineRecordDTO> queryByPipelineId(Long ciPipelineId);

    /**
     * 根据gitlabProjectId删除pipeline record
     * @param gitlabProjectId
     */
    void deleteByGitlabProjectId(Long gitlabProjectId);

    DevopsCiPipelineRecordDTO create(Long ciPipelineId, Long gitlabProjectId, Pipeline pipeline);

    /**
     * 重试流水线
     * @param projectId
     * @param gitlabPipelineId
     * @param gitlabProjectId
     */
    void retry(Long projectId, Long gitlabPipelineId, Long gitlabProjectId);

    /**
     * 取消执行流水线
     * @param projectId
     * @param gitlabPipelineId
     * @param gitlabProjectId
     */
    void cancel(Long projectId, Long gitlabPipelineId, Long gitlabProjectId);
}
