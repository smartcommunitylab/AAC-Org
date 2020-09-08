import * as React from "react";
import { Edit, SimpleForm } from 'react-admin';
import { TextField, TextInput } from 'react-admin';
import { SaveButton, ShowButton, Toolbar } from 'react-admin';
import { TopToolbar } from 'react-admin';

import { required, regex } from 'react-admin';

const validateName = regex(/^[\w\-\s]+$/, 'Must be a valid alphanumeric string (with space)');


const EditToolbar = props => (
    <Toolbar {...props} >
        {/* <ShowButton label="cancel" />  */}
        <SaveButton />
    </Toolbar>
);

const OrganizationEdit = ({ permissions, ...props }) => (
    <Edit undoable={false}  {...props}>
        <SimpleForm >
            <TextField source="id" />
            <TextField source="slug" />
            <TextField source="path" />
            <TextInput source="name" validate={validateName} />
        </SimpleForm>
    </Edit>
);

export default OrganizationEdit;