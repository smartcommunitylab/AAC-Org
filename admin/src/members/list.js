import * as React from "react";
import { List, Datagrid, SingleFieldList } from 'react-admin';
import { TextField, BooleanField, ArrayField } from 'react-admin';

import RoleFieldChip from '../fields/RoleFieldChip'

const MembersList = ({ permissions, ...props }) => (
    <List {...props} perPage={500} pagination={false}>
        <Datagrid rowClick="show">
            <TextField source="id" />
            <TextField source="username" />
            <TextField source="fullName" />
            <BooleanField source="owner" />
            {/* <ArrayField source="roles">
                <SingleFieldList linkType={false}>
                    <RoleFieldChip />
                </SingleFieldList>
            </ArrayField> */}
        </Datagrid>
    </List>
);

export default MembersList;