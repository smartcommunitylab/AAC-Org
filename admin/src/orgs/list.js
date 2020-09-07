import * as React from "react";
import { List, Datagrid, ReferenceField } from 'react-admin';
import { TextField, BooleanField, ArrayField } from 'react-admin';
import { EditButton } from 'react-admin';

import SelectButton from "../fields/SelectButton"
// import { Button } from '@material-ui/core';

// import orgSelect from '../orgSelectAction'

// const SelectButton = ({ classes, record, ...rest }) => {

//     const handleSelect = () => {
//         console.log("selected")
//         // console.dir(record)
//         //const org = localStorage.setItem('slug', record.id);
//         this.props.orgSelect(record.id)
//     };

//     return (
//         <Button
//             onClick={handleSelect}
//             label="Select"
//             {...rest}
//         >
//             Select
//         </Button>
//     );
// };


export const OrganizationsList = ({ permissions, ...props }) => (
    <List {...props} perPage={500} pagination={false}>
        <Datagrid rowClick="">
            <TextField source="id" />
            <TextField source="name" />
            {/* <TextField source="slug" /> */}
            <ReferenceField label="Owner" source="owner" reference="users" link={false}>
                <TextField source="username" />
            </ReferenceField >
            <TextField source="path" />
            <EditButton />
            <SelectButton />
        </Datagrid>
    </List>
);

export default OrganizationsList;