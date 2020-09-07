import * as React from "react";
import { Show, SimpleShowLayout } from 'react-admin';
import { TextField, FunctionField, ReferenceField } from 'react-admin';

import Button from '@material-ui/core/Button';
import { EditButton, ListButton, DeleteButton, TopToolbar } from 'react-admin';

import SelectButton from "../fields/SelectButton";

const ShowActions = ({ basePath, data, resource }) => (
    <TopToolbar>
        <SelectButton basePath={basePath} record={data} resource={resource} />
        <EditButton basePath={basePath} record={data} />
        <DeleteButton basePath={basePath}
            record={data}
            resource={resource}
            undoable={false}
        />
        <ListButton basePath={basePath} />

    </TopToolbar>
);


const OrganizationShow = ({ permissions, record, ...props }) => (
    <Show actions={<ShowActions />} {...props}>
        <SimpleShowLayout>
            <FunctionField addLabel={false} render={record => <h1> {record.name} </h1>} />
            <TextField source="id" />
            <TextField source="name" />
            <TextField source="slug" />
            <ReferenceField label="Owner" source="owner" reference="members">
                <TextField source="username" />
            </ReferenceField >
            <TextField source="path" />
        </SimpleShowLayout>
    </Show>
);

export default OrganizationShow;