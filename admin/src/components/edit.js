import * as React from "react";
import { Edit, SimpleForm, SimpleFormIterator, TabbedForm, FormTab } from 'react-admin';
import { TextField, TextInput, ArrayInput, } from 'react-admin';
import { SaveButton, ShowButton, Toolbar } from 'react-admin';
import { TopToolbar } from 'react-admin';
import { ReferenceArrayInput, ReferenceInput, SelectArrayInput } from 'react-admin';
import { required, regex } from 'react-admin';
import { AutocompleteArrayInput, AutocompleteInput } from 'react-admin';

const validateName = regex(/^[\w\-\s]+$/, 'Must be a valid alphanumeric string (with space)');
const validateRole = regex(/^[\w]+$/, 'Must be a valid alphanumeric string (without space)');

const transformOnUpdate = (data) => {
    console.log("before submit transform")
    console.log(data)

    // //we need to inflate spaces
    // const spaces = Array.from(data.spaces, s => ({ id: s }))
    // data.spaces = spaces;
    // console.log(data)

    return data;
}

const EditActions = ({ basePath, data, resource }) => (
    <TopToolbar>
        <ShowButton basePath={basePath} record={data} resource={resource} />
    </TopToolbar>
);

const ComponentEdit = ({ permissions, ...props }) => (
    <Edit undoable={false} actions={<EditActions />} transform={transformOnUpdate}  {...props}>
        {/* <SimpleForm redirect="show">
            <TextField source="id" />
            <TextField source="organization" />
            <TextField source="path" />
            <TextInput source="name" validate={validateName} />
            <ArrayInput source="roles">
                <SimpleFormIterator>
                    <TextInput />
                </SimpleFormIterator>
            </ArrayInput>

            <ReferenceArrayInput source="spaces" reference="spaces">
                <AutocompleteArrayInput />
            </ReferenceArrayInput>
        </SimpleForm> */}

        <TabbedForm redirect="list">
            <FormTab label="summary">
                <TextField source="id" />
                <TextField source="organization" />
                <TextField source="path" />
                <TextInput source="name" validate={validateName} />
            </FormTab>
            <FormTab label="roles">
                <ArrayInput source="roles">
                    <SimpleFormIterator>
                        <TextInput validate={validateRole} />
                    </SimpleFormIterator>
                </ArrayInput>
            </FormTab>
            <FormTab label="spaces">
                {/* <ReferenceArrayInput source="spaces" reference="spaces" format={list => {
                    console.log("map edit roles ")
                    console.dir(list)
                    return Array.from(list, s => (s.id))
                }
                }>
                    <AutocompleteArrayInput label="spaces" />
                </ReferenceArrayInput> */}
                <ArrayInput source="spaces" >
                    <SimpleFormIterator>
                        <ReferenceInput source="id" reference="spaces">
                            <AutocompleteInput label="spaces" optionValue="id" optionText="id" />
                        </ReferenceInput>
                    </SimpleFormIterator>
                </ArrayInput>
            </FormTab>
        </TabbedForm>
    </Edit>
);

export default ComponentEdit;