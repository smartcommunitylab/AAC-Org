import * as React from "react";
import { Create, SimpleForm } from 'react-admin';

import { required, regex } from 'react-admin';

import { AutocompleteInput, ReferenceInput } from 'react-admin';
import { SingleFieldList, TextField, ArrayField } from 'react-admin';




export const MemberCreate = ({ permissions, ...props }) => (
    <Create {...props}>
        <SimpleForm redirect="list" >

            <ReferenceInput label="Username/FullName" source="id" reference="users">
                <AutocompleteInput shouldRenderSuggestions={(val) => { return val.trim().length > 2 }} optionText={(record) => `${record.username} - ${record.fullName}`} />
            </ReferenceInput>
        </SimpleForm>
    </Create>
);

export default MemberCreate;