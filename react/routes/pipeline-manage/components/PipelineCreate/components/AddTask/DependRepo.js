import React, { useEffect, useState } from 'react';
import { Form, TextField, Select, TextArea, Button, Password } from 'choerodon-ui/pro';
import { Icon } from 'choerodon-ui';
import { observer } from 'mobx-react-lite';

const { Option } = Select;

const DependRepo = observer(({ ds, modal, handleAdd, dsData, handleParentCancel }) => {
  const [hasData, setHasData] = useState(false);
  useEffect(() => {
    if (dsData) {
      Object.keys(dsData).forEach(k => {
        dsData[k].forEach(v => {
          ds.create(v);
        });
      });
      setHasData(true);
    }
  }, []);

  const handleCreateDependRepo = (privateIf) => {
    ds.create({
      privateIf,
    });
  };

  const handleOk = async () => {
    const result = await ds.validate();
    if (result) {
      handleAdd(ds.toData());
      handleCancel(false);
      return true;
    }
    return false;
  };

  const handleCancel = (ParentCancel) => {
    ds.reset();
    if (ParentCancel) {
      handleParentCancel(ds.toData());
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
        ds.records.filter(d => !d.data.privateIf).map((r, rIndex) => (
          <div className="dependRepo_form_container">
            <Form columns={2} record={r}>
              <TextField name="name" />
              <Select name="type">
                <Option value="snapshot">snapshot仓库</Option>
                <Option value="release">relase仓库</Option>
              </Select>
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
      <div>
        <Button funcType="flat" onClick={() => handleCreateDependRepo(false)} className="depandRepo_addRepo">+添加公有依赖仓库</Button>
      </div>
      <div className="dependRepo_form_borderline" style={{ marginTop: 10 }} />
      {
        ds.records.filter(d => d.data.privateIf).map((r, rIndex) => (
          <div className="dependRepo_form_container" style={{ marginTop: 28 }}>
            <Form columns={2} record={r}>
              <TextField name="name" />
              <Select name="type">
                <Option value="snapshot">snapshot仓库</Option>
                <Option value="release">relase仓库</Option>
              </Select>
              <TextField name="username" />
              <Password name="password" />
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
      <div style={{ marginTop: 20 }}>
        <Button funcType="flat" onClick={() => handleCreateDependRepo(true)} className="depandRepo_addRepo">+添加私有依赖仓库</Button>
      </div>
    </div>
  );
});

export default DependRepo;
