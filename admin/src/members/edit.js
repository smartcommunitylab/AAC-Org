import * as React from "react";
import { Edit, SimpleForm } from 'react-admin';
import { ArrayInput, SimpleFormIterator, FormDataConsumer } from 'react-admin'
import { TextField } from 'react-admin';
import { SaveButton, ShowButton, Toolbar } from 'react-admin';
import { TopToolbar } from 'react-admin';
import { TextInput, SelectInput, ReferenceInput } from 'react-admin';

import RoleInput from '../fields/RoleInput'

import { useQuery, useGetOne, Loading, Error } from 'react-admin';

const useComponentSpaces = (componentId) => {

    const { data, loading, error } = useGetOne('component', componentId);
    if (loading) return [];
    if (error) return null;
    if (!data) return null;

    return data.spaces.map(s => ({ id: s, name: s }));
}

const systemRoles = ['ROLE_OWNER', 'ROLE_MEMBER', 'ROLE_PROVIDER']
const reservedRoles = ['ROLE_USER']

const transformOnUpdate = (data) => {
    // console.log("before submit transform")
    // console.log(data)

    //we need to cleanup referenced roles
    data.roles.map(r => {
        if (r.type == 'component' && r.component && r.space) {
            r.role = r.id
            if (r.role.indexOf("/") > -1) {
                r.role = r.role.split("/").pop()
            }
            if (r.role.indexOf(":") > -1) {
                r.role = r.role.split(":").pop()
            }
        }
        return r;
    })

    // console.log(data)
    return data;
}

const MemberEdit = ({ permissions, ...props }) => (
    <Edit undoable={false} transform={transformOnUpdate} {...props}>
        <SimpleForm redirect="show">
            <TextField source="id" />
            <TextField source="organization" />
            <TextField source="username" />
            <TextField source="fullName" />
            <ArrayInput source="roles" >
                <SimpleFormIterator>


                    <FormDataConsumer>
                        {({
                            formData, // The whole form data
                            scopedFormData = { type: '', role: '', component: '', space: '' }, // The data for this item of the ArrayInput
                            getSource, // A function to get the valid source inside an ArrayInput
                            formDataProps,
                            ...rest
                        }) => {

                            //system roles are not editable!
                            if ((systemRoles.includes(scopedFormData.role) || (reservedRoles.includes(scopedFormData.role)))
                                && (scopedFormData.type == 'organization' || scopedFormData.type == 'space')) {
                                return (
                                    <React.Fragment>
                                        <TextInput disabled={true} label="type" source={getSource('type')} />
                                        {scopedFormData.component && <TextInput disabled={true} label="component" source={getSource('component')} />}
                                        {scopedFormData.space && <TextInput disabled={true} label="space" source={getSource('space')} />}
                                        <TextInput disabled={true} label="role" source={getSource('role')} />

                                    </React.Fragment>
                                )
                            }

                            //system roles are not editable!
                            if (systemRoles.includes(scopedFormData.role)
                                && scopedFormData.type == 'component') {
                                return (
                                    <React.Fragment>
                                        <TextInput disabled={true} label="type" source={getSource('type')} />
                                        {scopedFormData.component && <TextInput disabled={true} label="component" source={getSource('component')} />}
                                        {scopedFormData.space && <TextInput disabled={true} label="space" source={getSource('space')} />}
                                        <TextInput disabled={true} label="role" source={getSource('role')} />

                                    </React.Fragment>
                                )
                            }

                            //component roles definition are not editable - hide
                            if (scopedFormData.type == 'component'
                                && scopedFormData.component && scopedFormData.role && !scopedFormData.space) {
                                return (
                                    <React.Fragment>
                                        <TextInput disabled={true} label="type" source={getSource('type')} />
                                        <TextInput disabled={true} label="component" source={getSource('component')} />
                                        <TextInput disabled={true} label="role" source={getSource('role')} />
                                    </React.Fragment>
                                )
                            }




                            // //org roles
                            // if (scopedFormData.type === 'organization') {
                            //     return (
                            //         <React.Fragment>
                            //             <SelectInput source={getSource('type')} choices={[
                            //                 { id: 'organization', name: 'organization' },
                            //                 { id: 'space', name: 'space' },
                            //                 { id: 'component', name: 'component' },
                            //             ]} />

                            //             <TextInput source={getSource('role')} />
                            //         </React.Fragment>
                            //     )
                            // }

                            // //component roles
                            // if (scopedFormData.type === 'component') {
                            //     return (
                            //         <React.Fragment>
                            //             <SelectInput source={getSource('type')} choices={[
                            //                 { id: 'organization', name: 'organization' },
                            //                 { id: 'space', name: 'space' },
                            //                 { id: 'component', name: 'component' },
                            //             ]} />

                            //             <ReferenceInput label="component" source={getSource('component')} reference="components">
                            //                 <SelectInput optionText="id" />
                            //             </ReferenceInput>
                            //             <TextInput source={getSource('role')} />
                            //         </React.Fragment>

                            //     )
                            // }

                            // //component roles
                            // if (scopedFormData.type === 'component') {
                            //     return (
                            //         <React.Fragment>
                            //             <SelectInput source={getSource('type')} choices={[
                            //                 { id: 'organization', name: 'organization' },
                            //                 { id: 'space', name: 'space' },
                            //                 { id: 'component', name: 'component' },
                            //             ]} />

                            //             <ReferenceInput label="component" source={getSource('component')} reference="components">
                            //                 <SelectInput optionText="id" />
                            //             </ReferenceInput>
                            //             <TextInput source={getSource('role')} />
                            //         </React.Fragment>

                            //     )
                            // }


                            return (
                                <React.Fragment>
                                    <SelectInput label="type" source={getSource('type')} choices={[
                                        { id: 'organization', name: 'organization' },
                                        { id: 'space', name: 'space' },
                                        { id: 'component', name: 'component' },
                                    ]} />






                                    {scopedFormData.type === 'space' &&
                                        <ReferenceInput label="space" source={getSource('space')} reference="spaces">
                                            <SelectInput optionText="id" />
                                        </ReferenceInput>
                                    }

                                    {(scopedFormData.type === 'organization' || scopedFormData.type === 'space') &&
                                        <TextInput label="role" source={getSource('role')} />
                                    }




                                    {scopedFormData.type === 'component' &&
                                        <ReferenceInput label="component" source={getSource('component')} reference="components">
                                            <SelectInput optionText="id" />
                                        </ReferenceInput>
                                    }


                                    {scopedFormData.type === 'component' && scopedFormData.component &&
                                        // <SelectInput source={getSource('space')} choices={spaceChoices} />
                                        <ReferenceInput label="space" source={getSource('space')} reference="spaces" filter={{ component: scopedFormData.component }}>
                                            <SelectInput optionText="id" />
                                        </ReferenceInput>
                                    }

                                    {scopedFormData.type === 'component' && scopedFormData.component && scopedFormData.space &&
                                        <ReferenceInput label="role" source={getSource('id')} reference="roles" filter={{ component: scopedFormData.component }}>
                                            <SelectInput optionText={(r) => (r.role)} />
                                        </ReferenceInput>
                                    }
                                </React.Fragment>

                            )


                        }}
                    </FormDataConsumer>

                    {/* <SelectInput source="type" choices={[
                        { id: 'organization', name: 'organization' },
                        { id: 'space', name: 'space' },
                        { id: 'component', name: 'component' },
                    ]} />
                    <ReferenceInput label="component" source="component" reference="components">
                        <SelectInput optionText="id" />
                    </ReferenceInput>
                    <TextInput source="role" /> */}
                </SimpleFormIterator>
            </ArrayInput>

        </SimpleForm>
    </Edit>
);

export default MemberEdit;