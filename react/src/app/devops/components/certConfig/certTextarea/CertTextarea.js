/**
 * @author ale0720@163.com
 * @date 2019-05-30 15:37
 */
import React, { Fragment } from 'react';
import {
  Form,
  Input,
} from 'choerodon-ui';
import { FormattedMessage } from 'react-intl';

const { TextArea } = Input;
const { Item: FormItem } = Form;
const formItemLayout = {
  labelCol: {
    xs: { span: 24 },
    sm: { span: 100 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 26 },
  },
};

export function CertTextarea(propsForm, formatMessage) {

  return <Fragment>
    <FormItem
      className="c7n-select_480"
      {...formItemLayout}
      label={<FormattedMessage id="certificate.cert.content" />}
    >
      {propsForm.getFieldDecorator('certValue', {
        rules: [
          {
            required: true,
            message: formatMessage({ id: 'required' }),
          },
        ],
      })(
        <TextArea
          autosize={{ minRows: 2 }}
          label={<FormattedMessage id="certificate.cert.content" />}
        />,
      )}
    </FormItem>
    <FormItem
      className="c7n-select_480"
      {...formItemLayout}
      label={<FormattedMessage id="certificate.key.content" />}
    >
      {propsForm.getFieldDecorator('keyValue', {
        rules: [
          {
            required: true,
            message: formatMessage({ id: 'required' }),
          },
        ],
      })(
        <TextArea
          autosize={{ minRows: 2 }}
          label={<FormattedMessage id="certificate.key.content" />}
        />,
      )}
    </FormItem>
  </Fragment>;
}