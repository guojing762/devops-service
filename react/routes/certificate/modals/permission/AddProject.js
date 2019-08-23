import React, { Fragment, useEffect } from 'react';
import { Table, Modal, SelectBox, Form, Select } from 'choerodon-ui/pro';
import { Button } from 'choerodon-ui';
import { injectIntl, FormattedMessage } from 'react-intl';
import { observer } from 'mobx-react-lite';
import map from 'lodash/map';

import './index.less';

export default injectIntl(observer(({ dataSet, optionsDs, intlPrefix, prefixCls, modal }) => {
  useEffect(() => {
    optionsDs.query();
    dataSet.getField('project').set('options', optionsDs);
    handleCreate();
  }, []);

  modal.handleOk(async () => {
    try {
      if (await dataSet.submit() !== false) {
        dataSet.query();
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  });

  function handleDelete(record) {
    dataSet.remove(record);
  }

  function handleCreate() {
    dataSet.create();
  }

  return (
    <div className={`${prefixCls}-project-add`}>
      {map(dataSet.created, (record) => (
        <div>
          <Form record={record}>
            <Select name="project" />
          </Form>
          <Button icon="delete" onClick={() => handleDelete(record)} />
        </div>
      ))}
      <Button
        type="primary"
        icon="add"
        onClick={handleCreate}
        className={`${prefixCls}-permission-wrap-button`}
      >
        <FormattedMessage id={`${intlPrefix}.project.add`} />
      </Button>
    </div>
  );
}));