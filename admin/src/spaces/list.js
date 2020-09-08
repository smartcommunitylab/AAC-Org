import * as React from "react";
import { List, Datagrid, TextField, EditButton, DeleteButton } from 'react-admin';


const SpacesList = ({ permissions, ...props }) => (
    <List {...props} perPage={500} pagination={false}>
        <Datagrid rowClick="show">
            <TextField source="id" />
            <TextField source="name" />
            <TextField source="organization" />
            <TextField source="path" />
            <EditButton />
            <DeleteButton undoable={false} />
        </Datagrid>
    </List>
);

export default SpacesList;