import * as React from "react";
import { Show, SimpleShowLayout } from 'react-admin';
import { TextField, FunctionField, ReferenceField } from 'react-admin';

import Button from '@material-ui/core/Button';
import { EditButton, ListButton, DeleteButton, TopToolbar } from 'react-admin';

import RecordTitle from '../fields/RecordTitle'

const ShowActions = ({ basePath, data, resource }) => (
    <TopToolbar>
        <EditButton basePath={basePath} record={data} />
        <DeleteButton basePath={basePath}
            record={data}
            resource={resource}
            undoable={false}
        />
        <ListButton basePath={basePath} />
    </TopToolbar>
);

const ShowTitle = ({ record }) => (
    <span>Space {record ? `${record.organization}/${record.id}` : ''}</span>
);

const SpaceShow = ({ permissions, record, ...props }) => (
    <Show title={<RecordTitle resource="space" />} actions={<ShowActions />} {...props}>
        <SimpleShowLayout>
            <FunctionField addLabel={false} render={record => <h1> {record.id} </h1>} />
            <TextField source="id" />
            <TextField source="name" />
            <TextField source="organization" />
            <ReferenceField label="Owner" source="owner" reference="members" linkType="show">
                <TextField source="username" />
            </ReferenceField >
            <TextField source="path" />
        </SimpleShowLayout>
    </Show>
);

export default SpaceShow;