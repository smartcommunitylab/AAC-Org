import * as React from "react";
import { Create, SimpleForm } from 'react-admin';
import { TextField, TextInput } from 'react-admin';

import { required, regex } from 'react-admin';

const validateSlug = [required(), regex(/^[a-z0-9_]+$/, 'Must be a valid slug: a-z0-9_')];
const validateName = regex(/^[\w\-\s]+$/, 'Must be a valid alphanumeric string (with space)');

export const OrganizationCreate = ({ permissions, ...props }) => (
    <Create {...props}>
        <SimpleForm redirect="list">
            <TextInput source="id" validate={validateSlug} />
            <TextInput source="name" validate={validateName} />
        </SimpleForm>
    </Create>
);

export default OrganizationCreate;