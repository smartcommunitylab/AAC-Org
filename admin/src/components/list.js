import * as React from "react";
import { List, Datagrid, EditButton } from 'react-admin';
import { TextField, ChipField, ArrayField, SingleFieldList, ReferenceField } from 'react-admin';
import TextArrayField from '../fields/TextArrayField';

const ComponentsList = ({ permissions, ...props }) => (
    <List {...props} perPage={500} pagination={false}>
        <Datagrid rowClick="show">
            <TextField source="id" />
            <TextField source="name" />
            <TextField source="path" />
            {/* <TextArrayField source="spaces" /> */}
            {/* <ArrayField source="spaces" fieldKey="id" > */}
            <ArrayField source="spaces" >
                <SingleFieldList>
                    {/* <TextField source="id" /> */}
                    <ReferenceField source="id" reference="spaces" link="show">
                        <ChipField source="id" />
                    </ReferenceField>
                </SingleFieldList>
            </ArrayField>
            <EditButton />
        </Datagrid>
    </List>
);

export default ComponentsList;