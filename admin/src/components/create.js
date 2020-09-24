import * as React from "react";
import { Create, SimpleForm, SimpleFormIterator } from 'react-admin';
import { TextField, TextInput, ArrayInput, } from 'react-admin';
import { ReferenceInput, AutocompleteInput, SelectInput } from 'react-admin';
import { FormDataConsumer } from 'react-admin';
import { required, regex } from 'react-admin';
import Typography from '@material-ui/core/Typography';

const validateSlug = [required(), regex(/^[a-z0-9_]+$/, 'Must be a valid slug: a-z0-9_')];
const validateName = regex(/^[\w\-\s]+$/, 'Must be a valid alphanumeric string (with space)');



const transformOnUpdate = (data) => {
    // console.log("before submit transform")
    // console.log(data)

    //we need to fetch customid if set
    if (data.id == null || data.id == '') {
        data.id = data.customId;
    }


    // console.log(data)
    return data;
}


export const ComponentCreate = ({ permissions, ...props }) => (
    <Create transform={transformOnUpdate} {...props}>
        <SimpleForm redirect="list">
            <Typography variant="body1" fullWidth gutterBottom>
                <p>Create a component either by selecting a model or by defining a custom component.</p>
            </Typography>
            <ReferenceInput label="component" reference="models" source="id" allowEmpty>
                <SelectInput optionText="id" />
            </ReferenceInput>

            <FormDataConsumer>
                {({ formData, ...rest }) => formData.id == null &&
                    <TextInput label="Custom component" source="customId" validate={validateSlug} />
                }
            </FormDataConsumer>

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