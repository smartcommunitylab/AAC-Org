import * as React from "react";
import { Edit, SimpleForm } from 'react-admin';
import { TextField, TextInput } from 'react-admin';
import { SaveButton, ShowButton, Toolbar } from 'react-admin';
import { TopToolbar } from 'react-admin';

import { required, regex } from 'react-admin';

const validateName = regex(/^[\w\-\s]+$/, 'Must be a valid alphanumeric string (with space)');

const EditActions = ({ basePath, data, resource }) => (
    <TopToolbar>
        <ShowButton basePath={basePath} record={data} resource={resource} />
    </TopToolbar>
);

const EditToolbar = props => (
    <Toolbar {...props} >
        {/* <ShowButton label="cancel" />  */}
        <SaveButton />
    </Toolbar>
);

const SpaceEdit = ({ permissions, ...props }) => (
    <Edit undoable={false} actions={<EditActions />} {...props}>
        <SimpleForm toolbar={<EditToolbar />}>
            <TextField source="id" />
            <TextField source="organization" />
            <TextField source="path" />
            <TextInput source="name" validate={validateName} />
        </SimpleForm>
    </Edit>
);

export default SpaceEdit;