import React, { useEffect, Fragment } from 'react';
import { observer } from 'mobx-react-lite';
import { TextField, Form, Button, Icon, Select, SelectBox, Tooltip } from 'choerodon-ui/pro';
import { map } from 'lodash';
import useNetWorkStore from '../stores';

import '../index.less';

const { Option } = Select;

function FormContent() {
  const {
    refresh,
    formDs,
    portDs,
    targetLabelsDs,
    modal,
    intl: {
      formatMessage,
    },
    networkId,
  } = useNetWorkStore();

  const current = formDs.current;

  modal.handleOk(async () => {
    if (await formDs.submit() !== false) {
      refresh();
    } else {
      return false;
    }
  });

  function createPortGroup() {
    portDs.create();
  }

  function removePortGroup(record) {
    portDs.remove(record);
    portDs.validate();
  }

  function createTargetLabelGroup() {
    targetLabelsDs.create();
  }

  function removeTargetLabelGroup(record) {
    targetLabelsDs.remove(record);
    targetLabelsDs.validate();
  }

  function targetPortOptionRenderer({ record, text, value }) {
    return value;
  }

  function targetPortOptionsFilter(record) {
    return !!record.get('portName');
  }


  function labelOptionRenderer({ record, text, value }) {
    return `${record.get('meaning')}`;
  }

  function appInstanceOptionRenderer({ record, text, value }) {
    // 如果数据带有status字段，就是来自网络信息详情的数据
    const status = record.get('status');
    if (status) {
      return <Fragment>
        <Tooltip
          title={formatMessage({ id: status })}
          placement="right"
        >
          {text}
        </Tooltip>
        { status !== 'running' && (
        <Tooltip title={formatMessage({ id: 'deleted' })} placement="top">
          <Icon type="error" className="c7ncd-instance-status-icon" />
        </Tooltip>
        )}
      </Fragment>;
    } else {
      return text;
    }
  }

  function clearInputOption(record) {
    const meaning = record.get('meaning');
    return meaning && meaning.indexOf(':') >= 0;
  }


  return (
    <Fragment>
      <div className="c7ncd-create-network">
        <Form dataSet={formDs} columns={3}>
          <TextField name="name" colSpan={3} maxLength={30} disabled={!!networkId} />
        </Form>

        <div className="hr" />
        <p className="network-panel-title">{formatMessage({ id: 'network.target' })}</p>

        <Form dataSet={formDs} columns={3}>
          <div
            className="network-panel-target-select"
            colSpan={3}
          >
            <SelectBox name="target">
              <Option value="instance"><span className="target-instance">{formatMessage({ id: 'network.target.instance' })}</span></Option>
              <Option value="param">{formatMessage({ id: 'network.target.param' })}</Option>
            </SelectBox>
          </div>
          <div colSpan={3} className="target-form">
            {
              (current && current.get('target') === 'instance')
                ? <Select name="appInstance" colSpan={3} className="app-instance-select" optionRenderer={appInstanceOptionRenderer} />
                : <div className="label-form">
                  {
                    map(targetLabelsDs.created, (record, index) => (<Form record={record} key={`target-label-record-${index}`} columns={4}>
                      <Select name="keyword" combo optionRenderer={labelOptionRenderer} optionsFilter={clearInputOption} />
                      <Icon className="network-group-icon" type="drag_handle" />
                      <Select name="value" combo optionRenderer={labelOptionRenderer} optionsFilter={clearInputOption} />
                      {
                        targetLabelsDs.created.length > 1 ? <Icon
                          colSpan={1}
                          className="delete-icon-target"
                          type="delete"
                          onClick={removeTargetLabelGroup.bind(this, record)}
                        /> : <span colSpan={1} />
                      }
                    </Form>))
                  }
                  <Button
                    color="primary"
                    funcType="flat"
                    onClick={createTargetLabelGroup}
                    icon="add"
                  >
                    {formatMessage({ id: 'network.config.addtarget' })}
                  </Button>
                </div>
            }
          </div>
        </Form>

        <div className="hr" />
        <p className="network-panel-title">{formatMessage({ id: 'network.config' })}</p>

        <Form dataSet={formDs} columns={3}>
          <div className="type-form" newLine colSpan={3}>
            <SelectBox name="type" record={current}>
              <Option value="ClusterIP"><span className="type-span">ClusterIP</span></Option>
              <Option value="NodePort"><span className="type-span">NodePort</span></Option>
              <Option value="LoadBalancer">LoadBalancer</Option>
            </SelectBox>
          </div>
          {current.get('type') === 'ClusterIP'
          && <TextField name="externalIps" record={current} colSpan={3} />}
        </Form>

        <div className="group-port">
          {
            map(portDs.created, (record, index) => (<Form record={record} key={`port-record-${index}`} columns={5}>
              
              {
                current.get('type') !== 'ClusterIP'
                  && <TextField name="nodePort" maxLength={5} />
              }
              <TextField name="port" maxLength={5} />
              <Select name="targetPort" combo optionRenderer={targetPortOptionRenderer} clearButton={false} optionsFilter={targetPortOptionsFilter} />
              {
                current.get('type') === 'NodePort'
                && <Select name="protocol" clearButton={false}>
                  {map(['TCP', 'UDP'], (item) => (
                    <Option value={item} key={item}>
                      {item}
                    </Option>
                  ))}
                </Select>
              }
              {
                portDs.created.length > 1 ? <Icon
                  colSpan={3}
                  className="delete-icon"
                  type="delete"
                  onClick={removePortGroup.bind(this, record)}
                /> : <span colSpan={3} />
              }
            </Form>))
          }
          <Button
            color="primary"
            funcType="flat"
            onClick={createPortGroup}
            icon="add"
          >
            {formatMessage({ id: 'network.config.addport' })}
          </Button>
        </div>
      </div>
    </Fragment>
  );
}

export default observer(FormContent);