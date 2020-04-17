import React, { useEffect, useState } from 'react';
import { Form, TextField, Select, TextArea, Button, Password } from 'choerodon-ui/pro';
import { Icon } from 'choerodon-ui';
import { observer } from 'mobx-react-lite';

const { Option } = Select;

const DependRepo = observer(({ ds, privateIf, modal, handleAdd, dsData, handleParentCancel }) => {
  const [hasData, setHasData] = useState(false);
  useEffect(() => {
    if (dsData) {
      dsData.forEach(d => {
        ds.create(d);
      });
      setHasData(true);
    }
  }, []);

  const handleCreateDependRepo = () => {
    ds.create({
      privateIf,
    });
  };

  const handleOk = async () => {
    const result = await ds.validate();
    if (result) {
      handleAdd(ds.toData(), privateIf);
      handleCancel(false);
      return true;
    }
    return false;
  };

  const handleCancel = (ParentCancel) => {
    ds.reset();
    if (ParentCancel) {
      handleParentCancel(privateIf);
    }
  };

  modal.handleOk(handleOk);
  modal.handleCancel(() => handleCancel(!hasData));

  const handleDeleteItem = async (r) => {
    await ds.remove(r);
  };

  return (
    <div className="dependRepo">
      {
        ds.records.map((r, rIndex) => (
          <div className="dependRepo_form_container">
            <Form columns={2} record={r}>
              <TextField name="name" />
              <Select name="type">
                <Option value="snapshot">snapshot仓库</Option>
                <Option value="release">relase仓库</Option>
              </Select>
              {
                privateIf ? [
                  <TextField name="username" />,
                  <Password name="password" />,
                ] : ''
              }
              <TextArea colSpan={2} name="url" />
              <div className="dependRepo_form_borderline" />
            </Form>
            <Icon
              onClick={() => handleDeleteItem(r)}
              style={{
                position: 'relative',
                left: '10px',
                bottom: '10px',
                fontSize: '18px',
                cursor: 'pointer',
              }}
              type="delete"
            />
          </div>
        ))
      }
      <Button funcType="flat" onClick={handleCreateDependRepo} className="depandRepo_addRepo">{`+添加${privateIf ? '私有' : '公有'}依赖仓库`}</Button>
    </div>
  );
});

export default DependRepo;
