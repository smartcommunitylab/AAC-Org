import * as React from "react";
import { Create, SimpleForm, SimpleFormIterator } from 'react-admin';
import { TextField, TextInput, ArrayInput, } from 'react-admin';
import { ReferenceInput, AutocompleteInput, } from 'react-admin';

import { required, regex } from 'react-admin';

const validateSlug = [required(), regex(/^[a-z0-9_]+$/, 'Must be a valid slug: a-z0-9_')];
const validateName = regex(/^[\w\-\s]+$/, 'Must be a valid alphanumeric string (with space)');






export const ComponentCreate = ({ permissions, ...props }) => (
    <Create {...props}>
        <SimpleForm redirect="list">
            <TextInput source="id" validate={validateSlug} />
            {/* <ReferenceInput label="id" reference="models">
                <AutocompleteInput optionText="id" />
            </ReferenceInput> */}
            <TextInput source="name" validate={validateName} />
            <ArrayInput source="roles">
                <SimpleFormIterator>
                    <TextInput />
                </SimpleFormIterator>
            </ArrayInput>
        </SimpleForm>
    </Create>
);

export default ComponentCreate;