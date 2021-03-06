import { axios } from '@choerodon/boot';
import React, { useEffect, useState } from 'react';
import { Form, TextField, Select, SelectBox, Modal, Button, DataSet } from 'choerodon-ui/pro';
import { message, Icon } from 'choerodon-ui';
import { observer } from 'mobx-react-lite';
import { usePipelineCreateStore } from './stores';
import AddTask from './components/AddTask';
import { usePipelineManageStore } from '../../stores';
import StageEditBlock from '../PipelineFlow/components/stageEditBlock';

import './pipelineCreate.less';

const { Option } = Select;

const PipelineCreate = observer(() => {
  const {
    PipelineCreateFormDataSet,
    modal,
    editBlockStore,
    createUseStore,
    AppState: {
      currentMenuType: {
        id,
        projectId,
      },
    },
    refreshTree,
    dataSource,
  } = usePipelineCreateStore();

  useEffect(() => {
    if (dataSource) {
      const { name, appServiceId, image, stageList } = dataSource;
      PipelineCreateFormDataSet.loadData([{
        name,
        appServiceId,
        image,
        selectImage: '1',
      }]);
      editBlockStore.setStepData(stageList, true);
    }
    const init = async () => {
      const res = await createUseStore.axiosGetDefaultImage();
      createUseStore.setDefaultImage(res);
      PipelineCreateFormDataSet.current.set('image', res);
    };
    init();
  }, []);

  const handleCreate = async () => {
    const result = await PipelineCreateFormDataSet.validate();
    if (result) {
      const origin = PipelineCreateFormDataSet.toData()[0];
      const data = {
        ...dataSource,
        ...origin,
        image: origin.selectImage === '1' ? origin.image : null,
        stageList: editBlockStore.getStepData2,
      };
      if (dataSource) {
        await axios.put(`/devops/v1/projects/${projectId}/ci_pipelines/${dataSource.id}`, data);
        editBlockStore.loadData(projectId, dataSource.id);
        refreshTree();
      } else {
        return createUseStore.axiosCreatePipeline(data, id).then((res) => {
          if (res.failed) {
            message.error(res.message);
            return false;
          } else {
            refreshTree();
            return true;
          }
        });
      }
    } else {
      return false;
    }
  };

  modal.handleOk(handleCreate);

  // const handleChangeImage = (data) => {
  //   if (data === '0') {
  //     PipelineCreateFormDataSet.current.set('image', createUseStore.getDefaultImage);
  //   } else {
  //     PipelineCreateFormDataSet.current.set('image', '');
  //   }
  // };

  const handleChangeSelectImage = (data) => {
    if (data === createUseStore.getDefaultImage) {
      PipelineCreateFormDataSet.current.set('selectImage', '0');
    } else {
      PipelineCreateFormDataSet.current.set('selectImage', '1');
    }
  };

  // const handleAddMission = () => {
  //   Modal.open({
  //     key: Modal.key(),
  //     title: '添加任务',
  //     style: {
  //       width: '740px',
  //     },
  //     children: <AddTask />,
  //     drawer: true,
  //     okText: '添加',
  //   });
  // };

  return (
    <div>
      <Form columns={3} dataSet={PipelineCreateFormDataSet}>
        <TextField name="name" />
        {/* 应用服务只能选择目前没有关联流水线的应用服务 */}
        <Select
          name="appServiceId"
          searchable
          searchMatcher="appServiceName"
        />
        <TextField style={{ display: 'none' }} />
        <Select
          // disabled={
          //   !!(PipelineCreateFormDataSet.current && PipelineCreateFormDataSet.current.get('selectImage') === '0')
          // }
          combo
          newLine
          colSpan={2}
          name="image"
          onChange={handleChangeSelectImage}
        >
          <Option value={createUseStore.getDefaultImage}>{createUseStore.getDefaultImage}</Option>
        </Select>
        {/* <SelectBox name="triggerType"> */}
        {/*  <Option value="auto">自动触发</Option> */}
        {/*  <Option disabled value="F">手动触发</Option> */}
        {/* </SelectBox> */}
      </Form>
      <StageEditBlock
        editBlockStore={editBlockStore}
        edit
        image={PipelineCreateFormDataSet.current.get('image')}
      />
      <p className="pipeline_createInfo"><Icon style={{ color: 'red', verticalAlign: 'text-bottom' }} type="error" />此页面定义了阶段与任务后，GitLab仓库中的.gitlab-ci.yml文件也会同步修改。</p>
    </div>
  );
});

export default PipelineCreate;
